package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.inf.ilpcw1.data.Drone;
import uk.ac.ed.inf.ilpcw1.data.DroneCapability;
import uk.ac.ed.inf.ilpcw1.data.DroneQueryRequest;
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DroneQueryService
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("DroneQueryService Unit Tests")
public class DroneQueryServiceTest {

    @Mock
    private ILPServiceClient ilpServiceClientMock;

    private DroneQueryService droneQueryService;


    @BeforeEach
    public void setUp() {
        droneQueryService = new DroneQueryService(ilpServiceClientMock);
    }

    private List<Drone> createMockDrones() {
        return Arrays.asList(
                Drone.builder()
                        .id(1)
                        .name("Drone 1")
                        .capability(DroneCapability.builder()
                                .cooling(true) // Drone with cooling capability
                                .build())
                        .build(),
                Drone.builder()
                        .id(2)
                        .name("Drone 2")
                        .capability(DroneCapability.builder()
                                .cooling(false) // Drone without cooling capability
                                .build())
                        .build(),
                Drone.builder()
                        .id(3)
                        .name("Drone 3")
                        .capability(DroneCapability.builder()
                                .cooling(null) // Drone with null cooling capability (will be treated as false)
                                .build())
                        .build(),
                Drone.builder()
                        .id(4)
                        .name("Drone 4")
                        .capability(null) // Drone with no capability (will be ignored)
                        .build(),
                Drone.builder()
                        .id(5)
                        .name("Drone 5")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(false)
                                .capacity(12.0)
                                .maxMoves(1500)
                                .costPerMove(0.07)
                                .costInitial(1.4)
                                .costFinal(3.5)
                                .build()
                        )
                        .build()
        );
    }


    @Nested
    @DisplayName("filterByCooling Tests")
    class FilterByCoolingTests {

        @Test
        @DisplayName("Should return drones with cooling capability")
        public void testFilterByCooling_True() {

            // mock ILPServiceClient to return predefined drones
            when(ilpServiceClientMock.getAllDrones()).thenReturn(createMockDrones());

            // call the method under test
            List<Integer> result = droneQueryService.filterByCooling(true);

            assertEquals(1, result.size());
            assertTrue(result.contains(1));
        }

        @Test
        @DisplayName("Should return drones without cooling capability")
        public void testFilterByCooling_False() {
            // mock ILPServiceClient to return predefined drones
            when(ilpServiceClientMock.getAllDrones()).thenReturn(createMockDrones());

            // call the method under test
            List<Integer> result = droneQueryService.filterByCooling(false);

            assertEquals(2, result.size());
            assertTrue(result.contains(2)); // Drone with cooling=false
            assertTrue(result.contains(3)); // Drone with cooling=null treated as false
        }

        @Test
        @DisplayName("Should return empty list when no drones match cooling criteria")
        public void testFilterByCooling_NoMatches() {
            // mock ILPServiceClient to return drones all with cooling
            when(ilpServiceClientMock.getAllDrones()).thenReturn(Arrays.asList(
                    Drone.builder().id(1).capability(DroneCapability.builder().cooling(true).build()).build(),
                    Drone.builder().id(2).capability(DroneCapability.builder().cooling(true).build()).build()
            ));

            // call the method under test
            List<Integer> result = droneQueryService.filterByCooling(false);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when drone list is empty")
        void shouldReturnEmptyListWhenNoDrones() {
            when(ilpServiceClientMock.getAllDrones()).thenReturn(List.of());

            List<Integer> result = droneQueryService.filterByCooling(true);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getByDroneId Tests")
    class GetByDroneIdTests {

        @Test
        @DisplayName("Should return drone details for valid ID")
        public void testGetByDroneId_ValidId() {
            // mock ILPServiceClient to return predefined drones
            when(ilpServiceClientMock.getAllDrones()).thenReturn(createMockDrones());

            // call the method under test
            Drone result = droneQueryService.getByDroneId(5);

            assertNotNull(result);
            assertEquals(5, result.getId());
            assertEquals("Drone 5", result.getName());
            assertFalse(result.getCapability().getCooling());
            assertFalse(result.getCapability().getHeating());
            assertEquals(12.0, result.getCapability().getCapacity());
            assertEquals(1500, result.getCapability().getMaxMoves());
            assertEquals(0.07, result.getCapability().getCostPerMove());
            assertEquals(1.4, result.getCapability().getCostInitial());
            assertEquals(3.5, result.getCapability().getCostFinal());
        }

        @Test
        @DisplayName("Should throw DroneNotFoundException for invalid ID")
        public void testGetByDroneId_InvalidId() {
            // mock ILPServiceClient to return predefined drones
            when(ilpServiceClientMock.getAllDrones()).thenReturn(createMockDrones());

            // call the method under test and expect exception
            assertThrows(DroneNotFoundException.class, () -> {
                droneQueryService.getByDroneId(999); // non-existent ID
            });
        }

        @Test
        @DisplayName("Should return first drone when multiple drones have same ID")
        void shouldReturnFirstDroneWithMatchingId() {
            // This is an edge case - shouldn't happen in practice
            List<Drone> duplicateDrones = Arrays.asList(
                    Drone.builder().id(1).name("First").build(),
                    Drone.builder().id(1).name("Second").build()
            );
            when(ilpServiceClientMock.getAllDrones()).thenReturn(duplicateDrones);

            Drone result = droneQueryService.getByDroneId(1);

            assertEquals("First", result.getName());
        }
    }


    private List<Drone> createMockDronesDynamicQueries() {
        return Arrays.asList(
                Drone.builder()
                        .id(1)
                        .name("Drone 1")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(false)
                                .capacity(15.5)
                                .maxMoves(6000)
                                .costPerMove(0.03)
                                .costInitial(3.4)
                                .costFinal(4.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(2)
                        .name("Drone 2")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(true)
                                .capacity(8.0)
                                .maxMoves(5000)
                                .costPerMove(0.02)
                                .costInitial(2.5)
                                .costFinal(3.0)
                                .build())
                        .build(),
                Drone.builder()
                        .id(3)
                        .name("Drone 3")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(false)
                                .capacity(20.0)
                                .maxMoves(7000)
                                .costPerMove(0.04)
                                .costInitial(4.0)
                                .costFinal(5.0)
                                .build())
                        .build(),
                Drone.builder()
                        .id(4)
                        .name("Drone 4")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(false)
                                .capacity(8.0)
                                .maxMoves(4000)
                                .costPerMove(0.01)
                                .costInitial(1.5)
                                .costFinal(2.0)
                                .build())
                        .build()
        );
    }

    @Nested
    @DisplayName("queryAsPath Tests")
    class QueryByAttributeTests {
        @Test
        @DisplayName("Should query by capacity")
        void shouldQueryByCapacity() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<Integer> result = droneQueryService.queryByAttribute("capacity", "8.0");

            assertEquals(2, result.size());
            assertTrue(result.contains(2));
            assertTrue(result.contains(4));
        }

        @Test
        @DisplayName("Should query by cooling")
        void shouldQueryByCooling() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<Integer> result = droneQueryService.queryByAttribute("cooling", "true");

            assertEquals(2, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(3));
        }

        @Test
        @DisplayName("Should query by heating")
        void shouldQueryByHeating() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<Integer> result = droneQueryService.queryByAttribute("heating", "true");

            assertEquals(1, result.size());
            assertTrue(result.contains(2));
        }

        @Test
        @DisplayName("Should query by maxMoves")
        void shouldQueryByMaxMoves() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<Integer> result = droneQueryService.queryByAttribute("maxMoves", "6000");

            assertEquals(1, result.size());
            assertTrue(result.contains(1));
        }

        @Test
        @DisplayName("Should return empty list when no match")
        void shouldReturnEmptyWhenNoMatch() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<Integer> result = droneQueryService.queryByAttribute("capacity", "100.0");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for unknown attribute")
        void shouldThrowExceptionForUnknownAttribute() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            assertThrows(InvalidRequestException.class, () ->
                    droneQueryService.queryByAttribute("unknownAttr", "value")
            );
        }
    }


    @Nested
    @DisplayName("query Tests")
    class QueryDronesTests {
        @Test
        @DisplayName("Should query with single attribute")
        void shouldQueryWithSingleAttribute() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("=")
                            .value("8.0")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(2, result.size());
            assertTrue(result.contains(2));
            assertTrue(result.contains(4));
        }

        @Test
        @DisplayName("Should query with multiple attributes (AND logic)")
        void shouldQueryWithMultipleAttributes() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = Arrays.asList(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("<")
                            .value("10.0")
                            .build(),
                    DroneQueryRequest.builder()
                            .attribute("cooling")
                            .operator("=")
                            .value("false")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(2, result.size());
            assertTrue(result.contains(2));
            assertTrue(result.contains(4));
        }

        @Test
        @DisplayName("Should handle less than operator")
        void shouldHandleLessThanOperator() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("<")
                            .value("10.0")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(2, result.size());
            assertTrue(result.contains(2));
            assertTrue(result.contains(4));
        }

        @Test
        @DisplayName("Should handle greater than operator")
        void shouldHandleGreaterThanOperator() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator(">")
                            .value("15.0")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(2, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(3));
        }

        @Test
        @DisplayName("Should handle not equal operator")
        void shouldHandleNotEqualOperator() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("!=")
                            .value("8.0")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(2, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(3));
        }

        @Test
        @DisplayName("Should return empty list when no drones match all criteria")
        void shouldReturnEmptyWhenNoMatch() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = Arrays.asList(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator(">")
                            .value("100.0")
                            .build(),
                    DroneQueryRequest.builder()
                            .attribute("cooling")
                            .operator("=")
                            .value("true")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list for empty query list")
        void shouldReturnEmptyForEmptyQueryList() {
            List<Integer> result = droneQueryService.queryDrones(List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list for null query list")
        void shouldReturnEmptyForNullQueryList() {
            List<Integer> result = droneQueryService.queryDrones(null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for invalid operator on boolean")
        void shouldThrowExceptionForInvalidBooleanOperator() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("cooling")
                            .operator("<")
                            .value("true")
                            .build()
            );

            assertThrows(InvalidRequestException.class, () ->
                    droneQueryService.queryDrones(queries)
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid numeric value")
        void shouldThrowExceptionForInvalidNumericValue() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = List.of(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("=")
                            .value("not-a-number")
                            .build()
            );

            assertThrows(InvalidRequestException.class, () ->
                    droneQueryService.queryDrones(queries)
            );
        }

        @Test
        @DisplayName("Should handle complex query with three attributes")
        void shouldHandleComplexQuery() {
            List<Drone> mockDrones = createMockDronesDynamicQueries();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(mockDrones);

            List<DroneQueryRequest> queries = Arrays.asList(
                    DroneQueryRequest.builder()
                            .attribute("capacity")
                            .operator("<")
                            .value("10.0")
                            .build(),
                    DroneQueryRequest.builder()
                            .attribute("cooling")
                            .operator("=")
                            .value("false")
                            .build(),
                    DroneQueryRequest.builder()
                            .attribute("heating")
                            .operator("=")
                            .value("false")
                            .build()
            );

            List<Integer> result = droneQueryService.queryDrones(queries);

            assertEquals(1, result.size());
            assertTrue(result.contains(4));
        }
    }

}