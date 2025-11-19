package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;
import uk.ac.ed.inf.ilpcw1.data.RestrictedArea;

import java.util.*;

/**
 * Service for path finding operations
 */
@Service
public class PathfindingService {
    private static final Logger logger = LoggerFactory.getLogger(PathfindingService.class);
    private static final double MOVE_DISTANCE = 0.00015;
    private static final double CLOSE_DISTANCE = 0.00015;

    // 16 compass directions in degrees
    private static final double[] COMPASS_DIRECTIONS = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private final RestService restService;

    @Autowired
    public PathfindingService(RestService restService) {
        this.restService = restService;
    }

    /**
     * Find a path from start to goal avoiding restricted areas
     * Uses A* algorithm with Manhattan distance heuristic
     *
     * @param start Starting position
     * @param goal Goal position
     * @param restrictedAreas Areas to avoid
     * @return List of positions forming the path (including start, excluding goal)
     */
    public List<LngLat> findPath(LngLat start, LngLat goal, List<RestrictedArea> restrictedAreas) {
        logger.info("Finding path from {} to {}", start, goal);

        logger.info("Start Lng: {}, Goal Lng: {}", start.getLongitude(), goal.getLongitude());
        logger.info("Start Lat: {}, Goal Lat: {}", start.getLatitude(), goal.getLatitude());

        // If already close to goal, return direct path
        if (restService.isCloseTo(start, goal)) {
            logger.info("Start is already close to goal");
            return new ArrayList<>(List.of(start));
        }

        // Priority queue for A* (ordered by f = g + h)
        // let Q be a priority queue
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));

        // This map effectively stores cost (g-score), fscore, and parent for all discovered nodes.
        // It replaces the pseudocode's cost[], fscore[], and parent[] arrays/maps.
        // Nodes not in the map are considered to have cost[v] = INFINITY.
        Map<String, Node> allNodes = new HashMap<>();

        // Set<String> closedSet = new HashSet<>(); // This maps to "label u as explored"
        Set<String> processed = new HashSet<>();


        // Create start node
        // cost[source] := 0
        // fscore[source] := heuristic(source)
        double startHeuristic = heuristic(start, goal);
        Node startNode = new Node(start, null, 0, startHeuristic);

        // Q.add_with_priority(source, fscore[source])
        priorityQueue.add(startNode);
        allNodes.put(positionKey(start), startNode);

        int iterations = 0;
        final int MAX_ITERATIONS = 50000; // Prevent infinite loops

        List<Region> noFlyZones = restrictedAreas.stream()
                .map(this::convertToRegion)
                .toList();


        // while Q is not empty do
        while (!priorityQueue.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;

            // u := Q.extract_min()
            Node current = priorityQueue.poll();
            String currentKey = positionKey(current.position);

            // if u is goal then return calculatePath(parent, goal)
            if (restService.isCloseTo(current.position, goal)) {
                logger.info("Path found in {} iterations with {} moves", iterations, current.g);
                return reconstructPath(current);
            }

            // if u is not labelled as explored then
            // This handles stale entries in the queue (if we found a better path
            // to this node *after* it was already in the queue).
            if (processed.contains(currentKey)) {
                continue;
            }

            // label u as explored
            processed.add(currentKey);

            // for all edges from u to v in G.adjacentEdges(u) do
            for (double angle : COMPASS_DIRECTIONS) {
                LngLat nextPos = restService.nextPosition(current.position, angle);
                String nextKey = positionKey(nextPos);

                // if v is not labelled as explored then
                if (processed.contains(nextKey)) {
                    continue;
                }

                // Domain-specific check: Skip if position intersects with restricted areas
                if (intersectsRestrictedArea(current.position, nextPos, noFlyZones)) {
                    continue;
                }

                // new_cost := cost[u] + Graph.Edges(u, v)
                // (Each move costs 1)
                double tentativeG = current.g + 1;

                Node nextNode = allNodes.get(nextKey);

                // if new_cost < cost[v] then
                // (if nextNode is null, its cost is considered INFINITY)
                if (nextNode == null || tentativeG < nextNode.g) {
                    double h = heuristic(nextPos, goal);

                    if (nextNode == null) {
                        // First time seeing this node. Create it and store it.
                        // parent[v] := u
                        // cost[v] := new_cost
                        // fscore[v] := new_cost + heuristic(v)
                        nextNode = new Node(nextPos, current, tentativeG, h);
                        allNodes.put(nextKey, nextNode);
                    } else {
                        // Found a better path to an existing node. Update it.
                        // parent[v] := u
                        // cost[v] := new_cost
                        // fscore[v] := new_cost + heuristic(v)
                        nextNode.parent = current;
                        nextNode.g = tentativeG;
                        nextNode.f = tentativeG + h;
                        // We DO NOT remove the old entry from priorityQueue, per the pseudocode.
                    }

                    // Q.add_with_priority(v, fscore[v])
                    priorityQueue.add(nextNode);
                }
            }
        }

        logger.warn("No path found after {} iterations", iterations);
        // Return path to the closest node found as fallback
//        return new ArrayList<>(List.of(start));
        return null;
    }

    /**
     * Calculate heuristic (estimated cost) from current to goal
     * Uses Euclidean distance divided by move distance
     */
    private double heuristic(LngLat from, LngLat to) {
        return restService.calculateDistance(from, to) / MOVE_DISTANCE;
    }

    /**
     * Create unique key for position (rounded to avoid floating point issues)
     */
    private String positionKey(LngLat pos) {
        return String.format("%.8f,%.8f", pos.getLongitude(), pos.getLatitude());
    }

    /**
     * Reconstruct path from goal node back to start
     */
    private List<LngLat> reconstructPath(Node goalNode) {
        List<LngLat> path = new ArrayList<>();
        Node current = goalNode;

        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Check if line segment from pos1 to pos2 intersects any restricted area
     */
    private boolean intersectsRestrictedArea(LngLat pos1, LngLat pos2, List<Region> noFlyZones) {
        for (Region region : noFlyZones) {
            // Check if either endpoint is inside the restricted area
            // Use 'region' directly, do not call convertToRegion()
            if (restService.isInRegion(pos1, region)) {
                return true;
            }
            if (restService.isInRegion(pos2, region)) {
                return true;
            }

            // Check if line segment intersects any edge
            List<LngLat> vertices = region.getVertices();
            for (int i = 0; i < vertices.size() - 1; i++) {
                if (lineSegmentsIntersect(pos1, pos2, vertices.get(i), vertices.get(i + 1))) {
                    return true;
                }
            }
        }
        return false;    }

    /**
     * Convert RestrictedArea to Region for compatibility with isInRegion
     */
    private Region convertToRegion(RestrictedArea area) {
        return Region.builder()
                .name(area.getName())
                .vertices(area.getVertices())
                .build();
    }

    /**
     * Check if two line segments intersect
     */
    private boolean lineSegmentsIntersect(LngLat p1, LngLat p2, LngLat p3, LngLat p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);

        // General case
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    /**
     * Calculate direction/orientation for line segment test
     */
    private double direction(LngLat p1, LngLat p2, LngLat p3) {
        return (p3.getLongitude() - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude()) -
                (p2.getLongitude() - p1.getLongitude()) * (p3.getLatitude() - p1.getLatitude());
    }
}