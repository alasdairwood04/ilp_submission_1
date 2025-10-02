package uk.ac.ed.inf.ilpcw1.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1")
public class RestServiceController {


    // http://localhost:8080/api/v1/uuid
    @GetMapping("/uuid")
    public String getStudentId() {
        return "s2524182";
    }
}
