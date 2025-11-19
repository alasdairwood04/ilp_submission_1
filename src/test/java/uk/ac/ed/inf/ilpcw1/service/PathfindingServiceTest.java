package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.inf.ilpcw1.data.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PathfindingServiceTest {
    @Mock
    private RestService restService;

    private PathfindingService pathfindingService;

    @BeforeEach
    void setUp() {
        pathfindingService = new PathfindingService(restService);
    }

    private LngLat servicePoint1() {
        return LngLat.builder()
                .longitude(-3.18635807889864)
                .latitude(55.9446806670849)
                .build();
    }

    private LngLat servicePoint2() {
        return LngLat.builder()
                .longitude(-3.17732611501824)
                .latitude(55.9811862793337)
                .build();
    }


    @DisplayName("Pathfinding Service Tests")
    @Test
    void testFindPath() {
        LngLat start = servicePoint1();

        LngLat goal = LngLat.builder()
                .longitude(-3.00)
                .latitude(55.121)
                .build();

        List<LngLat> noFlyZone = List.of(
                LngLat.builder().longitude(-3.19057881832123).latitude(55.9440241257753).build(),
                LngLat.builder().longitude(-3.18998873233795).latitude(55.9428465054091).build(),
                LngLat.builder().longitude(-3.1870973110199).latitude(55.9432881172426).build(),
                LngLat.builder().longitude(-3.18768203258514).latitude(55.9444777403937).build(),
                LngLat.builder().longitude(-3.19057881832123).latitude(55.9440241257753).build()
        );
        RestrictedArea area = RestrictedArea.builder()
                .name("George Square Area")
                .id(1)
                .limits(Limits.builder()
                        .lower(0)
                        .upper(-1)
                        .build())
                .vertices(noFlyZone).build();


        pathfindingService.findPath(start, goal, List.of(area));

        Assertions.assertTrue(true); // Placeholder assertion
    }
}
