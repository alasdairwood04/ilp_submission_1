package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidAngleException;

import java.util.List;
import java.util.Set;


@Service
public class ValidationService {

    // Valid attribute names for drone capabilities
    private static final Set<String> VALID_CAPABILITIES = Set.of(
            "cooling", "heating", "capacity", "maxMoves", "max_moves",
            "costPerMove", "costInitial", "cost_initial",
            "costFinal"
    );

    // Valid operators for queries
    private static final Set<String> NUMERIC_OPERATORS = Set.of("=", "==", "!=", "<", ">", "<=", ">=");
    private static final Set<String> BOOLEAN_OPERATORS = Set.of("=", "==");


    /**
     * Validates a LngLat object to ensure its longitude and latitude are valid and within range.
     *
     * The validation checks include:
     * 1. Both longitude and latitude fields are non-null.
     * 2. Both values are valid, non-NaN, and non-infinite numbers.
     * 3. Longitude is within the range [-180, 180].
     * 4. Latitude is within the range [-90, 90].
     *
     * @param position The LngLat object to validate.
     * @param fieldName The name of the field being validated (e.g., "position1") used in error messages.
     * @throws InvalidRequestException If either longitude or latitude is null (missing).
     * @throws InvalidCoordinateException If a coordinate is NaN, infinite, or outside the valid geographic range
     * (longitude: [-180, 180], latitude: [-90, 90]).
     */
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


    /**
     * Validate a DistanceRequest object.
     *
     * Checks performed:
     * 1. The request object is not null.
     * 2. Both position1 and position2 are present (not null).
     * 3. Each position's longitude and latitude are valid numbers and within acceptable geographic ranges
     *
     * @param request The DistanceRequest object to validate.
     * @throws InvalidRequestException If the request or a required field (position1, position2) is null/missing.
     * @throws InvalidCoordinateException If any coordinate within position1 or position2 is invalid (NaN, infinite, or out of range).
     */
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


    /**
     * Validate a CloseToRequest object.
     *
     * Checks performed:
     * 1. The request object is not null.
     * 2. Both position1 and position2 are present (not null).
     * 3. Each position's longitude and latitude are valid numbers and within acceptable geographic ranges
     *
     * @param request - The CloseToRequest object to validate.
     * @throws InvalidRequestException - If the request or a required field (position1, position2) is null/missing.
     * @throws InvalidCoordinateException - If any coordinate within position1 or position2 is invalid (NaN, infinite, or out of range).
     */
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


    /**
     * Validate a NextPositionRequest object.
     *
     * Checks performed:
     * 1. The request object is not null.
     * 2. The start position and angle are present (not null).
     * 3. The start position's longitude and latitude are valid numbers and within acceptable geographic ranges.
     * 4. The angle is a valid number (not NaN or infinite) and within the range [0, 360).
     *
     * @param request - The NextPositionRequest object to validate.
     * @throws InvalidRequestException - If the request or a required field (start, angle) is null/missing.
     * @throws InvalidCoordinateException - If any coordinate within the start position is invalid (NaN, infinite, or out of range).
     * @throws InvalidAngleException - If the angle is invalid (NaN, infinite, or out of range).
     */

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
            throw new InvalidAngleException("'angle' must be a valid number");
        }

        // Check range
        if (angle < 0 || angle >= 360) {
            throw new InvalidAngleException("'angle' must be between 0 (inclusive) and 360 (exclusive)");
        }
    }


    /**
     * Validate a RegionRequest object.
     *
     * Checks performed:
     * 1. The request object is not null.
     * 2. The position is present (not null) and valid.
     * 3. The region is present (not null).
     * 4. The region name is present (not null or empty).
     * 5. The region has at least 4 vertices to form a closed polygon.
     * 6. Each vertex's longitude and latitude are valid numbers and within acceptable geographic ranges.
     * 7. The first and last vertices are the same to ensure the polygon is closed
     *
     * @param request - The RegionRequest object to validate.
     * @throws InvalidRequestException - If the request or a required field (position, region, region.name, region.vertices) is null/missing or invalid.
     * @throws InvalidCoordinateException - If any coordinate within the position or region vertices is invalid (NaN, infinite, or out of range).
     *
     */
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

    // ==================== CW2 STATIC QUERY VALIDATIONS ====================

    /**
     * Validate drone ID for droneDetails endpoint
     * @param id The drone ID to validate
     * @throws InvalidRequestException if ID is null or invalid
     */
    public void validateDroneId(String id) {
        if (id == null) {
            throw new InvalidRequestException("Drone ID cannot be null");
        }
    }

    /**
     * Validate dronesWithCooling endpoint parameters
     * Note: Spring automatically converts path variable to boolean,
     * but we can add additional validation if needed
     * @param state The cooling state (true/false)
     */
    public void validateCoolingState(Boolean state) {
        if (state == null) {
            throw new InvalidRequestException("Cooling state cannot be null");
        }
        // Boolean validation is straightforward - Spring handles conversion
    }
}