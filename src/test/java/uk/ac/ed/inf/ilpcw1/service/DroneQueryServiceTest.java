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
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;

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
}