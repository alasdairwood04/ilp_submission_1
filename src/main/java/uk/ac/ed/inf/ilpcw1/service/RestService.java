package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service // Marks this class as a Spring service component
public class RestService {
    private static final double CLOSE_DISTANCE = 0.00015;
    private static final double MOVE_LENGTH = 0.00015;


    public double calculateDistance(LngLat position1, LngLat position2) {

        // using Euclidean distance formula
        double LongDiff = position1.getLongitude() - position2.getLongitude();
        double LatDiff = position1.getLatitude() - position2.getLatitude();
        return Math.sqrt(LongDiff * LongDiff + LatDiff * LatDiff);
    }

    public boolean isCloseTo(LngLat position1, LngLat position2) {
        double distance = calculateDistance(position1, position2);
        return distance < CLOSE_DISTANCE;
    }


    public LngLat nextPosition(LngLat start, double angle) {
        double angleInRadians = Math.toRadians(angle);

        double lngChange = MOVE_LENGTH * Math.cos(angleInRadians);
        double latChange = MOVE_LENGTH * Math.sin(angleInRadians);

        // Create new position
        double newLng = start.getLongitude() + lngChange;
        double newLat = start.getLatitude() + latChange;

        return new LngLat(newLng, newLat);
    }


    public boolean isInRegion(LngLat position, Region region) {
        var vertices = region.getVertices();
        // logger to log the vertices
        Logger logger = LoggerFactory.getLogger(RestService.class);
        logger.info("Region vertices: " + vertices);

        // Check if the point is on any of the polygon's edges
        boolean onEdge = isPointOnPolygonEdge(position, vertices);
        if (onEdge) {
            return true;
        }

        // Ray-casting algorithm to determine if the point is in the polygon
        int intersectCount = getIntersectCount(position, vertices);
        return intersectCount % 2 != 0;
    }

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
                if (Math.abs(area) < 1e-10) { // Use a small threshold to account for floating-point precision
                    return true;
                }
            }
        }
        return false;
    }

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
