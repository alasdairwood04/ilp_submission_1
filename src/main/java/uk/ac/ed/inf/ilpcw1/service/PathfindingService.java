package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.CoordinateKey;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;
import uk.ac.ed.inf.ilpcw1.data.RestrictedArea;

import java.util.*;

@Service
public class PathfindingService {
    private static final Logger logger = LoggerFactory.getLogger(PathfindingService.class);
    private static final double MOVE_DISTANCE = 0.00015;

    // Use 1.0 for strictly shortest path.
    // Use 1.0001 to break ties (prefer paths closer to goal) without noticeably affecting cost.
    private static final double HEURISTIC_WEIGHT = 1.0001;

    private static final double[] COMPASS_DIRECTIONS = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private final RestService restService;

    @Autowired
    public PathfindingService(RestService restService) {
        this.restService = restService;
    }

    public List<LngLat> findPath(LngLat start, LngLat goal, List<RestrictedArea> restrictedAreas) {
        logger.info("Finding path from {} to {}", start, goal);

        if (restService.isCloseTo(start, goal)) {
            logger.info("Start is already close to goal");
            return new ArrayList<>(List.of(start));
        }

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<CoordinateKey, Node> allNodes = new HashMap<>();

        List<Region> noFlyZones = restrictedAreas.stream()
                .map(this::convertToRegion)
                .toList();

        double startHeuristic = heuristic(start, goal);
        Node startNode = new Node(start, null, 0, startHeuristic);

        CoordinateKey startKey = CoordinateKey.fromLngLat(start);
        priorityQueue.add(startNode);
        allNodes.put(startKey, startNode);

        int iterations = 0;
        final int MAX_ITERATIONS = 100000;

        while (!priorityQueue.isEmpty()) {
            iterations++;
            if (iterations > MAX_ITERATIONS) {
                logger.warn("No path found after {} iterations (Max Reached)", iterations);
                return null;
            }

            Node current = priorityQueue.poll();

            CoordinateKey currentKey = CoordinateKey.fromLngLat(current.position);
            Node bestKnown = allNodes.get(currentKey);
            if (bestKnown != null && bestKnown.g < current.g) {
                continue;
            }

            if (restService.isCloseTo(current.position, goal)) {
                logger.info("Path found in {} iterations with {} moves", iterations, current.g);
                return reconstructPath(current);
            }

            for (double angle : COMPASS_DIRECTIONS) {
                LngLat nextPos = restService.nextPosition(current.position, angle);
                CoordinateKey nextKey = CoordinateKey.fromLngLat(nextPos);

                if (intersectsRestrictedArea(current.position, nextPos, noFlyZones)) {
                    continue;
                }

                double tentativeG = current.g + 1;
                Node nextNode = allNodes.get(nextKey);

                if (nextNode == null || tentativeG < nextNode.g) {
                    double h = heuristic(nextPos, goal);
                    double f = tentativeG + (h * HEURISTIC_WEIGHT);

                    if (nextNode == null) {
                        nextNode = new Node(nextPos, current, tentativeG, h);
                        nextNode.f = f;
                        allNodes.put(nextKey, nextNode);
                        priorityQueue.add(nextNode);
                    } else {
                        nextNode.parent = current;
                        nextNode.g = tentativeG;
                        nextNode.f = f;
                        priorityQueue.add(nextNode);
                    }
                }
            }
        }

        logger.warn("No path found - Priority Queue exhausted after {} iterations", iterations);
        return null;
    }

    private double heuristic(LngLat from, LngLat to) {
        return restService.calculateDistance(from, to) / MOVE_DISTANCE;
    }

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

    private boolean intersectsRestrictedArea(LngLat pos1, LngLat pos2, List<Region> noFlyZones) {
        for (Region region : noFlyZones) {
            if (restService.isInRegion(pos1, region) || restService.isInRegion(pos2, region)) {
                return true;
            }
            List<LngLat> vertices = region.getVertices();
            for (int i = 0; i < vertices.size() - 1; i++) {
                if (lineSegmentsIntersect(pos1, pos2, vertices.get(i), vertices.get(i + 1))) {
                    return true;
                }
            }
            if (!vertices.isEmpty() && lineSegmentsIntersect(pos1, pos2, vertices.get(vertices.size() - 1), vertices.get(0))) {
                return true;
            }
        }
        return false;
    }

    private Region convertToRegion(RestrictedArea area) {
        return Region.builder()
                .name(area.getName())
                .vertices(area.getVertices())
                .build();
    }

    private boolean lineSegmentsIntersect(LngLat p1, LngLat p2, LngLat p3, LngLat p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }
        return false;
    }

    private double direction(LngLat p1, LngLat p2, LngLat p3) {
        return (p3.getLongitude() - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude()) -
                (p2.getLongitude() - p1.getLongitude()) * (p3.getLatitude() - p1.getLatitude());
    }
}