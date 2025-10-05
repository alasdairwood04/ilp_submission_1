package uk.ac.ed.inf.ilpcw1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.InvalidCoordinateException;
import uk.ac.ed.inf.ilpcw1.service.RestService;
import uk.ac.ed.inf.ilpcw1.service.ValidationService;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;
import jakarta.validation.Valid;

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

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> calculateDistance(@Valid @RequestBody DistanceRequest request) {
        // 1. Check for null main objects
        if (request.getPosition1() == null || request.getPosition2() == null) {
            throw new InvalidRequestException("Request must include both 'position1' and 'position2'.");
        }

        // 2. ensure that fields within each position are lng and lat
        if (!validationService.isValidPositionFormat(request.getPosition1())) {
            throw new InvalidRequestException("The 'position1' field must contain valid 'lng' and 'lat' values.");
        }

        // 2. Use the validation service, which can throw the specific exception
        if (!validationService.isValidLngLat(request.getPosition1())) {
            throw new InvalidCoordinateException("The coordinates for 'position1' are invalid or out of range.");
        }

        if (!validationService.isValidLngLat(request.getPosition2())) {
            throw new InvalidCoordinateException("The coordinates for 'position2' are invalid or out of range.");
        }


        Double distance = restService.calculateDistance(request.getPosition1(), request.getPosition2());
        return ResponseEntity.ok(distance);
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody CloseToRequest request) {
        boolean isClose = restService.isCloseTo(request.getPosition1(), request.getPosition2());
        return ResponseEntity.ok(isClose);
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest request) {
        LngLat position = restService.nextPosition(request.getStart(), request.getAngle());
        return ResponseEntity.ok(position);
    }

//    @PostMapping("/isInRegion")
//    public ResponseEntity<LngLat> isInRegion(@RequestBody RegionRequest request) {
//        boolean insideRegion = restService.isInRegion(request.getPosition(), request.getRegion());
//        return ResponseEntity.ok(insideRegion);
//    }

}
