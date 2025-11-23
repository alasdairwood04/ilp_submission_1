package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ed.inf.ilpcw1.data.RestrictedArea;

import java.util.List;

@Service
public class RestService {
    private static final double CLOSE_DISTANCE = 0.00015;
    private static final double MOVE_LENGTH = 0.00015;

    /**
        * Calculate the Euclidean distance between two geographical positions.
        * @param position1 The first geographical position.
        * @param position2 The second geographical position.
        * @return The Euclidean distance between the two positions.
     */
    public double calculateDistance(LngLat position1, LngLat position2) {

        // using Euclidean distance formula
        double LongDiff = position1.getLongitude() - position2.getLongitude();
        double LatDiff = position1.getLatitude() - position2.getLatitude();
        return Math.sqrt(LongDiff * LongDiff + LatDiff * LatDiff);
    }

    /**
     * Check if two geographical positions are within a close distance.
     * This relies on result of {@link #calculateDistance(LngLat, LngLat)}.
     * @param position1 - The first position.
     * @param position2 - The second position.
     * @return - True if the positions are within CLOSE_DISTANCE (0.00015), false otherwise.
     */
    public boolean isCloseTo(LngLat position1, LngLat position2) {
        double distance = calculateDistance(position1, position2);
        return distance < CLOSE_DISTANCE;
    }


    /**
     * Calculate the next geographical position based on a starting position and an angle.
     * @param start - The starting geographical position.
     * @param angle - The angle in degrees (0 degrees is east/positive longitude change).
     * @return - The new geographical position after moving MOVE_LENGTH (0.00015) in the specified direction.
     */
    public LngLat nextPosition(LngLat start, double angle) {
        double angleInRadians = Math.toRadians(angle);

        double lngChange = MOVE_LENGTH * Math.cos(angleInRadians);
        double latChange = MOVE_LENGTH * Math.sin(angleInRadians);

        // Create new position
        double newLng = start.getLongitude() + lngChange;
        double newLat = start.getLatitude() + latChange;

        return LngLat.builder()
                .longitude(newLng)
                .latitude(newLat)
                .build();
    }


    /**
     * Check if a geographical position is inside a given region (polygon).
     * Uses Ray casting algorithm to determine if the point is in the polygon.
     * A point on the edge of the polygon is considered inside.
     * @param position - The geographical position to check.
     * @param region - The region defined by a polygon.
     * @return - True if the position is inside the region or on its edge, false otherwise.
     */
    public boolean isInRegion(LngLat position, Region region) {
        var vertices = region.getVertices();

        // First, do a bounding box check for quick exclusion
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        for (LngLat v : vertices) {
            if (v.getLongitude() < minLng) minLng = v.getLongitude();
            if (v.getLongitude() > maxLng) maxLng = v.getLongitude();
            if (v.getLatitude() < minLat) minLat = v.getLatitude();
            if (v.getLatitude() > maxLat) maxLat = v.getLatitude();
        }

        if (position.getLongitude() < minLng || position.getLongitude() > maxLng ||
                position.getLatitude() < minLat || position.getLatitude() > maxLat) {
            return false;
        }

        // logger to log the vertices
        Logger logger = LoggerFactory.getLogger(RestService.class);

        // Check if the point is on any of the polygon's edges
        boolean onEdge = isPointOnPolygonEdge(position, vertices);
        if (onEdge) {
            return true;
        }
            // Ray-casting algorithm to determine if the point is in the polygon
        int intersectCount = getIntersectCount(position, vertices);
        return intersectCount % 2 != 0;
    }


    public boolean isInRegionRestrictedArea(LngLat position, RestrictedArea region) {
        var vertices = region.getVertices();
        // logger to log the vertices
        Logger logger = LoggerFactory.getLogger(RestService.class);

        // Check if the point is on any of the polygon's edges
        boolean onEdge = isPointOnPolygonEdge(position, vertices);
        if (onEdge) {
            return true;
        }
        // Ray-casting algorithm to determine if the point is in the polygon
        int intersectCount = getIntersectCount(position, vertices);
        return intersectCount % 2 != 0;
    }


    public boolean isInNoFlyZone(LngLat position, List<RestrictedArea> noFlyZones) {
        for (RestrictedArea region : noFlyZones) {
            if (isInRegionRestrictedArea(position, region)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a point is on the edge of a polygon.
     * Checks if the point is collinear with any edge of the polygon and lies within that edge's bounding box.
     * Tolerance of 1e-10 is used to account for floating-point precision.
     * @param position - The point to check.
     * @param vertices - The vertices of the polygon.
     * @return - True if the point is on the edge of the polygon, false otherwise.
     */
    private boolean isPointOnPolygonEdge(LngLat position, List<LngLat> vertices) {
        for (int i = 0; i < vertices.size(); i++) {
            LngLat v1 = vertices.get(i);
            LngLat v2 = vertices.get((i + 1) % vertices.size());

            // Check if the point is within the bounding box of the edge
            if (Math.min(v1.getLongitude(), v2.getLongitude()) <= position.getLongitude() && position.getLongitude() <= Math.max(v1.getLongitude(), v2.getLongitude()) &&
                    Math.min(v1.getLatitude(), v2.getLatitude()) <= position.getLatitude() && position.getLatitude() <= Math.max(v1.getLatitude(), v2.getLatitude())) {

                // Check if the area of the triangle formed by the point and the edge is zero (collinear)
                double area = (v1.getLongitude() - position.getLongitude()) * (v2.getLatitude() - position.getLatitude()) -
                        (v2.getLongitude() - position.getLongitude()) * (v1.getLatitude() - position.getLatitude());
                if (Math.abs(area) < 1e-10) { // small threshold to account for floating-point precision
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Count the number of times a horizontal ray from the point intersects with the edges of the polygon.
     * Core of the Ray casting algorithm.
     * Odd count means the point is inside, even count means outside.
     * @param position - The point from which the ray is cast.
     * @param vertices - The vertices of the polygon.
     * @return - The number of intersections as an integer.
     */
    private static int getIntersectCount(LngLat position, List<LngLat> vertices) {
        int intersectCount = 0;
        for (int i = 0; i < vertices.size(); i++) {
            LngLat v1 = vertices.get(i);
            LngLat v2 = vertices.get((i + 1) % vertices.size());

            // Check if the ray intersects with the edge
            if (((v1.getLatitude() > position.getLatitude()) != (v2.getLatitude() > position.getLatitude())) && // Compute the latitude intersection
                    // Check if the point is to the left of the intersection point
                    (position.getLongitude() < ((v2.getLongitude() - v1.getLongitude()) * (position.getLatitude()
                            - v1.getLatitude())) / (v2.getLatitude() - v1.getLatitude()) + v1.getLongitude())) {
                intersectCount++;
            }
        }
        return intersectCount;
    }
}
