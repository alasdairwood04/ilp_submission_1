package uk.ac.ed.inf.ilpcw1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ed.inf.ilpcw1.data.CloseToRequest;
import uk.ac.ed.inf.ilpcw1.data.DistanceRequest;
import uk.ac.ed.inf.ilpcw1.data.LngLat;
import uk.ac.ed.inf.ilpcw1.data.NextPositionRequest;
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


    @GetMapping("/uuid")
    public String getStudentId() {
        return "s2524182";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> calculateDistance(@RequestBody DistanceRequest request) {
        // validate the input positions
        if (!validationService.isValidLngLat(request.getPosition1()) ||
                !validationService.isValidLngLat(request.getPosition2())) {
            return ResponseEntity.badRequest().build();
        }


        double distance = restService.calculateDistance(request.getPosition1(), request.getPosition2());
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
}
