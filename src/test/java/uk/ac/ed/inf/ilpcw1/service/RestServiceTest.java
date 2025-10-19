package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for RestService
 * Tests all geometric calculations and validations
 */
@DisplayName("RestService Test Suite")
public class RestServiceTest {
    private RestService restService;


    private static final double TOLERANCE = 0.00015;
    private static final double MOVE_DISTANCE = 0.00015;
    private static final double EPSILON = 1e-12;

    @BeforeEach
    public void setUp() {
        restService = new RestService();
    }

    @Nested
    @DisplayName("Distance Calculation Tests")
    class DistanceCalculationTests {

        @Test
        @DisplayName("Calculate distance between two points - general case")
        void testCalculateDistance_GeneralValidPoints() {
            LngLat pointA = LngLat.builder().
                    longitude(-3.192473)
                    .latitude(55.946233)
                    .build();
            LngLat pointB = LngLat.builder().
                    longitude(-3.192473)
                    .latitude(55.942617)
                    .build();
            double expectedDistance = 0.003616;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation is incorrect");
        }

        @Test
        @DisplayName("Calculate distance between negative coordinates")
        void testCalculateDistance_NegativeCoordinates() {
            LngLat pointA = LngLat.builder()
                    .longitude(-1.0)
                    .latitude(-1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(-4.0)
                    .latitude(-5.0)
                    .build();
            double expectedDistance = 5.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation with negative coordinates is incorrect");
        }

        @Test
        @DisplayName("Calculate distance between identical points")
        void testCalculateDistance_IdenticalPoints() {
            LngLat pointA = LngLat.builder().
                    longitude(-3.192473)
                    .latitude(55.946233)
                    .build();
            LngLat pointB = LngLat.builder().
                    longitude(-3.192473)
                    .latitude(55.946233)
                    .build();

            double expectedDistance = 0.0;
            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance between identical points should be zero");
        }

        @Test
        @DisplayName("Calculate distance for large distance")
        void testCalculateDistance_LargeDistance() {
            LngLat pointA = LngLat.builder()
                    .longitude(0.0)
                    .latitude(0.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(3000.0)
                    .latitude(4000.0)
                    .build();
            double expectedDistance = 5000.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation for large distances is incorrect");
        }

        @Test
        @DisplayName("Calculate distance for very close points")
        void testCalculateDistance_VeryClosePoints() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.000001)
                    .latitude(1.000001)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.000002)
                    .latitude(1.000002)
                    .build();
            double expectedDistance = Math.sqrt(2) * 0.000001;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation for very close points is incorrect");
        }

        @Test
        @DisplayName("Calculate Zero Distance")
        void testCalculateDistance_ZeroDistance() {
            LngLat pointA = LngLat.builder()
                    .longitude(0.0)
                    .latitude(0.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(0.0)
                    .latitude(0.0)
                    .build();
            double expectedDistance = 0.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance between identical points should be zero");
        }

        @Test
        @DisplayName("Calculate Distance with High Precision")
        void testCalculateDistance_HighPrecision() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.123456789)
                    .latitude(1.123456789)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.123456790)
                    .latitude(1.123456790)
                    .build();
            double expectedDistance = Math.sqrt(2) * 0.000000001;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation with high precision is incorrect");
        }

        @Test
        @DisplayName("Calculate Distance horizontal line")
        void testCalculateDistance_HorizontalLine() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(4.0)
                    .latitude(1.0)
                    .build();
            double expectedDistance = 3.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation for horizontal line is incorrect");
        }

        @Test
        @DisplayName("Calculate Distance vertical line")
        void testCalculateDistance_VerticalLine() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0)
                    .latitude(5.0)
                    .build();
            double expectedDistance = 4.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation for vertical line is incorrect");
        }

        @Test
        @DisplayName("Calcualte Distance crossing meridian")
        void testCalculateDistance_CrossingMeridian() {
            LngLat pointA = LngLat.builder()
                    .longitude(179.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(-179.0)
                    .latitude(1.0)
                    .build();
            double expectedDistance = 358.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation crossing the meridian is incorrect");
        }

        @Test
        @DisplayName("Calculate Distance crossing equator")
        void testCalculateDistance_CrossingEquator() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0)
                    .latitude(-1.0)
                    .build();
            double expectedDistance = 2.0;

            double actualDistance = restService.calculateDistance(pointA, pointB);

            assertEquals(expectedDistance, actualDistance, EPSILON, "Distance calculation crossing the equator is incorrect");
        }
    }

    @Nested
    @DisplayName("IsCloseTo Tests")
    class IsCloseToTests {

        @Test
        @DisplayName("Points that are clearly close should return true")
        void testIsCloseTo_ClearlyClose() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0001)
                    .latitude(1.0)
                    .build();

            assertTrue(restService.isCloseTo(pointA, pointB),
                    "Points with distance less than threshold should be considered close");
        }

        @Test
        @DisplayName("Points that are clearly not close should return false")
        void testIsCloseTo_ClearlyNotClose() {
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.001)
                    .latitude(1.0)
                    .build();

            assertFalse(restService.isCloseTo(pointA, pointB),
                    "Points with distance greater than threshold should not be considered close");
        }

        @Test
        @DisplayName("Points exactly at the threshold distance should return false")
        void testIsCloseTo_ExactlyAtThreshold() {
            // Create points that are exactly CLOSE_DISTANCE (0.00015) apart
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.00015)
                    .latitude(1.0)
                    .build();

            assertFalse(restService.isCloseTo(pointA, pointB),
                    "Points exactly at threshold distance should not be considered close (< condition)");
        }

        @Test
        @DisplayName("Points just below threshold should return true")
        void testIsCloseTo_JustBelowThreshold() {
            // Create points that are just below CLOSE_DISTANCE
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.000149)
                    .build();

            assertTrue(restService.isCloseTo(pointA, pointB),
                    "Points just below threshold distance should be considered close");
        }

        @Test
        @DisplayName("Points just above threshold should return false")
        void testIsCloseTo_JustAboveThreshold() {
            // Create points that are just above CLOSE_DISTANCE
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.000151)
                    .build();

            assertFalse(restService.isCloseTo(pointA, pointB),
                    "Points just above threshold distance should not be considered close");
        }

        @Test
        @DisplayName("Identical points should return true")
        void testIsCloseTo_ZeroDistance() {
            LngLat pointA = LngLat.builder()
                    .longitude(-3.192473)
                    .latitude(55.946233)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(-3.192473)
                    .latitude(55.946233)
                    .build();

            assertTrue(restService.isCloseTo(pointA, pointB),
                    "Identical points should be considered close");
        }

        @Test
        @DisplayName("Points with distance in both directions should be evaluated correctly")
        void testIsCloseTo_DiagonalDistance() {
            // Create points with a combined distance that tests the threshold
            // Using Pythagorean theorem to calculate diagonal distance
            double offset = 0.0001; // This will create a distance of about 0.00014142
            LngLat pointA = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();
            LngLat pointB = LngLat.builder()
                    .longitude(1.0 + offset)
                    .latitude(1.0 + offset)
                    .build();

            assertTrue(restService.isCloseTo(pointA, pointB),
                    "Points with combined distance below threshold should be considered close");
        }
    }

    @Nested
    @DisplayName("nextPosition Tests")
    class NextPositionTests {

        @Test
        @DisplayName("Moving East (0°) should increase longitude only")
        void testNextPosition_East() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 0.0);

            assertEquals(1.0 + MOVE_DISTANCE, result.getLongitude(), EPSILON, "Longitude should increase by MOVE_LENGTH");
            assertEquals(1.0, result.getLatitude(), EPSILON, "Latitude should remain unchanged");
        }

        @Test
        @DisplayName("Moving North (90°) should increase latitude only")
        void testNextPosition_North() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 90.0);

            assertEquals(1.0, result.getLongitude(), EPSILON, "Longitude should remain unchanged");
            assertEquals(1.0 + MOVE_DISTANCE, result.getLatitude(), EPSILON, "Latitude should increase by MOVE_LENGTH");
        }

        @Test
        @DisplayName("Moving West (180°) should decrease longitude only")
        void testNextPosition_West() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 180.0);

            assertEquals(1.0 - MOVE_DISTANCE, result.getLongitude(), EPSILON, "Longitude should decrease by MOVE_LENGTH");
            assertEquals(1.0, result.getLatitude(), EPSILON, "Latitude should remain unchanged");
        }

        @Test
        @DisplayName("Moving South (270°) should decrease latitude only")
        void testNextPosition_South() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 270.0);

            assertEquals(1.0, result.getLongitude(), EPSILON, "Longitude should remain unchanged");
            assertEquals(1.0 - MOVE_DISTANCE, result.getLatitude(), EPSILON, "Latitude should decrease by MOVE_LENGTH");
        }

        @Test
        @DisplayName("Moving Northeast (45°) should increase both longitude and latitude")
        void testNextPosition_Northeast() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 45.0);

            double expectedChange = MOVE_DISTANCE / Math.sqrt(2);
            assertEquals(1.0 + expectedChange, result.getLongitude(), EPSILON, "Longitude should increase by MOVE_LENGTH/√2");
            assertEquals(1.0 + expectedChange, result.getLatitude(), EPSILON, "Latitude should increase by MOVE_LENGTH/√2");
        }

        @Test
        @DisplayName("Moving Northwest (135°) should decrease longitude and increase latitude")
        void testNextPosition_Northwest() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 135.0);

            double expectedChange = MOVE_DISTANCE / Math.sqrt(2);
            assertEquals(1.0 - expectedChange, result.getLongitude(), EPSILON, "Longitude should decrease by MOVE_LENGTH/√2");
            assertEquals(1.0 + expectedChange, result.getLatitude(), EPSILON, "Latitude should increase by MOVE_LENGTH/√2");
        }

        @Test
        @DisplayName("Moving Southwest (225°) should decrease both longitude and latitude")
        void testNextPosition_Southwest() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 225.0);

            double expectedChange = MOVE_DISTANCE / Math.sqrt(2);
            assertEquals(1.0 - expectedChange, result.getLongitude(), EPSILON, "Longitude should decrease by MOVE_LENGTH/√2");
            assertEquals(1.0 - expectedChange, result.getLatitude(), EPSILON, "Latitude should decrease by MOVE_LENGTH/√2");
        }

        @Test
        @DisplayName("Moving Southeast (315°) should increase longitude and decrease latitude")
        void testNextPosition_Southeast() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 315.0);

            double expectedChange = MOVE_DISTANCE / Math.sqrt(2);
            assertEquals(1.0 + expectedChange, result.getLongitude(), EPSILON, "Longitude should increase by MOVE_LENGTH/√2");
            assertEquals(1.0 - expectedChange, result.getLatitude(), EPSILON, "Latitude should decrease by MOVE_LENGTH/√2");
        }

        @Test
        @DisplayName("Moving from origin (0,0) should work correctly")
        void testNextPosition_FromOrigin() {
            LngLat origin = LngLat.builder()
                    .longitude(0.0)
                    .latitude(0.0)
                    .build();

            LngLat result = restService.nextPosition(origin, 60.0);

            double expectedLng = MOVE_DISTANCE * Math.cos(Math.toRadians(60.0));
            double expectedLat = MOVE_DISTANCE * Math.sin(Math.toRadians(60.0));

            assertEquals(expectedLng, result.getLongitude(), EPSILON, "Longitude calculation from origin is incorrect");
            assertEquals(expectedLat, result.getLatitude(), EPSILON, "Latitude calculation from origin is incorrect");
        }

        @Test
        @DisplayName("Angle close to 360° should be handled correctly")
        void testNextPosition_AngleCloseToFullCircle() {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 359.9);

            double angleRad = Math.toRadians(359.9);
            double expectedLng = 1.0 + MOVE_DISTANCE * Math.cos(angleRad);
            double expectedLat = 1.0 + MOVE_DISTANCE * Math.sin(angleRad);

            assertEquals(expectedLng, result.getLongitude(), EPSILON, "Longitude calculation near 360° is incorrect");
            assertEquals(expectedLat, result.getLatitude(), EPSILON, "Latitude calculation near 360° is incorrect");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330})
        @DisplayName("Movement distance should be consistent for all angles")
        void testNextPosition_ConsistentDistance(double angle) {
            LngLat start = LngLat.builder()
                    .longitude(1.0)
                    .latitude(1.0)
                    .build();

            LngLat result = restService.nextPosition(start, angle);
            double distance = restService.calculateDistance(start, result);

            assertEquals(MOVE_DISTANCE, distance, EPSILON, "Distance moved should equal MOVE_LENGTH regardless of angle");
        }

        @Test
        @DisplayName("Negative coordinates should be handled correctly")
        void testNextPosition_NegativeCoordinates() {
            LngLat start = LngLat.builder()
                    .longitude(-1.0)
                    .latitude(-1.0)
                    .build();

            LngLat result = restService.nextPosition(start, 45.0);

            double expectedChange = MOVE_DISTANCE / Math.sqrt(2);
            assertEquals(-1.0 + expectedChange, result.getLongitude(), EPSILON, "Longitude calculation with negative coordinates is incorrect");
            assertEquals(-1.0 + expectedChange, result.getLatitude(), EPSILON, "Latitude calculation with negative coordinates is incorrect");
        }
    }

    @Nested
    @DisplayName("IsInRegion Tests")
    class IsInRegionTests {

        private Region createRectangularRegion() {
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

        private Region createTriangularRegion() {
            return Region.builder()
                    .name("triangle")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(2.0).latitude(4.0).build(),
                            LngLat.builder().longitude(4.0).latitude(8.0).build(),
                            LngLat.builder().longitude(6.0).latitude(4.0).build(),
                            LngLat.builder().longitude(2.0).latitude(4.0).build() // Closed polygon - same as first
                    ))
                    .build();
        }

        private Region createConcaveRegion() {
            // U-shaped region
            return Region.builder()
                    .name("concave")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.949000).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.949000).build(),
                            LngLat.builder().longitude(-3.184319).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.186000).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.186000).latitude(55.948000).build(),
                            LngLat.builder().longitude(-3.190792).latitude(55.948000).build(),
                            LngLat.builder().longitude(-3.190792).latitude(55.946233).build(),
                            LngLat.builder().longitude(-3.192473).latitude(55.946233).build() // Closed polygon
                    ))
                    .build();
        }

        private Region createConcaveRegion_simple() {
            // Simple concave region
            return Region.builder()
                    .name("concave_simple")
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
        }

        private Region createVerySmallRegion() {
            double size = 0.00001;
            double baseLng = -3.188396;
            double baseLat = 55.944425;

            return Region.builder()
                    .name("very_small")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(baseLng).latitude(baseLat).build(),
                            LngLat.builder().longitude(baseLng).latitude(baseLat + size).build(),
                            LngLat.builder().longitude(baseLng + size).latitude(baseLat + size).build(),
                            LngLat.builder().longitude(baseLng + size).latitude(baseLat).build(),
                            LngLat.builder().longitude(baseLng).latitude(baseLat).build() // Closed polygon
                    ))
                    .build();
        }

        private Region createVeryLargeRegion() {
            double size = 1000.0;
            return Region.builder()
                    .name("very_large")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(-size).latitude(-size).build(),
                            LngLat.builder().longitude(-size).latitude(size).build(),
                            LngLat.builder().longitude(size).latitude(size).build(),
                            LngLat.builder().longitude(size).latitude(-size).build(),
                            LngLat.builder().longitude(-size).latitude(-size).build() // Closed polygon
                    ))
                    .build();
        }

        private Region createIrregularConvexPolygon() {
            return Region.builder()
                    .name("irregular_convex")
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
        }

        private Region createIrregularConvexPolygon_simple() {
            return Region.builder()
                    .name("irregular_convex_simple")
                    .vertices(Arrays.asList(
                            LngLat.builder().longitude(0.0).latitude(4.0).build(),
                            LngLat.builder().longitude(1.0).latitude(7.0).build(),
                            LngLat.builder().longitude(5.0).latitude(8.0).build(),
                            LngLat.builder().longitude(8.0).latitude(7.0).build(),
                            LngLat.builder().longitude(9.0).latitude(5.0).build(),
                            LngLat.builder().longitude(8.0).latitude(2.0).build(),
                            LngLat.builder().longitude(5.0).latitude(0.0).build(),
                            LngLat.builder().longitude(2.0).latitude(1.0).build(),
                            LngLat.builder().longitude(0.0).latitude(4.0).build() // Closed polygon
                    ))
                    .build();
        }

        @Test
        @DisplayName("Point clearly inside a rectangular region should return true")
        void testIsInRegion_ClearlyInside() {
            Region region = createRectangularRegion();

            // Point inside the rectangle
            LngLat point = LngLat.builder()
                    .longitude(-3.188396)
                    .latitude(55.944425)
                    .build();

            assertTrue(restService.isInRegion(point, region),
                    "Point clearly inside the region should return true");
        }

        @Test
        @DisplayName("Point clearly outside a rectangular region should return false")
        void testIsInRegion_ClearlyOutside() {
            Region region = createRectangularRegion();

            // Point outside the rectangle
            LngLat point = LngLat.builder()
                    .longitude(-3.180000)
                    .latitude(55.940000)
                    .build();

            assertFalse(restService.isInRegion(point, region),
                    "Point clearly outside the region should return false");
        }

        @Test
        @DisplayName("Point on horizontal edge should return true")
        void testIsInRegion_OnHorizontalEdge() {
            Region region = createRectangularRegion();

            // Point on top horizontal edge
            LngLat point = LngLat.builder()
                    .longitude(-3.18947)
                    .latitude(55.946233)
                    .build();

            assertTrue(restService.isInRegion(point, region),
                    "Point on horizontal edge should return true");
        }

        @Test
        @DisplayName("Point on vertical edge should return true")
        void testIsInRegion_OnVerticalEdge() {
            // Define a simple rectangular region
            Region region = createRectangularRegion();

            // Point on right vertical edge
            LngLat point = LngLat.builder()
                    .longitude(-3.192473)
                    .latitude(55.943233)
                    .build();

            assertTrue(restService.isInRegion(point, region),
                    "Point on vertical edge should return true");
        }

        @Test
        @DisplayName("Point at vertex should return true")
        void testIsInRegion_AtVertex() {
            // Define a simple rectangular region
            Region region = createRectangularRegion();

            LngLat point = LngLat.builder()
                    .longitude(-3.192473)
                    .latitude(55.946233)
                    .build();

            assertTrue(restService.isInRegion(point, region),
                    "Point at vertex should return true");
        }

        @Test
        @DisplayName("Point inside concave region should return true")
        void testIsInRegion_InsideConcave() {
            Region region = createConcaveRegion_simple();

            // Point inside the main area of the U-shape
            LngLat point = LngLat.builder()
                    .longitude(3.0)
                    .latitude(4.0)
                    .build();

            assertFalse(restService.isInRegion(point, region),
                    "Point inside the concave 'inlet' should return false");
        }

        @Test
        @DisplayName("Point in concave region's bounding box but outside polygon should return false")
        void testIsInRegion_InBoundingBoxButOutside() {
            Region region = createConcaveRegion();

            // Point in the "empty" part of the U
            LngLat point = LngLat.builder()
                    .longitude(-3.188396)
                    .latitude(55.947000)
                    .build();

            assertFalse(restService.isInRegion(point, region),
                    "Point in bounding box but outside polygon should return false");
        }

        @Test
        @DisplayName("Point collinear with edge but outside should return false")
        void testIsInRegion_CollinearWithEdgeButOutside() {
            Region region = createRectangularRegion();

            // Point collinear with bottom edge but outside
            LngLat point = LngLat.builder()
                    .longitude(-3.180000)
                    .latitude(55.942617)
                    .build();

            assertFalse(restService.isInRegion(point, region),
                    "Point collinear with edge but outside should return false");
        }


        @Test
        @DisplayName("Point sharing latitude with a vertex should be correctly evaluated")
        void testIsInRegion_SameLatitudeAsVertex() {
            Region region = createTriangularRegion();

            // Point with same latitude as top vertex but outside
            LngLat outsidePoint = LngLat.builder()
                    .longitude(10.0)
                    .latitude(8.0)
                    .build();

            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point with same latitude as vertex but outside should return false");

            // Point with same latitude as bottom-right vertex but inside
            LngLat insidePoint = LngLat.builder()
                    .longitude(4.0)
                    .latitude(4.0)
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point with same latitude as vertex but inside should return true");
        }

        @Test
        @DisplayName("Point horizontally aligned with multiple vertices should be correctly evaluated")
        void testIsInRegion_HorizontallyAlignedWithVertices() {
            Region region = createIrregularConvexPolygon_simple();

            // Point horizontally aligned with bottom vertices but inside
            LngLat insidePoint = LngLat.builder()
                    .longitude(5.0)
                    .latitude(4.0)
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point horizontally aligned with vertices but inside should return true");

            // Point horizontally aligned but outside
            LngLat outsidePoint = LngLat.builder()
                    .longitude(9.0)
                    .latitude(4.0)
                    .build();

            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point horizontally aligned with vertices but outside should return false");
        }

        @Test
        @DisplayName("Very small region should handle points correctly")
        void testIsInRegion_VerySmallRegion() {
            Region region = createVerySmallRegion();
            double size = 0.00001;
            double baseLng = -3.188396;
            double baseLat = 55.944425;

            // Point inside the small region
            LngLat insidePoint = LngLat.builder()
                    .longitude(baseLng + (size / 2))
                    .latitude(baseLat + (size / 2))
                    .build();

            // Point outside but very close to the region
            LngLat outsidePoint = LngLat.builder()
                    .longitude(baseLng - (size / 2))
                    .latitude(baseLat - (size / 2))
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point inside very small region should return true");
            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point outside very small region should return false");
        }

        @Test
        @DisplayName("Very large region should handle points correctly")
        void testIsInRegion_VeryLargeRegion() {
            Region region = createVeryLargeRegion();
            double size = 1000.0;

            // Point inside the large region
            LngLat insidePoint = LngLat.builder()
                    .longitude(500.0)
                    .latitude(500.0)
                    .build();

            // Point outside the large region
            LngLat outsidePoint = LngLat.builder()
                    .longitude(size + 10)
                    .latitude(size + 10)
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point inside very large region should return true");
            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point outside very large region should return false");
        }

        @Test
        @DisplayName("Edge case with point on a very long edge")
        void testIsInRegion_PointOnLongEdge() {
            Region region = createVeryLargeRegion();
            double size = 1000.0;

            // Point on the top edge
            LngLat pointOnEdge = LngLat.builder()
                    .longitude(0.0)
                    .latitude(size)
                    .build();

            assertTrue(restService.isInRegion(pointOnEdge, region),
                    "Point on a very long edge should return true");
        }

        @Test
        @DisplayName("Edge case with point very close to but not on edge")
        void testIsInRegion_PointVeryCloseToEdge() {
            Region region = createRectangularRegion();

            // Point extremely close to edge but not on it
            double epsilon = 1e-10;
            LngLat pointNearEdge = LngLat.builder()
                    .longitude(-3.192473 + epsilon)
                    .latitude(55.944425)
                    .build();

            assertTrue(restService.isInRegion(pointNearEdge, region),
                    "Point extremely close to edge should be correctly evaluated");
        }

        @Test
        @DisplayName("Irregular convex polygon should evaluate points correctly")
        void testIsInRegion_IrregularConvexPolygon() {
            Region region = createIrregularConvexPolygon();

            // Point inside the irregular polygon
            LngLat insidePoint = LngLat.builder()
                    .longitude(-3.188396)
                    .latitude(55.946233)
                    .build();

            // Point outside the irregular polygon
            LngLat outsidePoint = LngLat.builder()
                    .longitude(-3.195000)
                    .latitude(55.940000)
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point inside irregular convex polygon should return true");
            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point outside irregular convex polygon should return false");
        }

        @Test
        @DisplayName("Edge case with ray exactly through a vertex")
        void testIsInRegion_RayThroughVertex() {
            Region region = createTriangularRegion();

            // Point that would cast a ray through the top vertex
            LngLat testPoint = LngLat.builder()
                    .longitude(-3.195000)
                    .latitude(55.946233)
                    .build();

            assertFalse(restService.isInRegion(testPoint, region),
                    "Ray going through vertex should be handled correctly");
        }

        @Test
        @DisplayName("Empty region should be handled gracefully")
        void testIsInRegion_EmptyRegion() {
            // Define an empty region
            Region region = new Region("empty", Arrays.asList());

            LngLat point = LngLat.builder()
                    .longitude(-3.188396)
                    .latitude(55.944425)
                    .build();

            // The implementation should handle this gracefully
            assertDoesNotThrow(() -> restService.isInRegion(point, region));
        }

        @Test
        @DisplayName("Region with minimum vertices (triangle) should evaluate correctly")
        void testIsInRegion_MinimalRegion() {
            Region region = createTriangularRegion();

            // Point inside the triangle
            LngLat insidePoint = LngLat.builder()
                    .longitude(4.0)
                    .latitude(6.0)
                    .build();

            // Point outside the triangle
            LngLat outsidePoint = LngLat.builder()
                    .longitude(10.0)
                    .latitude(13.0)
                    .build();

            assertTrue(restService.isInRegion(insidePoint, region),
                    "Point inside triangular region should return true");
            assertFalse(restService.isInRegion(outsidePoint, region),
                    "Point outside triangular region should return false");
        }
    }
}
