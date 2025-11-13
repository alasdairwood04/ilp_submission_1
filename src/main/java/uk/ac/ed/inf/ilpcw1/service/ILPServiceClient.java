package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.ilpcw1.data.Drone;
import uk.ac.ed.inf.ilpcw1.data.DroneServicePointRequest;
import uk.ac.ed.inf.ilpcw1.data.RestrictedArea;
import uk.ac.ed.inf.ilpcw1.data.ServicePoints;

import java.util.Arrays;
import java.util.List;

/**
 * Service client for fetching data from the ILP REST service
 */
@Service
public class ILPServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ILPServiceClient.class);

    private final String ilpEndpoint;
    private final RestTemplate restTemplate;


    //
    @Autowired
    public ILPServiceClient(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch all drones from the ILP REST Service
     * @return List of all drones
     */

    public List<Drone> getAllDrones() {
        try {
            String url = ilpEndpoint + "drones";
            logger.info("Fetching drones from: {}", url);

            Drone[] drones = restTemplate.getForObject(url, Drone[].class);

            if (drones == null) {
                logger.warn("No drones returned from ILP service");
                return List.of();
            }
            logger.info("Successfully fetched {} drones", drones.length);
            return Arrays.asList(drones);
        } catch (Exception e) {
            logger.error("Error fetching drones from ILP service", e);
            throw new RuntimeException("Failed to fetch drones from ILP service", e);
        }
    }

    /**
     * Fetch all drone availability from the ILP REST Service
     * @return List of all drone availability at service points
     */
    public List<DroneServicePointRequest> getDroneAvailability() {
        try {
            String url = ilpEndpoint + "drones-for-service-points";
            logger.info("Fetching drone availability from: {}", url);

            DroneServicePointRequest[] availability = restTemplate.getForObject(url, DroneServicePointRequest[].class);

            if (availability == null) {
                logger.warn("No drone availability returned from ILP service");
                return List.of();
            }
            logger.info("Successfully fetched availability for {} service points", availability.length);
            return Arrays.asList(availability);
        } catch (Exception e) {
            logger.error("Error fetching drone availability from ILP service", e);
            throw new RuntimeException("Failed to fetch drone availability from ILP service", e);
        }
    }

    public List<ServicePoints> getServicePoints() {
        try {
            String url = ilpEndpoint + "service-points";
            logger.info("Fetching service points from: {}", url);

            ServicePoints[] servicePoints = restTemplate.getForObject(url, ServicePoints[].class);

            if (servicePoints == null) {
                logger.warn("No service points returned from ILP service");
                return List.of();
            }
            logger.info("Successfully fetched {} service points", servicePoints.length);
            return Arrays.asList(servicePoints);
        } catch (Exception e) {
            logger.error("Error fetching service points from ILP service", e);
            throw new RuntimeException("Failed to fetch service points from ILP service", e);
        }
    }

    public List<RestrictedArea> getRestrictedAreas() {
        try {
            String url = ilpEndpoint + "restricted-areas";
            logger.info("Fetching restricted areas from: {}", url);

            RestrictedArea[] restrictedAreas = restTemplate.getForObject(url, RestrictedArea[].class);

            if (restrictedAreas == null) {
                logger.warn("No restricted areas returned from ILP service");
                return List.of();
            }
            logger.info("Successfully fetched {} restricted areas", restrictedAreas.length);
            return Arrays.asList(restrictedAreas);
        } catch (Exception e) {
            logger.error("Error fetching restricted areas from ILP service", e);
            throw new RuntimeException("Failed to fetch restricted areas from ILP service", e);
        }
    }
}