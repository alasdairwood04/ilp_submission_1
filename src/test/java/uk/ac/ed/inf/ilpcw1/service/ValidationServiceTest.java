package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.InvalidAngleException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for RestService
 * Tests all geometric calculations and validations
 */

@DisplayName("ValidationService Tests")
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Nested
    @DisplayName("DistanceRequest Validation Tests")
    class DistanceRequestValidationTests {

        @Test
        @DisplayName("Should accept valid distance request")
        void shouldAcceptValidDistanceRequest() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            assertDoesNotThrow(() -> validationService.validateDistanceRequest(request));
        }

        @Test
        @DisplayName("Should reject null request")
        void shouldRejectNullRequest() {
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateDistanceRequest(null)
            );
            assertEquals("Request body cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null position1")
        void shouldRejectNullPosition1() {
            DistanceRequest request = DistanceRequest.builder()
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("'position1' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null position2")
        void shouldRejectNullPosition2() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("'position2' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null longitude in position1")
        void shouldRejectNullLongitudeInPosition1() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lng is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null latitude in position1")
        void shouldRejectNullLatitudeInPosition1() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lat is required", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-180.1, -200.0, 180.1, 200.0})
        @DisplayName("Should reject invalid longitude values in position1")
        void shouldRejectInvalidLongitudesInPosition1(double lng) {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(lng).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lng must be between -180 and 180", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-90.1, -100.0, 90.1, 100.0})
        @DisplayName("Should reject invalid latitude values in position1")
        void shouldRejectInvalidLatitudesInPosition1(double lat) {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(lat).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lat must be between -90 and 90", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject NaN longitude in position1")
        void shouldRejectNaNLongitudeInPosition1() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(Double.NaN).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lng must be a valid number", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject infinite longitude in position1")
        void shouldRejectInfiniteLongitudeInPosition1() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(Double.POSITIVE_INFINITY).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateDistanceRequest(request)
            );
            assertEquals("position1.lng must be a valid number", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept boundary values")
        void shouldAcceptBoundaryValues() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-180.0).latitude(-90.0).build())
                    .position2(LngLat.builder().longitude(180.0).latitude(90.0).build())
                    .build();

            assertDoesNotThrow(() -> validationService.validateDistanceRequest(request));
        }

        @Test
        @DisplayName("Should accept identical positions")
        void shouldAcceptIdenticalPositions() {
            DistanceRequest request = DistanceRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .build();

            assertDoesNotThrow(() -> validationService.validateDistanceRequest(request));
        }
    }

    @Nested
    @DisplayName("CloseToRequest Validation Tests")
    class CloseToRequestValidationTests {

        @Test
        @DisplayName("Should accept valid closeTo request")
        void shouldAcceptValidRequest() {
            CloseToRequest request = CloseToRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            assertDoesNotThrow(() -> validationService.validateCloseTo(request));
        }

        @Test
        @DisplayName("Should reject null request")
        void shouldRejectNullRequest() {
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateCloseTo(null)
            );
            assertEquals("Request body cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null position1")
        void shouldRejectNullPosition1() {
            CloseToRequest request = CloseToRequest.builder()
                    .position2(LngLat.builder().longitude(-3.192473).latitude(55.942617).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateCloseTo(request)
            );
            assertEquals("'position1' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null position2")
        void shouldRejectNullPosition2() {
            CloseToRequest request = CloseToRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateCloseTo(request)
            );
            assertEquals("'position2' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject invalid coordinates in position2")
        void shouldRejectInvalidCoordinatesInPosition2() {
            CloseToRequest request = CloseToRequest.builder()
                    .position1(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .position2(LngLat.builder().longitude(200.0).latitude(55.942617).build())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateCloseTo(request)
            );
            assertEquals("position2.lng must be between -180 and 180", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept very close positions")
        void shouldAcceptVeryClosePositions() {
            CloseToRequest request = CloseToRequest.builder()
                    .position1(LngLat.builder().longitude(0.0).latitude(0.0).build())
                    .position2(LngLat.builder().longitude(0.00001).latitude(0.00001).build())
                    .build();

            assertDoesNotThrow(() -> validationService.validateCloseTo(request));
        }
    }

    @Nested
    @DisplayName("NextPositionRequest Validation Tests")
    class NextPositionRequestValidationTests {

        @Test
        @DisplayName("Should accept valid nextPosition request with angle 0")
        void shouldAcceptValidRequestWithAngle0() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .angle(0.0)
                    .build();

            assertDoesNotThrow(() -> validationService.validateNextPositionRequest(request));
        }

        @Test
        @DisplayName("Should accept valid nextPosition request with angle 90")
        void shouldAcceptValidRequestWithAngle90() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .angle(90.0)
                    .build();

            assertDoesNotThrow(() -> validationService.validateNextPositionRequest(request));
        }

        @Test
        @DisplayName("Should reject null request")
        void shouldRejectNullRequest() {
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateNextPositionRequest(null)
            );
            assertEquals("Request body cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null start position")
        void shouldRejectNullStartPosition() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .angle(45.0)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("'start' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null angle")
        void shouldRejectNullAngle() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("'angle' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with invalid start position")
        void shouldRejectInvalidStartPosition() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(200.0).latitude(55.946233).build())
                    .angle(45.0)
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("start.lng must be between -180 and 180", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1, -45, 360, 361, 400})
        @DisplayName("Should reject invalid angle values")
        void shouldRejectInvalidAngles(double angle) {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .angle(angle)
                    .build();

            InvalidAngleException exception = assertThrows(
                    InvalidAngleException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("'angle' must be between 0 (inclusive) and 360 (exclusive)", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject NaN angle")
        void shouldRejectNaNAngle() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .angle(Double.NaN)
                    .build();

            InvalidAngleException exception = assertThrows(
                    InvalidAngleException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("'angle' must be a valid number", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject infinite angle")
        void shouldRejectInfiniteAngle() {
            NextPositionRequest request = NextPositionRequest.builder()
                    .start(LngLat.builder().longitude(-3.192473).latitude(55.946233).build())
                    .angle(Double.POSITIVE_INFINITY)
                    .build();

            InvalidAngleException exception = assertThrows(
                    InvalidAngleException.class,
                    () -> validationService.validateNextPositionRequest(request)
            );
            assertEquals("'angle' must be a valid number", exception.getMessage());
        }
        @Test
        @DisplayName("Should accept all valid angles from 0 to 359")
        void shouldAcceptValidAngles() {
            LngLat start = LngLat.builder().longitude(-3.192473).latitude(55.946233).build();

            for (double angle = 0; angle < 360; angle += 22.5) {
                NextPositionRequest request = NextPositionRequest.builder()
                        .start(start)
                        .angle(angle)
                        .build();

                assertDoesNotThrow(() -> validationService.validateNextPositionRequest(request),
                        "Should accept angle " + angle);
            }
        }
    }

    @Nested
    @DisplayName("RegionRequest Validation Tests")
    class RegionRequestValidationTests {

        private Region createValidRegion() {
            return Region.builder()
                    .name("central")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build()
                    ))
                    .build();
        }

        @Test
        @DisplayName("Should accept valid isInRegion request")
        void shouldAcceptValidRequest() {
            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(-3.123).latitude(55.23).build())
                    .region(createValidRegion())
                    .build();

            assertDoesNotThrow(() -> validationService.validateIsInRegionRequest(request));
        }

        @Test
        @DisplayName("Should reject null request")
        void shouldRejectNullRequest() {
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(null)
            );
            assertEquals("Request body cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null position")
        void shouldRejectNullPosition() {
            RegionRequest request = RegionRequest.builder()
                    .region(createValidRegion())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'position' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with null region")
        void shouldRejectNullRegion() {
            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region' is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject request with invalid position coordinates")
        void shouldRejectInvalidPosition() {
            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(200.0).latitude(1.222).build())
                    .region(createValidRegion())
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("position.lng must be between -180 and 180", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with null name")
        void shouldRejectRegionWithNullName() {
            Region region = Region.builder()
                    .name(null)
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.name' is required and cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with empty name")
        void shouldRejectRegionWithEmptyName() {
            Region region = Region.builder()
                    .name("")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.name' is required and cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with whitespace-only name")
        void shouldRejectRegionWithWhitespaceOnlyName() {
            Region region = Region.builder()
                    .name("   ")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.name' is required and cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with null vertices")
        void shouldRejectRegionWithNullVertices() {
            Region region = Region.builder()
                    .name("test")
                    .vertices(null)
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.vertices' must contain at least 4 vertices to form a closed polygon", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with empty vertices")
        void shouldRejectRegionWithEmptyVertices() {
            Region region = Region.builder()
                    .name("test")
                    .vertices(Collections.emptyList())
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.vertices' must contain at least 4 vertices to form a closed polygon", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with less than 4 vertices")
        void shouldRejectRegionWithTooFewVertices() {
            Region region = Region.builder()
                    .name("test")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(0.0).latitude(0.0).build(),
                            LngLat.builder().longitude(1.0).latitude(0.0).build(),
                            LngLat.builder().longitude(0.0).latitude(0.0).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.vertices' must contain at least 4 vertices to form a closed polygon", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with null vertex in list")
        void shouldRejectRegionWithNullVertexInList() {
            List<LngLat> vertices = new ArrayList<>();
            vertices.add(LngLat.builder().longitude(-3.192473).latitude(55.946233).build());
            vertices.add(LngLat.builder().longitude(-3.192473).latitude(55.942617).build());
            vertices.add(null);
            vertices.add(LngLat.builder().longitude(-3.184319).latitude(55.946233).build());
            vertices.add(LngLat.builder().longitude(-3.192473).latitude(55.946233).build());

            Region region = Region.builder()
                    .name("test")
                    .vertices(vertices)
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("'region.vertices[2]' cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region that is not closed (first != last)")
        void shouldRejectOpenRegion() {
            Region region = Region.builder()
                    .name("open")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build()
                            // Missing closing point
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("The first and last vertices in 'region.vertices' must be the same to form a closed polygon", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject region with invalid vertex coordinates")
        void shouldRejectRegionWithInvalidVertexCoordinates() {
            Region region = Region.builder()
                    .name("invalid")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(200.0).latitude(55.942617).build(), // Invalid
                            LngLat.builder().longitude(-3.184319).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(1.234).latitude(1.222).build())
                    .region(region)
                    .build();

            InvalidCoordinateException exception = assertThrows(
                    InvalidCoordinateException.class,
                    () -> validationService.validateIsInRegionRequest(request)
            );
            assertEquals("region.vertices[1].lng must be between -180 and 180", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept triangular region (minimum valid closed polygon)")
        void shouldAcceptTriangularRegion() {
            Region region = Region.builder()
                    .name("triangle")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(2.0).latitude(4.0).build(),
                            LngLat.builder().longitude(4.0).latitude(8.0).build(),
                            LngLat.builder().longitude(6.0).latitude(4.0).build(),
                            LngLat.builder().longitude(2.0).latitude(4.0).build() // Closed polygon - same as first
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(0.5).latitude(0.3).build())
                    .region(region)
                    .build();

            assertDoesNotThrow(() -> validationService.validateIsInRegionRequest(request));
        }

        @Test
        @DisplayName("Should accept concave region")
        void shouldAcceptConcaveRegion() {
            Region region = Region.builder()
                    .name("pentagon")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(0.0).latitude(0.0).build(),
                            LngLat.builder().longitude(0.0).latitude(8.0).build(),
                            LngLat.builder().longitude(8.0).latitude(8.0).build(),
                            LngLat.builder().longitude(8.0).latitude(0.0).build(), // Indentation
                            LngLat.builder().longitude(6.0).latitude(0.0).build(),
                            LngLat.builder().longitude(6.0).latitude(5.0).build(),
                            LngLat.builder().longitude(2.0).latitude(5.0).build(),
                            LngLat.builder().longitude(2.0).latitude(0.0).build(),
                            LngLat.builder().longitude(0.0).latitude(0.0).build()
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(0.5).latitude(0.3).build())
                    .region(region)
                    .build();

            assertDoesNotThrow(() -> validationService.validateIsInRegionRequest(request));
        }

        @Test
        @DisplayName("Should accept complex polygon region")
        void shouldAcceptComplexPolygonRegion() {
            Region region = Region.builder()
                    .name("complex")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.191000).latitude(55.949000).build(),
                            LngLat.builder().longitude(-3.187000).latitude(55.950000).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.949000).build(),
                            LngLat.builder().longitude(-3.183000).latitude(55.947500).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.944425).build(),
                            LngLat.builder().longitude(-3.187000).latitude(55.942617).build(),
                            LngLat.builder().longitude(-3.190000).latitude(55.943500).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build() // Closed polygon
                    ))
                    .build();

            RegionRequest request = RegionRequest.builder()
                    .position(LngLat.builder().longitude(0.5).latitude(0.5).build())
                    .region(region)
                    .build();

            assertDoesNotThrow(() -> validationService.validateIsInRegionRequest(request));
        }
    }
}