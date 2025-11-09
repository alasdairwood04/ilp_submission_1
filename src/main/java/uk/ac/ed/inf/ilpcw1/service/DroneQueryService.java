package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    private final ILPServiceClient ilpServiceClient;

    @Autowired
    public DroneQueryService(ILPServiceClient ilpServiceClient) {
        this.ilpServiceClient = ilpServiceClient;
    }


    /**
     * Filter drones by cooling capability
     * @param hasCooling true to get drones with cooling, false for drones without
     * @return List of drones IDs matching criteria - either having or not having cooling
     */
    public List<Integer> filterByCooling(boolean hasCooling) {
        logger.info("Filtering drones with cooling={}", hasCooling);

        List<Drone> allDrones = ilpServiceClient.getAllDrones();

        List<Integer> droneIds = allDrones.stream()
                .filter(drone -> drone.getCapability() != null)
                .filter(drone -> {
                    Boolean cooling = drone.getCapability().getCooling();

                    // Handle null as false
                    boolean actualCooling = cooling != null && cooling;
                    return actualCooling == hasCooling;
                })
                .map(Drone::getId)
                .toList();

        logger.info("Found {} drones with cooling={}", droneIds.size(), hasCooling);
        return droneIds;
    }

    /**
     * Get details of a specific drone by ID
     * @param id The drone ID
     * @return The Drone object
     * @throws DroneNotFoundException if Drone is not found
     */
    public Drone getByDroneId(Integer id) {
        logger.info("Fetching drone with id={}", id);

        List<Drone> allDrones = ilpServiceClient.getAllDrones();

        return allDrones.stream()
                .filter(drone -> drone.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> {
            logger.warn("Drone with id={} not found", id);
            return new DroneNotFoundException("Drone with id=" + id + " not found");
        });
    }
}
