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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DroneQueryService
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("CalcDeliveryPathMethodTest")
public class CalcDeliveryPathMethodTest {

    @Mock
    private ILPServiceClient ilpServiceClientMock;

    @Mock
    private DroneQueryService droneQueryService;

    @BeforeEach
    public void setUp() {
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());
    }


    // drone list from the ILP REST API
    private List<Drone> createFullDroneList() {
        return List.of(
                Drone.builder()
                        .id("1")
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
                        .id("2")
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
                        .id("3")
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
                        .id("4")
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
                        .id("5")
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
                        .id("6")
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
                        .id("7")
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
                        .id("8")
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
                        .id("9")
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
                        .id("10")
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
                                        .id("1")
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id("2")
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
                                        .id("3")
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id("4")
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(MONDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(11, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.WEDNESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SATURDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id("5")
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
                                        .id("6")
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
                                        .id("7")
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
                                        .id("8")
                                        .available(List.of(
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.TUESDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.THURSDAY).from(LocalTime.of(12, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.FRIDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build(),
                                                DroneAvailabilityDetails.builder().dayOfWeek(DayOfWeek.SUNDAY).from(LocalTime.of(0, 0, 0)).until(LocalTime.of(23, 59, 59)).build()
                                        ))
                                        .build(),
                                DronesAtServicePoint.builder()
                                        .id("9")
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
                                        .id("10")
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

    private List<ServicePoints> createServicePoints() {
        return List.of(
                ServicePoints.builder()
                        .name("Appleton Tower")
                        .id(1)
                        .location(LngLat.builder().longitude(-3.18635807889864).latitude(55.9446806670849).build())
                        .build(),
                ServicePoints.builder()
                        .name("Ocean Terminal")
                        .id(2)
                        .location(LngLat.builder().longitude(-3.17732611501824).latitude(55.9811862793337).build())
                        .build()
        );
    }

    private List<RestrictedArea> createRestrictedAreas() {
        return List.of(
                RestrictedArea.builder()
                        .name("George Square Area")
                        .id(1)
                        .limits(Limits.builder().lower(0).upper(-1).build())
                        .vertices(List.of(
                                LngLat.builder().longitude(-3.19057881832123).latitude(55.9440241257753).build(),
                                LngLat.builder().longitude(-3.18998873233795).latitude(55.9428465054091).build(),
                                LngLat.builder().longitude(-3.1870973110199).latitude(55.9432881172426).build(),
                                LngLat.builder().longitude(-3.18768203258514).latitude(55.9444777403937).build(),
                                LngLat.builder().longitude(-3.19057881832123).latitude(55.9440241257753).build()
                        ))
                        .build(),
                RestrictedArea.builder()
                        .name("Dr Elsie Inglis Quadrangle")
                        .id(2)
                        .limits(Limits.builder().lower(0).upper(-1).build())
                        .vertices(List.of(
                                LngLat.builder().longitude(-3.19071829319).latitude(55.9451957023404).build(),
                                LngLat.builder().longitude(-3.19061636924744).latitude(55.9449824179636).build(),
                                LngLat.builder().longitude(-3.19002628326416).latitude(55.9450755422726).build(),
                                LngLat.builder().longitude(-3.19013357162476).latitude(55.945297838105).build(),
                                LngLat.builder().longitude(-3.19071829319).latitude(55.9451957023404).build()
                        ))
                        .build(),
                RestrictedArea.builder()
                        .name("Bristo Square Open Area")
                        .id(3)
                        .limits(Limits.builder().lower(0).upper(-1).build())
                        .vertices(List.of(
                                LngLat.builder().longitude(-3.18954348564148).latitude(55.9455231366331).build(),
                                LngLat.builder().longitude(-3.18938255310059).latitude(55.9455321485469).build(),
                                LngLat.builder().longitude(-3.1892591714859).latitude(55.9454480372693).build(),
                                LngLat.builder().longitude(-3.18920016288757).latitude(55.9453368899437).build(),
                                LngLat.builder().longitude(-3.18919479846954).latitude(55.9451957023404).build(),
                                LngLat.builder().longitude(-3.18913578987122).latitude(55.9451175983387).build(),
                                LngLat.builder().longitude(-3.18813800811768).latitude(55.9452738061846).build(),
                                LngLat.builder().longitude(-3.18855106830597).latitude(55.9461059027456).build(),
                                LngLat.builder().longitude(-3.18953812122345).latitude(55.9455591842759).build(),
                                LngLat.builder().longitude(-3.18954348564148).latitude(55.9455231366331).build()
                        ))
                        .build(),
                RestrictedArea.builder()
                        .name("Bayes Central Area")
                        .id(4)
                        .limits(Limits.builder().lower(0).upper(-1).build())
                        .vertices(List.of(
                                LngLat.builder().longitude(-3.1876927614212).latitude(55.9452069673277).build(),
                                LngLat.builder().longitude(-3.18755596876144).latitude(55.9449621408666).build(),
                                LngLat.builder().longitude(-3.18698197603226).latitude(55.9450567672283).build(),
                                LngLat.builder().longitude(-3.18723276257515).latitude(55.9453699337766).build(),
                                LngLat.builder().longitude(-3.18744599819183).latitude(55.9453361389472).build(),
                                LngLat.builder().longitude(-3.18737357854843).latitude(55.9451934493426).build(),
                                LngLat.builder().longitude(-3.18759351968765).latitude(55.9451566503593).build(),
                                LngLat.builder().longitude(-3.18762436509132).latitude(55.9452197343093).build(),
                                LngLat.builder().longitude(-3.1876927614212).latitude(55.9452069673277).build()
                        ))
                        .build()
        );
    }
    @Test
    @DisplayName("Scenario 1: Happy Path - Single Drone from Appleton")
    void testCalculatePath_HappyPath() {
        // 1. Setup Data
        List<Drone> drones = createFullDroneList();
        List<DroneServicePointRequest> availability = createFullAvailabilityList();
        List<ServicePoints> servicePoints = createServicePoints();
        List<RestrictedArea> restrictedAreas = createRestrictedAreas();

        // 2. Mock ILP Client
        when(ilpServiceClientMock.getAllDrones()).thenReturn(drones);
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(availability);
        when(ilpServiceClientMock.getServicePoints()).thenReturn(servicePoints);
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(restrictedAreas);

        // 3. Create dispatch
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(1001)
                .date(LocalDate.of(2025, 12, 22)) // Monday
                .time(LocalTime.of(10, 0))
                .requirements(Requirements.builder()
                        .capacity(1.0)
                        .cooling(false)
                        .heating(false)
                        .maxCost(50.0)
                        .build())
                .delivery(LngLat.builder().longitude(-3.185000).latitude(55.945000).build()) // Near Appleton
                .build();

        // 5. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(dispatch));

        // --- NEW: Print the response to the console ---
        System.out.println("Final Response: " + response);

        // 6. Assertions
        assertNotNull(response);
        assertEquals(1, response.getDronePaths().size(), "Should use exactly 1 drone");

        DronePathDetails path = response.getDronePaths().getFirst();

        // Verify it picked a drone from Appleton (Service Point 1 => Drones 1,2,3,4,5)
        assertTrue(List.of("1", "2", "3", "4", "5").contains(path.getDroneId()));

        // Check flight path structure
        assertEquals(2, path.getDeliveries().size()); // Outbound and Return
        assertEquals(1001, path.getDeliveries().get(0).getDeliveryId());
        assertFalse(path.getDeliveries().get(0).getFlightPath().isEmpty());
    }

    @Test
    @DisplayName("Scenario 2: Heavy Lifter - Capacity & Availability Constraint")
    void testCalcDeliveryPath_HeavyLifter() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());

        // Re-init with real RestService
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create Dispatch (18.0kg payload on Monday)
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(2001)
                .date(LocalDate.of(2025, 12, 22)) // Monday
                .time(LocalTime.of(10, 0))
                .requirements(Requirements.builder()
                        .capacity(18.0) // Only Drone 3 & 8 have capacity >= 18
                        .cooling(false)
                        .heating(false)
                        .build())
                .delivery(LngLat.builder().longitude(-3.186000).latitude(55.944000).build())
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(dispatch));

        System.out.println("Final Response: " + response);


        // 4. Assertions
        assertNotNull(response);
        assertEquals(1, response.getDronePaths().size());

        // Drone 8 is NOT available on Mondays. Drone 3 IS available.
        assertEquals("3", response.getDronePaths().get(0).getDroneId(),
                "Should select Drone 3 (High Capacity + Available on Monday)");
    }

    @Test
    @DisplayName("Scenario 3: Spatial Split - Two Drones Required")
    void testCalcDeliveryPath_SpatialSplit() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create Dispatches (One at Appleton, One at Ocean Terminal)
        MedDispatchRec orderAppleton = MedDispatchRec.builder()
                .id(3001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(9, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186358).latitude(55.944680).build()) // At Appleton
                .build();

        MedDispatchRec orderOcean = MedDispatchRec.builder()
                .id(3002)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(9, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.177326).latitude(55.981186).build()) // At Ocean Terminal
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(orderAppleton, orderOcean));


        System.out.println("Final Response: " + response);


        // 4. Assertions
        assertNotNull(response);

        // Should have split into 2 paths because they are too far for a single optimized trip
        // (Or at least your recursive logic should prefer splitting 2 far-away points)
        assertEquals(2, response.getDronePaths().size(), "Should require 2 drones for far-apart orders");

        // Verify Total Cost is sum of both
        assertTrue(response.getTotalCost() > 0);
    }

    @Test
    @DisplayName("Scenario 4: No-Fly Zone Avoidance")
    void testCalcDeliveryPath_NoFlyZone() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Dispatch West of George Square (Appleton is East)
        // Straight line intersects George Square Area
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(4001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(14, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.191500).latitude(55.943500).build())
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(dispatch));

        System.out.println("Final Response: " + response);

        // 4. Assertions
        assertEquals(1, response.getDronePaths().size());
        List<LngLat> flightPath = response.getDronePaths().get(0).getDeliveries().get(0).getFlightPath();

        assertFalse(flightPath.isEmpty());

        // In a real scenario, we might check move count > euclidean moves,
        // but here just ensuring a solution was found is a good baseline.
    }

    @Test
    @DisplayName("Scenario 5: Max Cost Constraint")
    void testCalcDeliveryPath_MaxCost() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Dispatch with tight budget
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(5001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(11, 0))
                .requirements(Requirements.builder()
                        .capacity(2.0)
                        .maxCost(25.0) // Very tight budget
                        .build())
                .delivery(LngLat.builder().longitude(-3.180000).latitude(55.950000).build())
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(dispatch));

        System.out.println("Final Response: " + response);

        // 4. Assertions
        assertEquals(1, response.getDronePaths().size());
        String chosenDrone = response.getDronePaths().get(0).getDroneId();

        // Drone 9 (Cost 0.06) would likely fail this check on a long flight.
        // Drone 1 (Cost 0.01) or Drone 7 (Cost 0.015) should be chosen.
        assertTrue(List.of("1", "7", "4").contains(chosenDrone),
                "Should choose a low-cost drone (1, 4, or 7) to meet maxCost constraint. Got: " + chosenDrone);

        assertTrue(response.getTotalCost() <= 25.0, "Total cost must be within limit");
    }

    @Test
    @DisplayName("Scenario 6: Impossible Order (Inside No-Fly Zone)")
    void testCalcDeliveryPath_ImpossibleOrder() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Dispatch INSIDE George Square Area
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(6001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(12, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.189000).latitude(55.944000).build())
                .build();

        // 3. Execute & Assert
        // Expecting the specific RuntimeException you throw in your "Failure/Base Case"
        Exception exception = assertThrows(RuntimeException.class, () -> {
            droneQueryService.calcDeliveryPath(List.of(dispatch));
        });

        assertTrue(exception.getMessage().contains("Undeliverable"),
                "Exception message should mention undeliverable status");
    }

    @Test
    @DisplayName("Scenario 7: Double Drop - One Drone, Two Deliveries")
    void testCalcDeliveryPath_DoubleDrop() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create two nearby dispatches (Both near Appleton)
        MedDispatchRec order1 = MedDispatchRec.builder()
                .id(7001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(10, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186500).latitude(55.944700).build()) // Very close
                .build();

        MedDispatchRec order2 = MedDispatchRec.builder()
                .id(7002)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(10, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186600).latitude(55.944800).build()) // Very close
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(order1, order2));

        System.out.println(response);

        // 4. Assertions
        assertNotNull(response);

        // Should be efficient enough to use just ONE drone
        assertEquals(1, response.getDronePaths().size(), "Should merge deliveries into one drone flight");

        DronePathDetails path = response.getDronePaths().get(0);
        assertEquals(3, path.getDeliveries().size(), "Drone should make 2 stops"); // 2 deliveries + return

        // Verify Delivery Sequence (just ensuring both IDs are present)
        List<Integer> deliveredIds = path.getDeliveries().stream()
                .map(Deliveries::getDeliveryId)
                .filter(id -> id != null) // Filter out the 'null' return-to-base leg if you implement it that way
                .toList();

        assertTrue(deliveredIds.containsAll(List.of(7001, 7002)));
    }

    @Test
    @DisplayName("Scenario 8: Triple Chain - One Drone, Three Deliveries")
    void testCalcDeliveryPath_TripleChain() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create 3 orders. Total Weight = 3.5 + 3.5 + 3.5 = 10.5kg
        // Drone 1 (Cap 4) -> Fail
        // Drone 2 (Cap 8) -> Fail
        // Drone 5 (Cap 12) -> Pass
        Requirements standardReq = Requirements.builder().capacity(3.5).build();

        MedDispatchRec d1 = MedDispatchRec.builder().id(8001).date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                .requirements(standardReq).delivery(LngLat.builder().longitude(-3.185000).latitude(55.945000).build()).build();
        MedDispatchRec d2 = MedDispatchRec.builder().id(8002).date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                .requirements(standardReq).delivery(LngLat.builder().longitude(-3.185500).latitude(55.945500).build()).build();
        MedDispatchRec d3 = MedDispatchRec.builder().id(8003).date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                .requirements(standardReq).delivery(LngLat.builder().longitude(-3.186000).latitude(55.946000).build()).build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(d1, d2, d3));

        System.out.println(response);

        // 4. Assertions
        assertEquals(1, response.getDronePaths().size(), "Should consolidate into 1 drone");

        String droneId = response.getDronePaths().get(0).getDroneId();
        assertTrue(List.of("3", "5", "8", "10").contains(droneId),
                "Must pick a high-capacity drone (>= 10.5kg). Got: " + droneId);

        assertEquals(3, response.getDronePaths().get(0).getDeliveries().size() - (response.getDronePaths().get(0).getDeliveries().stream().anyMatch(d -> d.getDeliveryId() == null) ? 1 : 0),
                "Should have 3 delivery legs");
    }

    @Test
    @DisplayName("Scenario 9: Ocean Split - Two Drones from Different Bases")
    void testCalcDeliveryPath_DifferentServicePoints() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create Dispatches
        // Order A: Near Appleton Tower (Service Point 1)
        MedDispatchRec orderAppleton = MedDispatchRec.builder()
                .id(9001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(14, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186358).latitude(55.944680).build())
                .build();

        // Order B: Near Ocean Terminal (Service Point 2) - ~4km North
        MedDispatchRec orderOcean = MedDispatchRec.builder()
                .id(9002)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(14, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.177326).latitude(55.981186).build())
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(orderAppleton, orderOcean));

        System.out.println(response);

        // 4. Assertions
        assertNotNull(response);
        assertEquals(2, response.getDronePaths().size(), "Must use 2 drones due to distance");

        // Analyze the paths to ensure they picked logical drones
        DronePathDetails path1 = response.getDronePaths().get(0);
        DronePathDetails path2 = response.getDronePaths().get(1);

        // Helper to check if a drone ID belongs to Appleton (1-5) or Ocean (6-10)
        boolean path1IsAppleton = Integer.parseInt(path1.getDroneId()) <= 5;
        boolean path2IsAppleton = Integer.parseInt(path2.getDroneId()) <= 5;

        // We expect one from Appleton and one from Ocean
        assertTrue(path1IsAppleton ^ path2IsAppleton,
                "One drone should be from Appleton (ID 1-5) and one from Ocean Terminal (ID 6-10)");
    }

    @Test
    @DisplayName("Scenario 10: Return Trip Battery Check")
    void testCalcDeliveryPath_ReturnTripBattery() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        // Use real RestService for accurate distance calc
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create Dispatch
        // Located approx 0.075 degrees away (approx 500 moves one way)
        // Drone 2 (Max 1000) -> 500 out + 500 back + hover = >1000. Fail.
        // Drone 1 (Max 2000) -> Success.
        MedDispatchRec dispatch = MedDispatchRec.builder()
                .id(10001)
                .date(LocalDate.of(2025, 12, 22))
                .time(LocalTime.of(10, 0))
                .requirements(Requirements.builder().capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186358 + 0.076).latitude(55.944680).build())
                .build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(dispatch));

        System.out.println(response);

        // 4. Assertions
        assertNotNull(response);
        String droneId = response.getDronePaths().get(0).getDroneId();

        assertNotEquals("2", droneId, "Drone 2 should not be chosen (insufficient battery for return trip)");
        assertTrue(List.of("1", "6", "3", "5").contains(droneId), "Should choose a long-range drone");
    }

    @Test
    @DisplayName("Scenario 11: Scatter Bomb - Many Small Orders")
    void testCalcDeliveryPath_ManySmallOrders() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create 10 small dispatches around Appleton
        List<MedDispatchRec> dispatches = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dispatches.add(MedDispatchRec.builder()
                    .id(11000 + i)
                    .date(LocalDate.of(2025, 12, 22))
                    .time(LocalTime.of(10, 0))
                    .requirements(Requirements.builder().capacity(0.1).build())
                    // Scatter them slightly
                    .delivery(LngLat.builder()
                            .longitude(-3.186358 + (i * 0.001))
                            .latitude(55.944680 + (i % 2 == 0 ? 0.001 : -0.001))
                            .build())
                    .build());
        }

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(dispatches);

        System.out.println(response);

        // 4. Assertions
        assertNotNull(response);
        assertFalse(response.getDronePaths().isEmpty());

        // Verify all IDs are present in the response (spread across however many drones)
        long totalDelivered = response.getDronePaths().stream()
                .flatMap(p -> p.getDeliveries().stream())
                .filter(d -> d.getDeliveryId() != null)
                .count();

        assertEquals(10, totalDelivered, "All 10 orders must be scheduled");
    }

    @Test
    @DisplayName("Scenario 12: Conflicting Requirements - Split Required")
    void testCalcDeliveryPath_ConflictingRequirements() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        // 2. Create Dispatches
        // Order A: Needs Cooling (Only Drones 1, 5, 8, 9)
        MedDispatchRec orderCool = MedDispatchRec.builder()
                .id(12001).date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                .requirements(Requirements.builder().cooling(true).capacity(1.0).build())
                .delivery(LngLat.builder().longitude(-3.186000).latitude(55.944000).build()).build();

        // Order B: Needs High Capacity (20.0) (Only Drones 3, 8)
        // BUT: Drone 8 (which has both) is NOT available on Monday (based on your mock data).
        // Drone 3 is available but has NO cooling.
        // Therefore: No single available drone has (Cooling AND Capacity 20).
        MedDispatchRec orderHeavy = MedDispatchRec.builder()
                .id(12002).date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                .requirements(Requirements.builder().cooling(false).capacity(20.0).build())
                .delivery(LngLat.builder().longitude(-3.186000).latitude(55.944000).build()).build();

        // 3. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(List.of(orderCool, orderHeavy));

        System.out.println(response);

        // 4. Assertions
        assertNotNull(response);
        assertEquals(2, response.getDronePaths().size(),
                "Should split into 2 drones because no single available drone meets both requirements");
    }

    @Test
    @DisplayName("Scenario 13: Twin Clusters - 2 Drones, 3 Deliveries Each (North vs South)")
    void testCalcDeliveryPath_TwinClusters() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        List<MedDispatchRec> dispatches = new ArrayList<>();
        Requirements standardReq = Requirements.builder().capacity(1.5).build();

        // --- Cluster A: 3 Orders near Appleton (South) ---
        // Approx Location: -3.186, 55.944
        for (int i = 0; i < 3; i++) {
            dispatches.add(MedDispatchRec.builder()
                    .id(13000 + i)
                    .date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                    .requirements(standardReq)
                    .delivery(LngLat.builder()
                            .longitude(-3.186358 + (i * 0.0005)) // Slight offset
                            .latitude(55.944680 + (i * 0.0005))
                            .build())
                    .build());
        }

        // --- Cluster B: 3 Orders near Ocean Terminal (North) ---
        // Approx Location: -3.177, 55.981 (Far away!)
        for (int i = 0; i < 3; i++) {
            dispatches.add(MedDispatchRec.builder()
                    .id(13100 + i)
                    .date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(10, 0))
                    .requirements(standardReq)
                    .delivery(LngLat.builder()
                            .longitude(-3.177326 + (i * 0.0005))
                            .latitude(55.981186 + (i * 0.0005))
                            .build())
                    .build());
        }

        // 2. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(dispatches);

        System.out.println(response);

        // 3. Assertions
        assertNotNull(response);
        assertEquals(2, response.getDronePaths().size(),
                "Should split into 2 drones (one for North cluster, one for South)");

        // Verify load balancing: Both drones should have roughly equal work (3 deliveries each)
        int deliveriesDrone1 = response.getDronePaths().get(0).getDeliveries().size();
        int deliveriesDrone2 = response.getDronePaths().get(1).getDeliveries().size();

        // Note: .size() includes the deliveries. If your implementation adds a 'return to base'
        // as a delivery with null ID, you might need to subtract 1 or filter.
        // Assuming size() = actual deliveries based on previous tests:
        assertTrue(deliveriesDrone1 >= 2 && deliveriesDrone2 >= 2,
                "Each drone should handle multiple deliveries (approx 3 each)");

        // Verify Geographic Split: Drones should be from different bases
        // IDs 1-5 are Appleton, 6-10 are Ocean Terminal
        String id1 = response.getDronePaths().get(0).getDroneId();
        String id2 = response.getDronePaths().get(1).getDroneId();

        boolean hasAppletonDrone = Integer.parseInt(id1) <= 5 || Integer.parseInt(id2) <= 5;
        boolean hasOceanDrone = Integer.parseInt(id1) >= 6 || Integer.parseInt(id2) >= 6;

        assertTrue(hasAppletonDrone && hasOceanDrone,
                "Should utilize drones from both Service Points due to distance");
    }

    @Test
    @DisplayName("Scenario 14: Capacity Saturation - Multiple Drones to Same Region")
    void testCalcDeliveryPath_CapacitySaturation() {
        // 1. Setup
        when(ilpServiceClientMock.getAllDrones()).thenReturn(createFullDroneList());
        when(ilpServiceClientMock.getDroneAvailability()).thenReturn(createFullAvailabilityList());
        when(ilpServiceClientMock.getServicePoints()).thenReturn(createServicePoints());
        when(ilpServiceClientMock.getRestrictedAreas()).thenReturn(createRestrictedAreas());
        droneQueryService = new DroneQueryService(ilpServiceClientMock, new RestService());

        Requirements heavyReq = Requirements.builder().capacity(5.0).build();
        List<MedDispatchRec> dispatches = new ArrayList<>();

        // Create 5 Heavy Orders at Ocean Terminal
        // Total Weight = 25kg.
        // Max Capacity of any single drone is 20kg (Drone 3 or 8).
        // Therefore, IMPOSSIBLE for 1 drone.
        for (int i = 0; i < 5; i++) {
            dispatches.add(MedDispatchRec.builder()
                    .id(14000 + i)
                    .date(LocalDate.of(2025, 12, 22)).time(LocalTime.of(12, 0)) // MONDAY
                    .requirements(heavyReq)
                    .delivery(LngLat.builder()
                            .longitude(-3.177326) // Exact Ocean Terminal location
                            .latitude(55.981186)
                            .build())
                    .build());
        }

        // 2. Execute
        DeliveryPathResponse response = droneQueryService.calcDeliveryPath(dispatches);

        System.out.println(response);

        // 3. Assertions
        assertNotNull(response);
        assertTrue(response.getDronePaths().size() >= 2,
                "Must use at least 2 drones because 25kg > Max Capacity (20kg)");

        // Verify one of the drones is the "Heavy Lifter" (Drone 8 or 3) taking the bulk
        boolean usedHeavyLifter = response.getDronePaths().stream()
                .anyMatch(p -> List.of("3", "8").contains(p.getDroneId()));

        assertTrue(usedHeavyLifter, "Should utilize a heavy-lift drone (ID 3 or 8) for efficiency");

        // Total scheduled deliveries must match input
        long totalScheduled = response.getDronePaths().stream()
                .mapToLong(p -> p.getDeliveries().stream()
                        .filter(d -> d.getDeliveryId() != null).count())
                .sum();

        assertEquals(5, totalScheduled, "All 5 heavy orders must be delivered");
    }
}

