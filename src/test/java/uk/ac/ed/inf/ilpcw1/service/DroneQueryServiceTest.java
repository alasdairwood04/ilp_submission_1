package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;
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


    // drone list from the ILP REST API
    private List<Drone> createFullDroneList() {
        return List.of(
                Drone.builder()
                        .id(1)
                        .name("Drone 1")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(true)
                                .capacity(4.0)
                                .maxMoves(2000)
                                .costPerMove(0.01)
                                .costInitial(4.3)
                                .costFinal(6.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(2)
                        .name("Drone 2")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(true)
                                .capacity(8.0)
                                .maxMoves(1000)
                                .costPerMove(0.03)
                                .costInitial(2.6)
                                .costFinal(5.4)
                                .build())
                        .build(),
                Drone.builder()
                        .id(3)
                        .name("Drone 3")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(false)
                                .capacity(20.0)
                                .maxMoves(4000)
                                .costPerMove(0.05)
                                .costInitial(9.5)
                                .costFinal(11.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(4)
                        .name("Drone 4")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(true)
                                .capacity(8.0)
                                .maxMoves(1000)
                                .costPerMove(0.02)
                                .costInitial(1.4)
                                .costFinal(2.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(5)
                        .name("Drone 5")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(true)
                                .capacity(12.0)
                                .maxMoves(1500)
                                .costPerMove(0.04)
                                .costInitial(1.8)
                                .costFinal(3.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(6)
                        .name("Drone 6")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(true)
                                .capacity(4.0)
                                .maxMoves(2000)
                                .costPerMove(0.03)
                                .costInitial(3.0)
                                .costFinal(4.0)
                                .build())
                        .build(),
                Drone.builder()
                        .id(7)
                        .name("Drone 7")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(true)
                                .capacity(8.0)
                                .maxMoves(1000)
                                .costPerMove(0.015)
                                .costInitial(1.4)
                                .costFinal(2.2)
                                .build())
                        .build(),
                Drone.builder()
                        .id(8)
                        .name("Drone 8")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(false)
                                .capacity(20.0)
                                .maxMoves(4000)
                                .costPerMove(0.04)
                                .costInitial(5.4)
                                .costFinal(12.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(9)
                        .name("Drone 9")
                        .capability(DroneCapability.builder()
                                .cooling(true)
                                .heating(true)
                                .capacity(8.0)
                                .maxMoves(1000)
                                .costPerMove(0.06)
                                .costInitial(2.4)
                                .costFinal(1.5)
                                .build())
                        .build(),
                Drone.builder()
                        .id(10)
                        .name("Drone 10")
                        .capability(DroneCapability.builder()
                                .cooling(false)
                                .heating(false)
                                .capacity(12.0)
                                .maxMoves(1500)
                                .costPerMove(0.07)
                                .costInitial(1.4)
                                .costFinal(3.5)
                                .build())
                        .build()
        );
    }

    private List<DroneServicePointRequest> createFullAvailabilityList() {
        return List.of(
                // ----- Service Point 1 -----
                DroneServicePointRequest.builder()
                        .servicePointId(1)
                        .drones(List.of(
                                DronesAtServicePoint.builder()
                                        .id(1)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(2)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(3)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(4)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(5)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build()
                                        ))
                                        .build()
                        ))
                        .build(),

                // ----- Service Point 2 -----
                DroneServicePointRequest.builder()
                        .servicePointId(2)
                        .drones(List.of(
                                DronesAtServicePoint.builder()
                                        .id(6)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(7)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(8)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(9)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id(10)
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build()
                        ))
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

    @Nested
    @DisplayName("queryAvailableDrones Tests")
    class QueryAvailableDronesTests {
        private List<Drone> allDrones;
        private List<DroneServicePointRequest> allAvailability;

        @BeforeEach
        void setUp() {
            allDrones = createFullDroneList();
            allAvailability = createFullAvailabilityList();
            when(ilpServiceClientMock.getAllDrones()).thenReturn(allDrones);
            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(allAvailability);
        }

        @Test
        @DisplayName("Scenario 1: Should return drones matching high capacity (>=15)")
        void shouldReturnDronesMatchingHighCapacity() {
            // ARRANGE
            // Drones with cap >= 15: 3 (20), 8 (20)
            // Availability for Mon @ 10:00:
            // - Drone 3: YES (Mon am)
            // - Drone 8: NO


            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2024, 1, 1)) // Monday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(15.0)
                                    .cooling(false)
                                    .heating(false)
                                    .build())
                            .build(
                            ));


            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(1, result.size());
            assertTrue(result.contains(3));
        }

        @Test
        @DisplayName("Scenario 2: Should return drones matching cooling on Wednesday")
        void shouldReturnDronesMatchingCooling() {
            // ARRANGE
            // Drones with cooling: 1, 5, 8, 9
            // Availability for Wed @ 10:00:
            // - Drone 1: YES (Wed all)
            // - Drone 5: NO
            // - Drone 8: NO
            // - Drone 9: YES (Wed all)
            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2025, 12, 17)) // Wednesday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(4.0)
                                    .cooling(true)
                                    .heating(false)
                                    .build())
                            .build(
                            ));

            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(1, 9)));
        }

        @Test
        @DisplayName("Scenario 3: Should return drones matching heating on Tuesday PM")
        void shouldReturnDronesMatchingHeating() {
            // ARRANGE
            // Drones with heating: 1, 2, 4, 5, 6, 7, 9
            // Availability for Tue @ 14:00 (2 PM):
            // - Drone 1: NO
            // - Drone 2: YES (Tue all)
            // - Drone 4: NO (Tue am)
            // - Drone 5: YES (Tue all)
            // - Drone 6: NO
            // - Drone 7: YES (Tue all)
            // - Drone 9: NO (Tue am)

            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2025, 12, 9)) // Tuesday
                            .time(LocalTime.of(14, 0))
                            .requirements(Requirements.builder()
                                    .capacity(4.0)
                                    .cooling(false)
                                    .heating(true)
                                    .build())
                            .build(
                            ));

            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(3, result.size());
            assertTrue(result.containsAll(List.of(2, 5, 7)));
        }

        @Test
        @DisplayName("Scenario 4: Should return drones matching capacity (>=10) AND cooling AND time (Fri PM)")
        void shouldReturnDronesMatchingCapacityAndCoolingAndTime() {
            // ARRANGE
            // Drones with cap >= 10 AND cooling: 5 (12, C,H), 8 (20, C)
            // Availability for Fri @ 14:00 (2 PM):
            // - Drone 5: YES (Fri all)
            // - Drone 8: YES (Fri all)

            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2025, 12, 12)) // Friday
                            .time(LocalTime.of(14, 0))
                            .requirements(Requirements.builder()
                                    .capacity(10.0)
                                    .cooling(true)
                                    .heating(false)
                                    .build())
                            .build(
                            ));

            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(5, 8)));
        }

        @Test
        @DisplayName("Scenario 5: Should return drones matching aggregated capacity (>=15)")
        void shouldReturnDronesMatchingAggregatedCapacity() {
            // ARRANGE
            // Dispatches require 5 + 5 + 5 = 15 capacity.
            // All are on Mon @ 10:00

            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2024, 1, 1)) // Monday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(5.0)
                                    .cooling(false)
                                    .heating(false)
                                    .build())
                            .build(
                            ),
                    MedDispatchRec.builder()
                            .id(102)
                            .date(LocalDate.of(2024, 1, 1)) // Monday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(5.0)
                                    .cooling(false)
                                    .heating(false)
                                    .build())
                            .build(
                            ),
                    MedDispatchRec.builder()
                            .id(103)
                            .date(LocalDate.of(2024, 1, 1)) // Monday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(5.0)
                                    .cooling(false)
                                    .heating(false)
                                    .build())
                            .build(
                            )
            );

            // This is the same as Scenario 1: Drones 3, 8 meet capacity.
            // Availability for Mon @ 10:00:
            // - Drone 3: YES (Mon am)
            // - Drone 8: NO

            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(1, result.size());
            assertTrue(result.contains(3));
        }

        @Test
        @DisplayName("Scenario: where the dispatch dont require cooling or heating, but the only available drones have those capabilities")
        void shouldReturnDronesWhenNoCoolingOrHeatingRequiredButDronesHaveThoseCapabilities() {
            // ARRANGE
            // Dispatch requires cap 5.0, cooling=false, heating=false on Mon @ 10:00
            List<MedDispatchRec> dispatches = List.of(
                    MedDispatchRec.builder()
                            .id(101)
                            .date(LocalDate.of(2024, 1, 1)) // Monday
                            .time(LocalTime.of(10, 0))
                            .requirements(Requirements.builder()
                                    .capacity(5.0)
                                    .cooling(false)
                                    .heating(false)
                                    .build())
                            .build()
            );

            // Drones with cap >= 5.0: 1, 2, 3, 5, 6, 7, 8, 9, 10
            // Availability for Mon @ 10:00:
            // - Drone 1: YES (Mon am)
            // - Drone 2: YES (Mon all)
            // - Drone 3: YES (Mon am)
            // - Drone 5: NO
            // - Drone 6: YES (Mon am)
            // - Drone 7: YES (Mon all)
            // - Drone 8: NO
            // - Drone 9: YES (Mon all)
            // - Drone 10: NO
            // So expected: 1, 2, 3, 6, 7, 9

            // ACT
            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);

            // ASSERT
            assertEquals(6, result.size());
            assertTrue(result.containsAll(List.of(1, 2, 3, 6, 7, 9)));
        }
//
//        @Test
//        @DisplayName("Scenario 6: Should match aggregated requirements AND multiple times")
//        void shouldMatchAggregatedRequirementsAndMultipleTimes() {
//            // ARRANGE
//            // Dispatch 1: Mon @ 14:00, cap 2.0, heating=true
//            // Dispatch 2: Tue @ 10:00, cap 2.0, heating=false
//            // AGGREGATE: cap >= 4.0, heating=true
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(14, 0), 2.0, false, true),
//                    createDispatch(102, TUESDAY, LocalTime.of(10, 0), 2.0, false, false)
//            );
//
//            // Drones with cap >= 4.0 AND heating=true: 1, 2, 4, 5, 6, 7, 9
//            // Check Availability for BOTH times:
//            // - Drone 1: Mon 14:00 (YES), Tue 10:00 (NO) -> FAIL
//            // - Drone 2: Mon 14:00 (YES), Tue 10:00 (YES) -> PASS
//            // - Drone 4: Mon 14:00 (NO),  Tue 10:00 (YES) -> FAIL
//            // - Drone 5: Mon 14:00 (NO),  Tue 10:00 (YES) -> FAIL
//            // - Drone 6: Mon 14:00 (YES), Tue 10:00 (NO) -> FAIL
//            // - Drone 7: Mon 14:00 (YES), Tue 10:00 (YES) -> PASS
//            // - Drone 9: Mon 14:00 (YES), Tue 10:00 (YES) -> PASS
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertEquals(3, result.size());
//            assertTrue(result.containsAll(List.of(2, 7, 9)));
//        }
//
//        // ----- Edge Case Scenarios -----
//
//        @Test
//        @DisplayName("EDGE CASE: Should return empty for conflicting cooling/heating requirements")
//        void shouldReturnEmptyForConflictingCoolingAndHeating() {
//            // ARRANGE
//            // This batch is impossible: one item needs cooling, one needs heating.
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, true, false),
//                    createDispatch(102, MONDAY, LocalTime.of(10, 0), 1.0, false, true)
//            );
//
//            // Note: Drones 1, 5, 9 have BOTH capabilities, but the "either/or" rule
//            // means no single drone can be chosen for this *batch*.
//            // This test assumes the `if (coolingRequired && heatingRequired)` fix.
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertTrue(result.isEmpty(), "A batch with cooling AND heating requirements should return no drones.");
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should handle dispatch with null requirements")
//        void shouldHandleNullRequirementsInDispatch() {
//            // ARRANGE
//            // A null requirements object should be treated as cap=0, cool=false, heat=false
//            MedDispatchRec dispatch1 = createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, false, false);
//            MedDispatchRec dispatch2 = MedDispatchRec.builder()
//                    .id(102)
//                    .date(MONDAY)
//                    .time(LocalTime.of(10, 0))
//                    .requirements(null) // Null requirements
//                    .build();
//            List<MedDispatchRec> dispatches = List.of(dispatch1, dispatch2);
//
//            // AGGREGATE: cap >= 1.0, cooling=false, heating=false
//            // Drones with cap >= 1.0, no C/H: 3, 10
//            // Drones that also have C or H are also fine: 1, 2, 4, 5, 6, 7, 8, 9
//            // Basically, all drones *except* one with cap < 1.0
//            // Check Availability (Mon @ 10:00):
//            // - Drones 1, 3, 4, 6, 9 are available. (5 drones)
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertEquals(5, result.size());
//            assertTrue(result.containsAll(List.of(1, 3, 4, 6, 9)));
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should treat null cooling/heating capability as false")
//        void shouldTreatNullCoolingAndHeatingAsFalse() {
//            // ARRANGE
//            // A dispatch that requires heating
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, false, true)
//            );
//
//            // A custom drone with null cooling/heating, but available
//            Drone nullDrone = Drone.builder().id(101).name("Null Cap Drone")
//                    .capability(DroneCapability.builder().capacity(10.0).cooling(null).heating(null).build())
//                    .build();
//
//            // Mock data to only include this drone
//            when(ilpServiceClientMock.getAllDrones()).thenReturn(List.of(nullDrone));
//            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(List.of(
//                    DroneServicePointRequest.builder()
//                            .servicePointId(1)
//                            .drones(List.of(DronesAtServicePoint.builder()
//                                    .id(101)
//                                    .available(List.of(DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.MIN).until(LocalTime.MAX).build()))
//                                    .build()))
//                            .build()
//            ));
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            // The drone is treated as heating=false, so it does not match.
//            assertTrue(result.isEmpty());
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should gracefully skip drone with null capability")
//        void shouldGracefullySkipDroneWithNullCapability() {
//            // ARRANGE
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, false, false)
//            );
//            // Custom drone with null capability
//            Drone nullCapDrone = Drone.builder().id(100).name("No Cap Drone").capability(null).build();
//
//            when(ilpServiceClientMock.getAllDrones()).thenReturn(List.of(nullCapDrone, drone3)); // drone 3 should still be found
//            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(allAvailability);
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            // The null-cap drone is skipped, but Drone 3 is found.
//            assertEquals(1, result.size());
//            assertTrue(result.contains(3));
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should return empty if no drones in system")
//        void shouldReturnEmptyWhenNoDronesInSystem() {
//            // ARRANGE
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, false, false)
//            );
//            when(ilpServiceClientMock.getAllDrones()).thenReturn(List.of()); // No drones returned
//            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(allAvailability);
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertTrue(result.isEmpty());
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should return empty if no availability in system")
//        void shouldReturnEmptyWhenNoAvailabilityInSystem() {
//            // ARRANGE
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(10, 0), 1.0, false, false)
//            );
//            when(ilpServiceClientMock.getAllDrones()).thenReturn(allDrones);
//            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(List.of()); // No availability
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertTrue(result.isEmpty());
//        }
//
//        @Test
//        @DisplayName("EDGE CASE: Should fail for dispatch just outside time window")
//        void shouldFailForDispatchOutsideTimeWindow() {
//            // ARRANGE
//            // Drone 3 is available Mon 00:00 to 11:59:59
//            // Dispatch is at 12:00:00 (one second too late)
//            List<MedDispatchRec> dispatches = List.of(
//                    createDispatch(101, MONDAY, LocalTime.of(12, 0, 0), 1.0, false, false)
//            );
//
//            when(ilpServiceClientMock.getAllDrones()).thenReturn(List.of(drone3));
//            when(ilpServiceClientMock.getDroneAvailability()).thenReturn(allAvailability);
//
//            // ACT
//            List<Integer> result = droneQueryService.queryAvailableDrones(dispatches);
//
//            // ASSERT
//            assertTrue(result.isEmpty());
//        }
    }
}