package uk.ac.ed.inf.ilpcw1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.service.RestService;
import uk.ac.ed.inf.ilpcw1.service.ValidationService;

@RestController
@RequestMapping("/api/v1")
public class RestServiceController {

    private final RestService restService;
    private final ValidationService validationService;

    @Autowired
    public RestServiceController(RestService restService, ValidationService validationService) {
        this.restService = restService;
        this.validationService = validationService;

    }

    @GetMapping("/uid")
    public String getStudentId() {
        return "s2524182";
    }

    /**
     * Calculate the distance between two geographical positions.
     * @param request - The request body containing position1 and position2.
     * @return - The calculated distance as a Double.
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> calculateDistance(@RequestBody DistanceRequest request) {
        // Validate the entire request
        validationService.validateDistanceRequest(request);

        Double distance = restService.calculateDistance(request.getPosition1(), request.getPosition2());
        return ResponseEntity.ok(distance);
    }

    /**
     * Check if two geographical positions are within a close distance.
     * @param request - The request body containing position1 and position2.
     * @return - True if the positions are close, false otherwise.
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody CloseToRequest request) {
        // Validate the entire request
        validationService.validateCloseTo(request);

        boolean isClose = restService.isCloseTo(request.getPosition1(), request.getPosition2());
        return ResponseEntity.ok(isClose);
    }


    /**
     * Calculate the next geographical position based on a starting position and an angle.
     * @param request - The request body containing the start position and angle.
     * @return - The new geographical position as LngLat.
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest request) {
        // Validate the entire request
        validationService.validateNextPositionRequest(request);

        LngLat position = restService.nextPosition(request.getStart(), request.getAngle());
        return ResponseEntity.ok(position);
    }


    /**
     * Check if a geographical position is inside a specified region.
     * @param request - The request body containing the position and region.
     * @return - True if the position is inside the region, false otherwise.
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionRequest request) {
        // Validate the entire request
        validationService.validateIsInRegionRequest(request);

        boolean insideRegion = restService.isInRegion(request.getPosition(), request.getRegion());
        return ResponseEntity.ok(insideRegion);
    }
}
