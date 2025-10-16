package uk.ac.ed.inf.ilpcw1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.service.RestService;
import uk.ac.ed.inf.ilpcw1.service.ValidationService;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(RestServiceController.class)
public class RestServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestService restService;

    @MockitoBean
    private ValidationService validationService;


//    @Test
//    public void testHealthCheck() throws Exception {
//        mockMvc.perform(get("/actuator/health"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("UP"));
//    }

    @Test
    public void testGetStudentId() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2524182"));
    }


    // ========= distanceTo tests ==============
    @Test
    public void testCalculateDistance_ValidRequest() throws Exception {
        DistanceRequest request = DistanceRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.192473)
                        .latitude(55.946233)
                        .build())
                .position2(LngLat.builder()
                        .longitude(-3.192473)
                        .latitude(55.942617)
                        .build())
                .build();
        when(restService.calculateDistance(any(LngLat.class),  any(LngLat.class))).thenReturn(0.003616000000000001);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("0.003616000000000001"));
        verify(validationService, times(1)).validateDistanceRequest(any(DistanceRequest.class));
        verify(restService, times(1)).calculateDistance(any(LngLat.class), any(LngLat.class));

    }

    @Test
    public void testCalculateDistance_zero_distance_ValidRequest() throws Exception {
        DistanceRequest request = DistanceRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        when(restService.calculateDistance(any(LngLat.class), any(LngLat.class))).thenReturn(0.0);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
        verify(validationService, times(1)).validateDistanceRequest(any(DistanceRequest.class));
        verify(restService, times(1)).calculateDistance(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testCalculateDistance_InvalidRequest_MissingPosition1() throws Exception {
        DistanceRequest request = DistanceRequest.builder()
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'position1' is required"))
                .when(validationService).validateDistanceRequest(any(DistanceRequest.class));
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'position1' is required"));
        verify(validationService, times(1)).validateDistanceRequest(any(DistanceRequest.class));
        verify(restService, times(0)).calculateDistance(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testCalculateDistance_InvalidRequest_MissingPosition2() throws Exception {
        DistanceRequest request = DistanceRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'position2' is required"))
                .when(validationService).validateDistanceRequest(any(DistanceRequest.class));
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'position2' is required"));
        verify(validationService, times(1)).validateDistanceRequest(any(DistanceRequest.class));
        verify(restService, times(0)).calculateDistance(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testCalculateDistance_InvalidRequest_NullBody() throws Exception {
        doThrow(new InvalidRequestException("Request body cannot be null"))
                .when(validationService).validateDistanceRequest(any(DistanceRequest.class));
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON"))
                .andExpect(jsonPath("$.message").value("The JSON request body is malformed or invalid."));
        }

    // ========= isCloseTo tests ==============

    @Test
    public void testIsCloseTo_ValidRequest_True() throws Exception {
        CloseToRequest request = CloseToRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944600)
                        .build())
                .build();
        when(restService.isCloseTo(any(LngLat.class), any(LngLat.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(validationService, times(1)).validateCloseTo(any(CloseToRequest.class));
        verify(restService, times(1)).isCloseTo(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testIsCloseTo_ValidRequest_False() throws Exception {
        CloseToRequest request = CloseToRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.946000)
                        .build())
                .build();
        when(restService.isCloseTo(any(LngLat.class), any(LngLat.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        verify(validationService, times(1)).validateCloseTo(any(CloseToRequest.class));
        verify(restService, times(1)).isCloseTo(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testIsCloseTo_InvalidRequest_MissingPosition1() throws Exception {
        CloseToRequest request = CloseToRequest.builder()
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'position1' is required"))
                .when(validationService).validateCloseTo(any(CloseToRequest.class));
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'position1' is required"));
        verify(validationService, times(1)).validateCloseTo(any(CloseToRequest.class));
        verify(restService, times(0)).isCloseTo(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testIsCloseTo_InvalidRequest_MissingPosition2() throws Exception {
        CloseToRequest request = CloseToRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'position2' is required"))
                .when(validationService).validateCloseTo(any(CloseToRequest.class));
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'position2' is required"));
        verify(validationService, times(1)).validateCloseTo(any(CloseToRequest.class));
        verify(restService, times(0)).isCloseTo(any(LngLat.class), any(LngLat.class));
    }

    @Test
    public void testIsCloseTo_InvalidRequest_NullBody() throws Exception {
        doThrow(new InvalidRequestException("Request body cannot be null"))
                .when(validationService).validateCloseTo(any(CloseToRequest.class));
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON"))
                .andExpect(jsonPath("$.message").value("The JSON request body is malformed or invalid."));
    }

    @Test
    public void testIsCloseTo_InvalidCoordinates() throws Exception {
        CloseToRequest request = CloseToRequest.builder()
                .position1(LngLat.builder()
                        .longitude(-200.0) // Invalid longitude
                        .latitude(55.944494)
                        .build())
                .position2(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(95.0) // Invalid latitude
                        .build())
                .build();
        doThrow(new InvalidCoordinateException("Invalid longitude in position1"))
                .when(validationService).validateCloseTo(any(CloseToRequest.class));
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid longitude in position1"));
        verify(validationService, times(1)).validateCloseTo(any(CloseToRequest.class));
        verify(restService, times(0)).isCloseTo(any(LngLat.class), any(LngLat.class));
    }

    // ======== nextPosition tests ==============
    @Test
    public void testNextPosition_ValidRequest() throws Exception {
        NextPositionRequest request = NextPositionRequest.builder()
                .start(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .angle(90.0)
                .build();
        LngLat expectedResponse = LngLat.builder()
                .longitude(-3.186874)
                .latitude(55.944644)
                .build();
        when(restService.nextPosition(any(LngLat.class), any(Double.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
        verify(validationService, times(1)).validateNextPositionRequest(any(NextPositionRequest.class));
        verify(restService, times(1)).nextPosition(any(LngLat.class), any(Double.class));
    }

    @Test
    public void testNextPosition_InvalidRequest_MissingStart() throws Exception {
        NextPositionRequest request = NextPositionRequest.builder()
                .angle(90.0)
                .build();
        doThrow(new InvalidRequestException("'start' is required"))
                .when(validationService).validateNextPositionRequest(any(NextPositionRequest.class));
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'start' is required"));
        verify(validationService, times(1)).validateNextPositionRequest(any(NextPositionRequest.class));
        verify(restService, times(0)).nextPosition(any(LngLat.class), any(Double.class));
    }

    @Test
    public void testNextPosition_InvalidRequest_MissingAngle() throws Exception {
        NextPositionRequest request = NextPositionRequest.builder()
                .start(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'angle' is required"))
                .when(validationService).validateNextPositionRequest(any(NextPositionRequest.class));
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'angle' is required"));
        verify(validationService, times(1)).validateNextPositionRequest(any(NextPositionRequest.class));
        verify(restService, times(0)).nextPosition(any(LngLat.class), any(Double.class));
    }

    @Test
    public void testNextPosition_InvalidRequest_NullBody() throws Exception {
        doThrow(new InvalidRequestException("Request body cannot be null"))
                .when(validationService).validateNextPositionRequest(any(NextPositionRequest.class));
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON"))
                .andExpect(jsonPath("$.message").value("The JSON request body is malformed or invalid."));
    }

    @Test
    public void testNextPosition_InvalidCoordinates() throws Exception {
        NextPositionRequest request = NextPositionRequest.builder()
                .start(LngLat.builder()
                        .longitude(-200.0) // Invalid longitude
                        .latitude(55.944494)
                        .build())
                .angle(90.0)
                .build();
        doThrow(new InvalidCoordinateException("Invalid longitude in start"))
                .when(validationService).validateNextPositionRequest(any(NextPositionRequest.class));
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid longitude in start"));
        verify(validationService, times(1)).validateNextPositionRequest(any(NextPositionRequest.class));
        verify(restService, times(0)).nextPosition(any(LngLat.class), any(Double.class));
    }

    // ======== isInRegion tests ==============

    @Test
    public void testIsInRegion_ValidRequest_Inside() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build()
                        ))
                        .build())
                .build();
        when(restService.isInRegion(any(LngLat.class), any(Region.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(1)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_ValidRequest_Outside() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.188874)
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build()
                        ))
                        .build())
                .build();
        when(restService.isInRegion(any(LngLat.class), any(Region.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(1)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_InvalidRequest_MissingPosition() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build()
                        ))
                        .build())
                .build();
        doThrow(new InvalidRequestException("'position' is required"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'position' is required"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_InvalidRequest_MissingRegion() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .build();
        doThrow(new InvalidRequestException("'region' is required"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'region' is required"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_InvalidRequest_NullBody() throws Exception {
        doThrow(new InvalidRequestException("Request body cannot be null"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON"))
                .andExpect(jsonPath("$.message").value("The JSON request body is malformed or invalid."));
    }

    @Test
    public void testIsInRegion_InvalidCoordinates() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-200.0) // Invalid longitude
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build()
                        ))
                        .build())
                .build();
        doThrow(new InvalidCoordinateException("Invalid longitude in position"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid longitude in position"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_openRegion() throws Exception {
        // first and last vertices dont match - open region
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.946).build() // should be 55.945 to close the region
                        ))
                        .build())
                .build();
        doThrow(new InvalidRequestException("The first and last vertices in 'region.vertices' must be the same to form a closed polygon"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("The first and last vertices in 'region.vertices' must be the same to form a closed polygon"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_insufficientVertices() throws Exception {
        // less than 3 vertices
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.944).build() // only 3 vertices
                        ))
                        .build())
                .build();
        doThrow(new InvalidRequestException("'region.vertices' must contain at least 4 vertices to form a closed polygon"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'region.vertices' must contain at least 4 vertices to form a closed polygon"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_pointOnEdge() throws Exception {
        // point exactly on the edge of the polygon
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.1865)
                        .latitude(55.944)
                        .build())
                .region(Region.builder()
                        .name("TestRegion")
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.944).build() // closing the polygon
                        ))
                        .build())
                .build();
        when(restService.isInRegion(any(LngLat.class), any(Region.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(1)).isInRegion(any(LngLat.class), any(Region.class));
    }

    @Test
    public void testIsInRegion_nullRegionName() throws Exception {
        RegionRequest request = RegionRequest.builder()
                .position(LngLat.builder()
                        .longitude(-3.186874)
                        .latitude(55.944494)
                        .build())
                .region(Region.builder()
                        .name(null) // null region name
                        .vertices(Arrays.asList(
                                LngLat.builder().longitude(-3.187).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.944).build(),
                                LngLat.builder().longitude(-3.186).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.945).build(),
                                LngLat.builder().longitude(-3.187).latitude(55.944).build() // closing the polygon
                        ))
                        .build())
                .build();
        doThrow(new InvalidRequestException("'region.name' is required and cannot be empty"))
                .when(validationService).validateIsInRegionRequest(any(RegionRequest.class));
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("'region.name' is required and cannot be empty"));
        verify(validationService, times(1)).validateIsInRegionRequest(any(RegionRequest.class));
        verify(restService, times(0)).isInRegion(any(LngLat.class), any(Region.class));
    }
}


