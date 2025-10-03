package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;

@Service // Marks this class as a Spring service component
public class RestService {
    private static final double CLOSE_DISTANCE = 0.00015;


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
}
