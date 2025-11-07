package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.config.ILPConfig;
import uk.ac.ed.inf.ilpcw1.data.Drone;
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for querying and filtering drones
 */

@Service
public class DroneQueryService {
    private static final Logger logger = LoggerFactory.getLogger(DroneQueryService.class);
    
}
