package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

@Service
public class ValidationService {

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

    public void validateCloseTo (CloseToRequest request) {
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


    public void validateNextPositionRequest(NextPositionRequest request) {
        // Check for null request
        if (request == null) {
            throw new InvalidRequestException("Request body cannot be null");
        }

        // Check for null start position
        if (request.getStart() == null) {
            throw new InvalidRequestException("'start' is required");
        }

        // Validate start position
        validateLngLat(request.getStart(), "start");

        // Check for null angle
        if (request.getAngle() == null) {
            throw new InvalidRequestException("'angle' is required");
        }

        double angle = request.getAngle();

        // Check for NaN or Infinity
        if (Double.isNaN(angle) || Double.isInfinite(angle)) {
            throw new InvalidCoordinateException("'angle' must be a valid number");
        }

        // Check range
        if (angle < 0 || angle >= 360) {
            throw new InvalidCoordinateException("'angle' must be between 0 (inclusive) and 360 (exclusive)");
        }
    }

    public void validateIsInRegionRequest(RegionRequest request) {
        // Check for null request
        if (request == null) {
            throw new InvalidRequestException("Request body cannot be null");
        }

        // Check for null position
        if (request.getPosition() == null) {
            throw new InvalidRequestException("'position' is required");
        }

        // Validate position
        validateLngLat(request.getPosition(), "position");

        // Check for null region
        if (request.getRegion() == null) {
            throw new InvalidRequestException("'region' is required");
        }

        Region region = request.getRegion();

        // Check for null or empty region name
        if (region.getName() == null || region.getName().trim().isEmpty()) {
            throw new InvalidRequestException("'region.name' is required and cannot be empty");
        }

        // Check for null or insufficient vertices - using 4 because a closed polygon needs at least 4 points (first and last are the same)
        // so a triangle would need 4 points to close
        if (region.getVertices() == null || region.getVertices().size() < 4) {
            throw new InvalidRequestException("'region.vertices' must contain at least 4 vertices to form a closed polygon");
        }

        // Validate each vertex
        for (int i = 0; i < region.getVertices().size(); i++) {
            LngLat vertex = region.getVertices().get(i);
            if (vertex == null) {
                throw new InvalidRequestException("'region.vertices[" + i + "]' cannot be null");
            }
            validateLngLat(vertex, "region.vertices[" + i + "]");
        }

        // Check if the first and last vertices are the same to ensure the polygon is closed
        LngLat firstVertex = region.getVertices().getFirst();
        LngLat lastVertex = region.getVertices().getLast();
        if (!firstVertex.equals(lastVertex)) {
            throw new InvalidRequestException("The first and last vertices in 'region.vertices' must be the same to form a closed polygon");
        }

    }
}