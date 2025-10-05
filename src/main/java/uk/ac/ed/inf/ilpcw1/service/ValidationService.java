package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.DistanceRequest;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

@Service
public class ValidationService {

    public void validateDistanceRequest(DistanceRequest request) {
        // Check for null request
        if (request == null) {
            throw new InvalidRequestException("Request body cannot be null");
        }

        // Check for null positions
        if (request.getPosition1() == null) {
            throw new InvalidRequestException("'position1' is required");
        }
        if (request.getPosition2() == null) {
            throw new InvalidRequestException("'position2' is required");
        }

        // Validate position1
        validateLngLat(request.getPosition1(), "position1");

        // Validate position2
        validateLngLat(request.getPosition2(), "position2");
    }

    private void validateLngLat(LngLat position, String fieldName) {
        // Check for null fields
        if (position.getLongitude() == null) {
            throw new InvalidRequestException(fieldName + ".lng is required");
        }
        if (position.getLatitude() == null) {
            throw new InvalidRequestException(fieldName + ".lat is required");
        }

        double lng = position.getLongitude();
        double lat = position.getLatitude();

        // Check for NaN or Infinity
        if (Double.isNaN(lng) || Double.isInfinite(lng)) {
            throw new InvalidCoordinateException(fieldName + ".lng must be a valid number");
        }
        if (Double.isNaN(lat) || Double.isInfinite(lat)) {
            throw new InvalidCoordinateException(fieldName + ".lat must be a valid number");
        }

        // Check range
        if (lng < -180 || lng > 180) {
            throw new InvalidCoordinateException(fieldName + ".lng must be between -180 and 180");
        }
        if (lat < -90 || lat > 90) {
            throw new InvalidCoordinateException(fieldName + ".lat must be between -90 and 90");
        }
    }
}