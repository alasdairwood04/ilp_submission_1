package uk.ac.ed.inf.ilpcw1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ed.inf.ilpcw1.data.DistanceRequest;
import uk.ac.ed.inf.ilpcw1.service.RestService;

@RestController
@RequestMapping("/api/v1")
public class RestServiceController {

    private final RestService restService;

    @Autowired
    public RestServiceController(RestService restService) {
        this.restService = restService;
    }


    @GetMapping("/uuid")
    public String getStudentId() {
        return "s2524182";
    }

    @PostMapping("/distanceTo")
    public double calculateDistance(@RequestBody DistanceRequest request) {
        return restService.calculateDistance(request.getPosition1(), request.getPosition2());
    }
}
