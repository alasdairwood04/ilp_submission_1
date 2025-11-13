package uk.ac.ed.inf.ilpcw1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.*;
import uk.ac.ed.inf.ilpcw1.exception.DroneNotFoundException;
import uk.ac.ed.inf.ilpcw1.exception.InvalidRequestException;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
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


    /**
     * Implements logic for 3b: Query drones by a list of dynamic attributes.
     *
     * @param queries The list of query requests from the POST body.
     * @return A list of matching drone IDs.
     */
    public List<Integer> queryDrones(List<DroneQueryRequest> queries) {
        if (queries == null || queries.isEmpty()) {
            logger.warn("Empty query attributes list");
            return List.of();
        }

        logger.info("Executing dynamic query with {} criteria", queries.size());

        // Fetch all drones once
        List<Drone> allDrones = ilpServiceClient.getAllDrones();

        // Start with a stream of all drones
        var filteredStream = allDrones.stream();

        // Apply each query as a filter
        for (DroneQueryRequest query : queries) {
            filteredStream = filteredStream.filter(drone -> matchesAttribute(drone, query));
        }

        // Collect the IDs of the filtered drones
        List<Integer> droneIds = filteredStream
                .map(Drone::getId)
                .collect(Collectors.toList());

        logger.info("Found {} drones matching all criteria", droneIds.size());
        return droneIds;
    }

    /**
     * Implements logic for 3a: Query drones by a single path variable attribute.
     * Uses query endpoint logic - everything is just '='.
     *
     * @param attributeName The attribute to check.
     * @param attributeValue The value to match (operator is always "=").
     * @return A list of matching drone IDs.
     */
    public List<Integer> queryByAttribute(String attributeName, String attributeValue) {
        logger.info("Executing dynamic path query: {} = {}", attributeName, attributeValue);
        // Create a single query request, as 3a is just 3b with one "equals" query
        DroneQueryRequest query = new DroneQueryRequest(attributeName, "=", attributeValue);
        return queryDrones(List.of(query));
    }


    /**
     * Helper method to check if a single drone matches a single query.
     * This performs the reflection, mapping, and comparison.
     */
    private boolean matchesAttribute(Drone drone, DroneQueryRequest query) {
        try {
            String attribute = query.getAttribute();
            Object actualValue = getFieldValue(drone, attribute);

            // handle nulls - checks for cooling/heating
            if (actualValue == null) {
                if (attribute.equals("cooling") || attribute.equals("heating")) {
                    actualValue = false; // Treat null/not-present as 'false'
                } else {
                    return false; // Other null fields (like 'name') cannot be matched
                }
            }

            return compareValues(actualValue, query.getOperator(), query.getValue());

        } catch (Exception e) {
            logger.warn("Failed to query attribute '{}' on drone {}: {}",
                    query.getAttribute(), drone.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Performs mapping and reflection to get a value from a drone.
     * It checks fields on Drone first, then on DroneCapability.
     */
    private Object getFieldValue(Object object, String attributeName) throws IllegalAccessException, InvalidRequestException {
        try {
            Field field = object.getClass().getDeclaredField(attributeName);
            field.setAccessible(true); // Allow access to private fields
            return field.get(object);
        } catch (NoSuchFieldException e) {
            // Mapping Logic: If not found, check for nested objects.
            // In our case, the only nested object is "capability".
            if (object instanceof Drone) {
                DroneCapability capability = ((Drone) object).getCapability();
                if (capability != null) {
                    // Recurse: try to find the field on the Capability object
                    return getFieldValue(capability, attributeName);
                }
            }
            // If it's not on the Drone or its Capability, throw InvalidRequestException
            throw new InvalidRequestException("Unknown attribute: " + attributeName);
        }
    }

    /**
     * Compares the actual value from the drone with the value from the query.
     * Handles type casting and operators as per the specification.
     */
    private boolean compareValues(Object actualValue, String operator, String queryValue) {
        // Handle boolean comparison
        if (actualValue instanceof Boolean) {
            boolean actual = (Boolean) actualValue;
            boolean query = Boolean.parseBoolean(queryValue);
            return actual == query; // Only equals operator is supported
        }

        // Handle numerical comparison
        if (actualValue instanceof Number) {
            double actual = ((Number) actualValue).doubleValue();
            double query;
            try {
                query = Double.parseDouble(queryValue);
            } catch (NumberFormatException e) {
                return false; // Cannot compare if query value is not a number
            }

            return switch (operator) {
                case "=" -> actual == query;
                case "!=" -> actual != query;
                case "<" -> actual < query;
                case ">" -> actual > query;
                default -> false; // Invalid operator for numbers
            };
        }

        // Handle string comparison
        if (actualValue instanceof String) {
            String actual = (String) actualValue;
            // Only equals operator is supported
            return Objects.equals(operator, "=") && actual.equals(queryValue);
        }

        return false;
    }

    /**
     * 4 Drone availability query
     *  @param
     */

    public List<Integer> queryAvailableDrones(List<MedDispatchRec> dispatches) {
        if (dispatches == null) {
            logger.warn("Dispatch record is null");
            return List.of();
        }

        // check that date and time are of type LocalDate and LocalTime
        for (MedDispatchRec record : dispatches) {
            if (record.getDate() == null || record.getTime() == null) {
                logger.warn("Dispatch record with id={} has null date or time", record.getId());
                return List.of();
            }
        }


        logger.info("Querying available drones for {} dispatch records", dispatches.size());

        // helper function that returns requirements which is the aggregated required capabilities
        Requirements aggregateRequirements = aggregateRequirements(dispatches);
        logger.info("Aggregated requirements: {}", aggregateRequirements);

        // fetch all drones and availability data
        List<Drone> allDrones = ilpServiceClient.getAllDrones();
        List<DroneServicePointRequest> dronesForServicePoints = ilpServiceClient.getDroneAvailability();

        // build availability map <droneID, availabilityDetails>
        Map<Integer, List<DroneAvailabilityDetails>> availabilityMap = buildAvailabilityMap(dronesForServicePoints);

        logger.info("Built availability map for {} drones", availabilityMap.size());

        logger.info("Querying available drones for {} dispatch records", dispatches.size());

        // filter drones based on availability and capabilities
        List<Integer> availableDrones = new ArrayList<>();

        for (Drone drone : allDrones) {
            if (canDroneServeAllDispatches(drone, dispatches, aggregateRequirements, availabilityMap)) {
                availableDrones.add(drone.getId());
            }
        }

        logger.info("Found {} available drones matching all dispatch requirements", availableDrones.size());
        return availableDrones;
    }

    private boolean canDroneServeAllDispatches(Drone drone, List<MedDispatchRec> dispatches,
                                               Requirements aggregatedRequirements,
                                               Map<Integer, List<DroneAvailabilityDetails>> availabilityMap) {

        // check aggregate capabilities (capacity, cooling, heating)
        if (!checkCapabilities(drone, aggregatedRequirements)) {
            logger.debug("Drone id={} failed capability check", drone.getId());
            return false;
        }

        // check availability for each dispatch date/time
        // drone must be available for all dispatches
        for (MedDispatchRec dispatch : dispatches) {
            if (!isDroneAvailableForDispatch(drone.getId(), dispatch, availabilityMap)) {
                logger.debug("Drone {} not available for dispatch {} on {} at {}",
                        drone.getId(), dispatch.getId(), dispatch.getDate(), dispatch.getTime());
                return false;
            }
        }

        // max cost constraint - will look at later
        return true;
    }

    private boolean checkCapabilities(Drone drone, Requirements requirements) {
        DroneCapability capability = drone.getCapability();
        if (capability == null) {
            logger.debug("Drone id={} has no capability data", drone.getId());
            return false;
        }

        // check capacity
        if (requirements.getCapacity() != null) {
            if (capability.getCapacity() == null ||
            capability.getCapacity() < requirements.getCapacity()) {
                logger.debug("Drone id={} failed capacity check: required={}, available={}",
                        drone.getId(), requirements.getCapacity(), capability.getCapacity());
                return false;
            }
        }

        // check cooling
        if (Boolean.TRUE.equals(requirements.getCooling())) {
            if (!Boolean.TRUE.equals(capability.getCooling())) {
                logger.debug("Drone id={} failed cooling check: required=true, available={}",
                        drone.getId(), capability.getCooling());
                return false;
            }
        }

        // check heating
        if (Boolean.TRUE.equals(requirements.getHeating())) {
            if (!Boolean.TRUE.equals(capability.getHeating())) {
                logger.debug("Drone id={} failed heating check: required=true, available={}",
                        drone.getId(), capability.getHeating());
                return false;
            }
        }

        return true;
    }

    private boolean isDroneAvailableForDispatch(Integer droneId, MedDispatchRec dispatch,
                                                Map<Integer, List<DroneAvailabilityDetails>> availabilityMap) {

        // get availability details for drone
        List<DroneAvailabilityDetails> availabilityDetails = availabilityMap.get(droneId);

        // check if drone is available for the dispatch date and time
        if (availabilityDetails == null || availabilityDetails.isEmpty()) {
            logger.debug("No availability data for drone id={}", droneId);
            return false;
        }

        // get dispatch date and time
        DayOfWeek dayOfWeek = dispatch.getDate().getDayOfWeek();
        LocalTime dispatchTime = dispatch.getTime();

        for (DroneAvailabilityDetails slot : availabilityDetails) {
            // check if the day matches
            if (slot.getDayOfWeek().equals(dayOfWeek)) {
                logger.debug("Checking availability for drone id={} on {} at {}",
                        droneId, dispatch.getDate(), dispatch.getTime());
                // check if the time falls within the available slot
                if (!dispatchTime.isBefore(slot.getFrom()) && !dispatchTime.isAfter(slot.getUntil())) {
                    logger.debug("Drone id={} is available for dispatch id={} on {} at {}",
                            droneId, dispatch.getId(), dispatch.getDate(), dispatch.getTime());
                    return true; // drone is available for this dispatch
                }
            }
        }
        logger.debug("Drone id={} is NOT available for dispatch id={} on {} at {}",
                droneId, dispatch.getId(), dispatch.getDate(), dispatch.getTime());
        return false; // drone is not available for this dispatch
    }

    // create lookup map for droneID, availability details
    private Map<Integer, List<DroneAvailabilityDetails>> buildAvailabilityMap(List<DroneServicePointRequest> allAvailabilityData) {
        Map<Integer, List<DroneAvailabilityDetails>> availabilityMap = new HashMap<>();
        for (DroneServicePointRequest servicePoint : allAvailabilityData) {
            for (DronesAtServicePoint drone : servicePoint.getDrones()) {
                availabilityMap.put(drone.getId(), drone.getAvailable());
            }
        }
        return availabilityMap;
    }

    private Requirements aggregateRequirements(List<MedDispatchRec> dispatches) {
        Requirements aggregated = new Requirements();

        double capacitySum = 0.0;
        boolean hasCapacity = false;

        double maxCostSum = 0.0;
        boolean hasMaxCost = false;

        boolean coolingRequired = false;
        boolean heatingRequired = false;

        if (dispatches == null || dispatches.isEmpty()) {
            // ensure booleans are explicit false
            aggregated.setCooling(false);
            aggregated.setHeating(false);
            return aggregated;
        }

        for (MedDispatchRec record : dispatches) {
            if (record == null) continue;
            Requirements req = record.getRequirements();
            if (req == null) continue;

            // Aggregate capacity (sum when present)
            if (req.getCapacity() != null) {
                capacitySum += req.getCapacity();
                hasCapacity = true;
            }

            // Aggregate maxCost (sum when present)
            if (req.getMaxCost() != null) {
                maxCostSum += req.getMaxCost();
                hasMaxCost = true;
            }

            // Aggregate cooling / heating (treat null as false)
            if (Boolean.TRUE.equals(req.getCooling())) {
                coolingRequired = true;
            }
            if (Boolean.TRUE.equals(req.getHeating())) {
                heatingRequired = true;
            }
        }

        if (hasCapacity) {
            aggregated.setCapacity(capacitySum);
        } else {
            aggregated.setCapacity(null);
        }

        if (hasMaxCost) {
            aggregated.setMaxCost(maxCostSum);
        } else {
            aggregated.setMaxCost(null);
        }

        aggregated.setCooling(coolingRequired);
        aggregated.setHeating(heatingRequired);

        return aggregated;
    }
}