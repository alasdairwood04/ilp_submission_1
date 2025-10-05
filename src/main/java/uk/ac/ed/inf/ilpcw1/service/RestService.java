package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;

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

        // Check if region is closed (first vertex equals last vertex)
        LngLat first = vertices.get(0);
        LngLat last = vertices.get(vertices.size() - 1);

//        if (!arePositionsEqual(first, last)) {
//
//        }

        return true;
    }

    private boolean arePositionsEqual(LngLat first, LngLat last) {
        double first_lat = first.getLatitude();
        double first_lng = first.getLongitude();

        double last_lat = last.getLatitude();
        double last_lng = last.getLongitude();

        // if coordinates equal
        return first_lat == last_lat && first_lng == last_lng;
    }
}
