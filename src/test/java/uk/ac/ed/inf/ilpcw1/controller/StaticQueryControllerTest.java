package uk.ac.ed.inf.ilpcw1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.ilpcw1.data.Drone;
import uk.ac.ed.inf.ilpcw1.data.DroneCapability;
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;
import uk.ac.ed.inf.ilpcw1.service.DroneQueryService;
import uk.ac.ed.inf.ilpcw1.service.RestService;
import uk.ac.ed.inf.ilpcw1.service.ValidationService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestServiceController.class)
@DisplayName("Static Query Controller Tests")
class StaticQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestService restService;

    @MockitoBean
    private ValidationService validationService;

    @MockitoBean
    private DroneQueryService droneQueryService;

    @Nested
    @DisplayName("GET /dronesWithCooling/{state} Tests")
    class DronesWithCoolingTests {

        @Test
        @DisplayName("Should return drones with cooling when state is true")
        void shouldReturnDronesWithCooling() throws Exception {
            // IDs are now Strings
            List<String> droneIds = Arrays.asList("1", "3", "5");
            when(droneQueryService.filterByCooling(true)).thenReturn(droneIds);

            mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                    .andExpect(status().isOk())
                    // Verify JSON array of strings
                    .andExpect(content().json("[\"1\", \"3\", \"5\"]"));

            verify(droneQueryService, times(1)).filterByCooling(true);
        }

        @Test
        @DisplayName("Should return drones without cooling when state is false")
        void shouldReturnDronesWithoutCooling() throws Exception {
            List<String> droneIds = Arrays.asList("2", "4", "6");
            when(droneQueryService.filterByCooling(false)).thenReturn(droneIds);

            mockMvc.perform(get("/api/v1/dronesWithCooling/false"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[\"2\", \"4\", \"6\"]"));

            verify(droneQueryService, times(1)).filterByCooling(false);
        }

        @Test
        @DisplayName("Should return empty array when no drones match")
        void shouldReturnEmptyArrayWhenNoMatch() throws Exception {
            when(droneQueryService.filterByCooling(true)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        @DisplayName("Should handle invalid boolean value gracefully")
        void shouldHandleInvalidBooleanValue() throws Exception {
            // Spring will convert non-boolean values, "invalid" will be false
            when(droneQueryService.filterByCooling(false)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/dronesWithCooling/false"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /droneDetails/{id} Tests")
    class DroneDetailsTests {

        @Test
        @DisplayName("Should return drone details when ID exists")
        void shouldReturnDroneDetailsWhenExists() throws Exception {
            // Drone ID is String "4"
            Drone mockDrone = Drone.builder()
                    .id("4")
                    .name("Test Drone")
                    .capability(DroneCapability.builder()
                            .cooling(true)
                            .heating(false)
                            .capacity(15.5)
                            .maxMoves(6000)
                            .costPerMove(0.03)
                            .costInitial(3.4)
                            .costFinal(4.5)
                            .build())
                    .build();

            when(droneQueryService.getByDroneId("4")).thenReturn(mockDrone);

            mockMvc.perform(get("/api/v1/droneDetails/4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("4"))
                    .andExpect(jsonPath("$.name").value("Test Drone"))
                    .andExpect(jsonPath("$.capability.cooling").value(true))
                    .andExpect(jsonPath("$.capability.heating").value(false))
                    .andExpect(jsonPath("$.capability.capacity").value(15.5))
                    .andExpect(jsonPath("$.capability.maxMoves").value(6000))
                    .andExpect(jsonPath("$.capability.costPerMove").value(0.03))
                    .andExpect(jsonPath("$.capability.costInitial").value(3.4))
                    .andExpect(jsonPath("$.capability.costFinal").value(4.5));

            verify(droneQueryService, times(1)).getByDroneId("4");
        }

        @Test
        @DisplayName("Should return 404 when drone ID does not exist")
        void shouldReturn404WhenDroneNotFound() throws Exception {
            when(droneQueryService.getByDroneId("999"))
                    .thenThrow(new DroneNotFoundException("Drone with id 999 not found"));

            mockMvc.perform(get("/api/v1/droneDetails/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Drone with id 999 not found"));

            verify(droneQueryService, times(1)).getByDroneId("999");
        }

        @Test
        @DisplayName("Should return drone with minimal capability data")
        void shouldReturnDroneWithMinimalData() throws Exception {
            Drone minimalDrone = Drone.builder()
                    .id("1")
                    .name("Minimal Drone")
                    .capability(DroneCapability.builder().build())
                    .build();

            when(droneQueryService.getByDroneId("1")).thenReturn(minimalDrone);

            mockMvc.perform(get("/api/v1/droneDetails/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("1"))
                    .andExpect(jsonPath("$.name").value("Minimal Drone"));
        }
    }
}