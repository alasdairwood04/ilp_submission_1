package uk.ac.ed.inf.ilpcw1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.Region;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import uk.ac.ed.inf.ilpcw1.service.RestService.*;

public class RestServiceTest {

    private RestService restService;

    @BeforeEach
    public void setUp() {
        restService = new RestService();
    }

    @Test
    public void testIsPointInRegion_Inside() {
        Region region = Region.builder()
                .name("TestRegion")
                .vertices(Arrays.asList(
                        new LngLat(-3.0, 55.0),
                        new LngLat(-3.0, 56.0),
                        new LngLat(-2.0, 56.0),
                        new LngLat(-2.0, 55.0)
                ))
                .build();

        LngLat point = new LngLat(-2.5, 55.5);
        assertTrue(restService.isInRegion(point, region));
    }

    @Test
    public void testIsPointInRegion_Outside() {
        Region region = Region.builder()
                .name("TestRegion")
                .vertices(Arrays.asList(
                        new LngLat(-3.0, 55.0),
                        new LngLat(-3.0, 56.0),
                        new LngLat(-2.0, 56.0),
                        new LngLat(-2.0, 55.0)
                ))
                .build();

        LngLat point = new LngLat(-4.0, 55.5);
        assertFalse(restService.isInRegion(point, region));
    }

    @Test
    public void testIsPointInRegion_OnEdge() {
        Region region = Region.builder()
                .name("TestRegion")
                .vertices(Arrays.asList(
                        new LngLat(-3.0, 55.0),
                        new LngLat(-3.0, 56.0),
                        new LngLat(-2.0, 56.0),
                        new LngLat(-2.0, 55.0)
                ))
                .build();

        LngLat point = new LngLat(-3.0, 55.5);
        assertTrue(restService.isInRegion(point, region));
    }

    @Test
    public void testIsPointInRegion_ComplexShape() {
        Region region = Region.builder()
                .name("ComplexRegion")
                .vertices(Arrays.asList(
                        new LngLat(0.0, 0.0),
                        new LngLat(5.0, 0.0),
                        new LngLat(5.0, 5.0),
                        new LngLat(3.0, 3.0),
                        new LngLat(0.0, 5.0)
                ))
                .build();
        LngLat insidePoint = new LngLat(2.0, 2.0);
        LngLat outsidePoint = new LngLat(4.0, 4.0);

        assertTrue(restService.isInRegion(insidePoint, region));
        assertFalse(restService.isInRegion(outsidePoint, region));

    }

}
