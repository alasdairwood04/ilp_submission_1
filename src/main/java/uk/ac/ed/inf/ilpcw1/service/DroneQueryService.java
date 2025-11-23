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
    private final PathfindingService pathfindingService;
    private final RestService restService;


    @Autowired
    public DroneQueryService(ILPServiceClient ilpServiceClient, RestService restService) {
        this.ilpServiceClient = ilpServiceClient;
        this.restService = restService;
        this.pathfindingService = new PathfindingService(new RestService());
    }


    /**
     * Filter drones by cooling capability
     *
     * @param hasCooling true to get drones with cooling, false for drones without
     * @return List of drones IDs matching criteria - either having or not having cooling
     */
    public List<String> filterByCooling(boolean hasCooling) {
        logger.info("Filtering drones with cooling={}", hasCooling);

        List<Drone> allDrones = ilpServiceClient.getAllDrones();

        List<String> droneIds = allDrones.stream()
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
     *
     * @param id The drone ID
     * @return The Drone object
     * @throws DroneNotFoundException if Drone is not found
     */
    public Drone getByDroneId(String id) {
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
    public List<String> queryDrones(List<DroneQueryRequest> queries) {
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
        List<String> droneIds = filteredStream
                .map(Drone::getId)
                .collect(Collectors.toList());

        logger.info("Found {} drones matching all criteria", droneIds.size());
        return droneIds;
    }

    /**
     * Implements logic for 3a: Query drones by a single path variable attribute.
     * Uses query endpoint logic - everything is just '='.
     *
     * @param attributeName  The attribute to check.
     * @param attributeValue The value to match (operator is always "=").
     * @return A list of matching drone IDs.
     */
    public List<String> queryByAttribute(String attributeName, String attributeValue) {
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
     * Implements logic for 4: Query available drones for a list of dispatch records.
     *
     * @param dispatches List of dispatch records
     * @return List of available drone IDs
     */
    public List<String> queryAvailableDrones(List<MedDispatchRec> dispatches) {
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
        Map<String, List<DroneAvailabilityDetails>> availabilityMap = buildAvailabilityMap(dronesForServicePoints);

        logger.info("Built availability map for {} drones", availabilityMap.size());

        logger.info("Querying available drones for {} dispatch records", dispatches.size());

        // filter drones based on availability and capabilities
        List<String> availableDrones = new ArrayList<>();

        for (Drone drone : allDrones) {
            if (canDroneServeAllDispatches(drone, dispatches, aggregateRequirements, availabilityMap)) {
                availableDrones.add(drone.getId());
            }
        }

        logger.info("Found {} available drones matching all dispatch requirements", availableDrones.size());
        return availableDrones;
    }

    // Internal version of queryAvailableDrones that uses cached data
    private List<String> queryAvailableDronesInternal(
            List<MedDispatchRec> dispatches,
            Map<String, Drone> droneLookup,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap) {

        Requirements aggregateRequirements = aggregateRequirements(dispatches);
        List<String> availableDrones = new ArrayList<>();

        logger.info("The aggregated requirements are: {}", aggregateRequirements);


        for (Drone drone : droneLookup.values()) {
            if (canDroneServeAllDispatches(drone, dispatches, aggregateRequirements, availabilityMap)) {
                availableDrones.add(drone.getId());
            }
        }
        return availableDrones;
    }

    /**
     * Helper method to check if a drone can serve all dispatches
     *
     * @param drone                  used to compare its capabilities
     * @param dispatches             list of dispatch records
     * @param aggregatedRequirements the aggregated requirements from all dispatches
     * @param availabilityMap        map of drone availability details
     * @return true if drone can serve all dispatches, false otherwise
     */
    private boolean canDroneServeAllDispatches(Drone drone, List<MedDispatchRec> dispatches,
                                               Requirements aggregatedRequirements,
                                               Map<String, List<DroneAvailabilityDetails>> availabilityMap) {

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

    /**
     * Helper method to check if a drone meets the aggregated requirements
     *
     * @param drone to check
     * @param requirements aggregated requirements
     * @return true if drone meets requirements, false otherwise
     */
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

        // all checks passed
        return true;
    }

    /**
     * Helper method to check if a drone is available for a specific dispatch
     *
     * @param droneId         id of the drone
     * @param dispatch        dispatch record
     * @param availabilityMap map of drone availability details
     * @return true if drone is available for the dispatch, false otherwise
     */
    private boolean isDroneAvailableForDispatch(String droneId, MedDispatchRec dispatch,
                                                Map<String, List<DroneAvailabilityDetails>> availabilityMap) {

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

    /**
     * Helper method to build a map of drone availability
     *
     * @param allAvailabilityData list of drone availability data from service points
     * @return map of drone ID to list of availability details
     */
    private Map<String, List<DroneAvailabilityDetails>> buildAvailabilityMap(List<DroneServicePointRequest> allAvailabilityData) {
        Map<String, List<DroneAvailabilityDetails>> availabilityMap = new HashMap<>();
        for (DroneServicePointRequest servicePoint : allAvailabilityData) {
            for (DronesAtServicePoint drone : servicePoint.getDrones()) {
                availabilityMap.put(String.valueOf(drone.getId()), drone.getAvailable());
            }
        }
        return availabilityMap;
    }

    /**
     * Helper method to aggregate requirements from multiple dispatch records
     *
     * @param dispatches list of dispatch records
     * @return aggregated requirements
     */
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

    // Helper to find the nearest Service Point
    private ServicePoints findNearestServicePoint(LngLat location, Collection<ServicePoints> servicePoints) {
        return servicePoints.stream()
                .min(Comparator.comparingDouble(sp -> restService.calculateDistance(location, sp.getLocation())))
                .orElseThrow(() -> new RuntimeException("No Service Points found"));
    }

    public DeliveryPathResponse calcDeliveryPath(List<MedDispatchRec> dispatches) {
        logger.info("Calculating delivery path for {} dispatch records", dispatches.size());

        // fetch all drones
        List<Drone> allDrones = ilpServiceClient.getAllDrones();

        // fetch drone availability
        List<DroneServicePointRequest> dronesForServicePoints = ilpServiceClient.getDroneAvailability();

        // fetch restricted areas
        List<RestrictedArea> restrictedAreas = ilpServiceClient.getRestrictedAreas();

        // check if dispatch coordinate is inside a non-fly zone
        for (MedDispatchRec record : dispatches) {
            LngLat deliveryLocation = record.getDelivery();
            if (restService.isInNoFlyZone(deliveryLocation, restrictedAreas)) {
                logger.error("Dispatch ID {} has delivery location inside a no-fly zone: {}",
                        record.getId(), deliveryLocation);
                throw new InvalidRequestException("Dispatch ID " + record.getId() +
                        " has delivery location inside a no-fly zone: " + deliveryLocation);
            }
        }

        // fetch service points locations
        List<ServicePoints> servicePoints = ilpServiceClient.getServicePoints();

        // build availability map
        Map<String, List<DroneAvailabilityDetails>> availabilityMap = buildAvailabilityMap(dronesForServicePoints);

        // ID to Drone object map
        Map<String, Drone> droneLookup = allDrones.stream()
                .collect(Collectors.toMap(Drone::getId, drone -> drone));

        // ID to home service point map
        Map<String, ServicePoints> droneToServicePoint = mapDronesToServicePoints(
                droneLookup.keySet().stream().toList(),
                dronesForServicePoints,
                servicePoints
        );

        List<DronePathDetails> finalDronePaths = assignDispatchesToMultipleDrones(
                new ArrayList<>(dispatches),
                droneLookup,
                droneToServicePoint,
                restrictedAreas,
                availabilityMap, // cache of availability data
                new HashSet<>() // used drone IDs
        );

        // 4. Calculate Totals
        double totalCost = 0.0;
        int totalMoves = 0;

        for (DronePathDetails pathDetails : finalDronePaths) {
            if (pathDetails == null) continue;

            Drone drone = droneLookup.get(pathDetails.getDroneId());
            int flightMoves = 0;

            for (Deliveries delivery : pathDetails.getDeliveries()) {
                if (delivery.getFlightPath() != null) {
                    flightMoves += delivery.getFlightPath().size();
                }
            }

            // Aggregate totals
            totalMoves += flightMoves;

            // Calculate cost PER DRONE flight (Fixed Costs + Variable Costs)
            totalCost += calculateDroneCost(drone, flightMoves);
        }

        logger.info("Path calculation complete. Drones: {}, Moves: {}, Cost: {}",
                finalDronePaths.size(), totalMoves, totalCost);

        return DeliveryPathResponse.builder()
                .totalCost(totalCost)
                .totalMoves(totalMoves)
                .dronePaths(finalDronePaths)
                .build();
    }

    /**
     * Recursive method to assign dispatches to multiple drones
     *
     * @param dispatches          list of dispatch records
     * @param droneLookup         map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas     list of restricted areas (drones cant fly in)
     * @return list of DronePathDetails for assigned drones
     */
    private List<DronePathDetails> assignDispatchesToMultipleDrones(
            List<MedDispatchRec> dispatches,
            Map<String, Drone> droneLookup,
            Map<String, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap,
            Set<String> usedDroneIds) {

        logger.info("Assigning dispatches. Batch size: {}", dispatches.size());

        // base case: try to assign all dispatches to a single drone
        Optional<DronePathDetails> singleDronePath = findSingleDroneForAllDispatches(
                dispatches,
                droneLookup,
                droneToServicePoint,
                restrictedAreas,
                availabilityMap,
                usedDroneIds
        );

        if (singleDronePath.isPresent()) {
            usedDroneIds.add(singleDronePath.get().getDroneId());
            return new ArrayList<>(List.of(singleDronePath.get()));
        }

        // 2. failure base case: single item cannot be delivered
        if (dispatches.size() == 1) {
            logger.error("Dispatch ID {} cannot be delivered by any available drone (Constraint/Range/No-Fly).",
                    dispatches.get(0).getId());
            throw new RuntimeException("Undeliverable dispatch " + dispatches.get(0).getId());
        }

        // 3 constraint-based splitting - (mix of cooling/heating vs standard)
        // Check for Cooling mix
        boolean hasCooling = dispatches.stream().anyMatch(d -> Boolean.TRUE.equals(d.getRequirements().getCooling()));
        boolean hasNonCooling = dispatches.stream().anyMatch(d -> !Boolean.TRUE.equals(d.getRequirements().getCooling()));

        if (hasCooling && hasNonCooling) {
            logger.info("Splitting batch by COOLING constraint.");
            return splitByConstraint(dispatches, d -> Boolean.TRUE.equals(d.getRequirements().getCooling()),
                    droneLookup, droneToServicePoint, restrictedAreas, availabilityMap, usedDroneIds);
        }

        // Check for Heating mix (only if not already split by cooling)
        boolean hasHeating = dispatches.stream().anyMatch(d -> Boolean.TRUE.equals(d.getRequirements().getHeating()));
        boolean hasNonHeating = dispatches.stream().anyMatch(d -> !Boolean.TRUE.equals(d.getRequirements().getHeating()));

        if (hasHeating && hasNonHeating) {
            logger.info("Splitting batch by HEATING constraint.");
            return splitByConstraint(dispatches, d -> Boolean.TRUE.equals(d.getRequirements().getHeating()),
                    droneLookup, droneToServicePoint, restrictedAreas, availabilityMap, usedDroneIds);
        }

        // 4. spatial analysis - determining if dispatches are co-located or spatially distributed
        double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;

        for (MedDispatchRec d : dispatches) {
            LngLat loc = d.getDelivery();
            if (loc.getLongitude() < minLng) minLng = loc.getLongitude();
            if (loc.getLongitude() > maxLng) maxLng = loc.getLongitude();
            if (loc.getLatitude() < minLat) minLat = loc.getLatitude();
            if (loc.getLatitude() > maxLat) maxLat = loc.getLatitude();
        }

        double lngRange = maxLng - minLng;
        double latRange = maxLat - minLat;

        // threshold of 0.0015 degrees (~167 meters)
        final double COLOCATION_THRESHOLD = 0.0015;
        boolean isColocated = (lngRange < COLOCATION_THRESHOLD && latRange < COLOCATION_THRESHOLD);

        if (isColocated) {
            // CO-LOCATED CASE: Use greedy capacity-based assignment
            logger.info("Dispatches are co-located. Using greedy capacity-based assignment.");
            return greedyCapacityAssignment(
                    dispatches,
                    droneLookup,
                    droneToServicePoint,
                    restrictedAreas,
                    availabilityMap,
                    usedDroneIds
            );
        } else {
            // SPATIAL CASE: Use spatial split
            logger.info("Dispatches are spatially distributed. Using spatial split.");
            return spatialSplitAssignment(
                    dispatches,
                    droneLookup,
                    droneToServicePoint,
                    restrictedAreas,
                    availabilityMap,
                    usedDroneIds,
                    lngRange,
                    latRange
            );
        }
    }

    /**
     * Helper method to split dispatches by a given constraint
     * @param dispatches list of dispatch records
     * @param condition predicate to split by
     * @param droneLookup map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas list of restricted areas
     * @param availabilityMap map of drone availability details
     * @param usedDroneIds set of already used drone IDs
     * @return list of DronePathDetails for assigned drones
     */
    private List<DronePathDetails> splitByConstraint(
            List<MedDispatchRec> dispatches,
            java.util.function.Predicate<MedDispatchRec> condition,
            Map<String, Drone> droneLookup,
            Map<String, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap,
            Set<String> usedDroneIds) {

        List<MedDispatchRec> trueBatch = new ArrayList<>();
        List<MedDispatchRec> falseBatch = new ArrayList<>();

        for (MedDispatchRec d : dispatches) {
            if (condition.test(d)) {
                trueBatch.add(d);
            } else {
                falseBatch.add(d);
            }
        }

        List<DronePathDetails> results = new ArrayList<>();
        // recurse on both halves
        results.addAll(assignDispatchesToMultipleDrones(trueBatch, droneLookup, droneToServicePoint, restrictedAreas, availabilityMap, usedDroneIds));
        results.addAll(assignDispatchesToMultipleDrones(falseBatch, droneLookup, droneToServicePoint, restrictedAreas, availabilityMap, usedDroneIds));
        return results;
    }

    /**
     * Greedy capacity-based assignment for co-located dispatches
     * @param dispatches list of dispatch records
     * @param droneLookup map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas list of restricted areas
     * @param availabilityMap map of drone availability details
     * @param usedDroneIds set of already used drone IDs
     * @return list of DronePathDetails for assigned drones
     */
    private List<DronePathDetails> greedyCapacityAssignment(
            List<MedDispatchRec> dispatches,
            Map<String, Drone> droneLookup,
            Map<String, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap,
            Set<String> usedDroneIds) {

        List<DronePathDetails> allPaths = new ArrayList<>();
        List<MedDispatchRec> remaining = new ArrayList<>(dispatches);

        // Cache drones that cannot reach this location
        Set<String> unreachableDrones = new HashSet<>();

        // Sort dispatches by capacity (largest first) for better packing
        remaining.sort(Comparator.comparingDouble(d ->
                -(d.getRequirements().getCapacity() != null ? d.getRequirements().getCapacity() : 0.0)));

        while (!remaining.isEmpty()) {
            // Get available candidates for the first dispatch in remaining
            List<String> candidates = queryAvailableDronesInternal(
                    List.of(remaining.get(0)),
                    droneLookup,
                    availabilityMap
            ).stream()
                    .filter(id -> !usedDroneIds.contains(id))
                    .filter(id -> !unreachableDrones.contains(id)) // Filter out known unreachable drones
                    .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                logger.error("No available drones for dispatch ID {}", remaining.get(0).getId());
                throw new RuntimeException("Undeliverable dispatch " + remaining.get(0).getId());
            }

            // Sort candidates by capacity (largest first) and proximity
            LngLat targetLocation = remaining.get(0).getDelivery();
            candidates.sort((id1, id2) -> {
                // ... (sorting logic remains same) ...
                Drone d1 = droneLookup.get(id1);
                Drone d2 = droneLookup.get(id2);

                // Primary: Higher capacity first
                int capacityCompare = Double.compare(
                        d2.getCapability().getCapacity(),
                        d1.getCapability().getCapacity()
                );

                if (capacityCompare != 0) return capacityCompare;

                // Secondary: Closer service point first
                ServicePoints sp1 = droneToServicePoint.get(id1);
                ServicePoints sp2 = droneToServicePoint.get(id2);
                double dist1 = restService.calculateDistance(sp1.getLocation(), targetLocation);
                double dist2 = restService.calculateDistance(sp2.getLocation(), targetLocation);

                return Double.compare(dist1, dist2);
            });

            // Try each candidate drone and pack as many dispatches as possible
            boolean foundAssignment = false;

            for (String droneId : candidates) {
                Drone drone = droneLookup.get(droneId);
                List<MedDispatchRec> packed = new ArrayList<>();
                double accumulatedCapacity = 0.0;

                // Greedy pack: Add dispatches until we hit capacity or move limit
                for (MedDispatchRec dispatch : remaining) {
                    double reqCapacity = dispatch.getRequirements().getCapacity() != null
                            ? dispatch.getRequirements().getCapacity() : 0.0;

                    // Check if adding this dispatch would exceed drone capacity
                    if (accumulatedCapacity + reqCapacity <= drone.getCapability().getCapacity()) {
                        // Check if this dispatch is compatible with already packed ones
                        List<MedDispatchRec> testBatch = new ArrayList<>(packed);
                        testBatch.add(dispatch);

                        // Verify aggregated requirements are still met
                        Requirements aggregated = aggregateRequirements(testBatch);
                        if (checkCapabilities(drone, aggregated)) {
                            // Check availability for this specific dispatch
                            if (isDroneAvailableForDispatch(droneId, dispatch, availabilityMap)) {
                                packed.add(dispatch);
                                accumulatedCapacity += reqCapacity;
                            }
                        }
                    }
                }

                if (!packed.isEmpty()) {
                    // Try to build a route for this drone with packed dispatches
                    ServicePoints startPoint = droneToServicePoint.get(droneId);
                    DronePathDetails pathDetails = buildDroneRoute(
                            drone,
                            startPoint,
                            packed,
                            restrictedAreas
                    );

                    // Check if pathDetails is null (pathfinding failed)
                    if (pathDetails == null) {
                        logger.debug("Drone {} failed pathfinding for greedy batch. Marking as unreachable.", droneId);
                        unreachableDrones.add(droneId); // Cache this failure!
                        continue; // Skip this drone, try the next candidate
                    }

                    // Verify the route is valid (within move limits)
                    int totalMoves = pathDetails.getDeliveries().stream()
                            .mapToInt(d -> d.getFlightPath().size())
                            .sum();

                    if (totalMoves <= drone.getCapability().getMaxMoves()) {
                        // Check maxCost constraints
                        double costPerMove = drone.getCapability().getCostPerMove();
                        double fixedCost = drone.getCapability().getCostInitial()
                                + drone.getCapability().getCostFinal();
                        double totalCost = fixedCost + (costPerMove * totalMoves);
                        double costPerDispatch = totalCost / packed.size();

                        boolean meetsAllCostConstraints = packed.stream()
                                .allMatch(d -> d.getRequirements().getMaxCost() == null ||
                                        costPerDispatch <= d.getRequirements().getMaxCost());

                        if (meetsAllCostConstraints) {
                            logger.info("Drone {} successfully packed {} dispatches ({}kg / {}kg capacity)",
                                    droneId, packed.size(), accumulatedCapacity,
                                    drone.getCapability().getCapacity());

                            allPaths.add(pathDetails);
                            usedDroneIds.add(droneId);
                            remaining.removeAll(packed);
                            foundAssignment = true;
                            break; // Move to next batch
                        }
                    }
                }
            }

            if (!foundAssignment) {
                // ... (error handling) ...
                logger.error("Could not find suitable drone for dispatch {}", remaining.get(0).getId());
                throw new RuntimeException("Undeliverable dispatch " + remaining.get(0).getId());
            }
        }

        return allPaths;
    }

    /**
     * Spatial split assignment for distributed dispatches
     * @param dispatches list of dispatch records
     * @param droneLookup map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas list of restricted areas
     * @param availabilityMap map of drone availability details
     * @param usedDroneIds set of already used drone IDs
     * @param lngRange range of longitudes
     * @param latRange range of latitudes
     * @return list of DronePathDetails for assigned drones
     */
    private List<DronePathDetails> spatialSplitAssignment(
            List<MedDispatchRec> dispatches,
            Map<String, Drone> droneLookup,
            Map<String, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap,
            Set<String> usedDroneIds,
            double lngRange,
            double latRange) {

        List<MedDispatchRec> sortedDispatches = new ArrayList<>(dispatches);
        boolean splitByLongitude = lngRange >= latRange;

        if (splitByLongitude) {
            sortedDispatches.sort(Comparator.comparingDouble(d -> d.getDelivery().getLongitude()));
        } else {
            sortedDispatches.sort(Comparator.comparingDouble(d -> d.getDelivery().getLatitude()));
        }

        int splitIndex = findBestSplitPoint(sortedDispatches, lngRange, latRange);

        // Split
        List<MedDispatchRec> leftBatch = sortedDispatches.subList(0, splitIndex);
        List<MedDispatchRec> rightBatch = sortedDispatches.subList(splitIndex, sortedDispatches.size());

        // Recurse
        List<DronePathDetails> leftResults = assignDispatchesToMultipleDrones(
                leftBatch, droneLookup, droneToServicePoint, restrictedAreas,
                availabilityMap, usedDroneIds);

        List<DronePathDetails> rightResults = assignDispatchesToMultipleDrones(
                rightBatch, droneLookup, droneToServicePoint, restrictedAreas,
                availabilityMap, usedDroneIds);

        // Combine
        List<DronePathDetails> combined = new ArrayList<>();
        combined.addAll(leftResults);
        combined.addAll(rightResults);

        return combined;
    }
    /**
     * Helper method to find a single drone that can handle all dispatches
     *
     * @param dispatches          list of dispatch records
     * @param droneLookup         map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas     list of restricted areas (drones cant fly in)
     * @return Optional of DronePathDetails if a single drone can handle all dispatches, empty otherwise
     */
    private Optional<DronePathDetails> findSingleDroneForAllDispatches(
            List<MedDispatchRec> dispatches,
            Map<String, Drone> droneLookup,
            Map<String, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas,
            Map<String, List<DroneAvailabilityDetails>> availabilityMap,
            Set<String> ignoredDroneIds) {

        // 1. get candidates based on capabilities and time availability
        List<String> candidateDrones = queryAvailableDronesInternal(dispatches, droneLookup, availabilityMap)
                .stream()
                .filter(id -> !ignoredDroneIds.contains(id))
                .collect(Collectors.toList());

        logger.info("Found {} candidate drones for all dispatches", candidateDrones.size());
        logger.info("The candidate drone IDs are: {}", candidateDrones);

        if (candidateDrones.isEmpty() || dispatches.isEmpty()) {
            return Optional.empty();
        }

        // 2. pre-calculate route metrics for sorting
        LngLat firstTarget = dispatches.get(0).getDelivery();
        LngLat lastTarget = dispatches.get(dispatches.size() - 1).getDelivery();

        // calculate the internal distance of the route
        double internalRouteDist = 0.0;
        for (int i = 0; i < dispatches.size() - 1; i++) {
            internalRouteDist += restService.calculateDistance(
                    dispatches.get(i).getDelivery(),
                    dispatches.get(i + 1).getDelivery()
            );
        }
        final double routeFixedDist = internalRouteDist;

        // find the closest ServicePoint to the START of the dispatch list
        ServicePoints globalNearestSP = findNearestServicePoint(
                firstTarget,
                droneToServicePoint.values()
        );

        // 3. sort candidates to check "Cheapest & Closest" drones first
        candidateDrones.sort((id1, id2) -> {
            Drone drone1 = droneLookup.get(id1);
            Drone drone2 = droneLookup.get(id2);
            ServicePoints sp1 = droneToServicePoint.get(id1);
            ServicePoints sp2 = droneToServicePoint.get(id2);

            // Metric 1: locality to the specific Service Point closest to the pickup
            boolean isLocal1 = sp1.getId().equals(globalNearestSP.getId());
            boolean isLocal2 = sp2.getId().equals(globalNearestSP.getId());
            if (isLocal1 != isLocal2) return isLocal1 ? -1 : 1;

            // Metric 2: Estimated Total Cost
            double dist1 = restService.calculateDistance(sp1.getLocation(), firstTarget)
                    + routeFixedDist
                    + restService.calculateDistance(lastTarget, sp1.getLocation());

            double dist2 = restService.calculateDistance(sp2.getLocation(), firstTarget)
                    + routeFixedDist
                    + restService.calculateDistance(lastTarget, sp2.getLocation());

            double estMoves1 = dist1 / 0.00015;
            double estMoves2 = dist2 / 0.00015;

            double cost1 = (drone1.getCapability().getCostInitial() + drone1.getCapability().getCostFinal())
                    + (estMoves1 * drone1.getCapability().getCostPerMove());

            double cost2 = (drone2.getCapability().getCostInitial() + drone2.getCapability().getCostFinal())
                    + (estMoves2 * drone2.getCapability().getCostPerMove());

            int costCompare = Double.compare(cost1, cost2);
            if (costCompare != 0) return costCompare;

            // Metric 3: euclidean Proximity (Tie-breaker)
            return Double.compare(dist1, dist2);
        });

        logger.info("Evaluating {} candidate drones for {} dispatches", candidateDrones.size(), dispatches.size());

        // 4. try pathfinding on sorted candidates
        for (String droneId : candidateDrones) {
            logger.info("Evaluating drone {}", droneId);
            Drone drone = droneLookup.get(droneId);
            ServicePoints startPoint = droneToServicePoint.get(droneId);

            // euclidean Check
            double distOut = restService.calculateDistance(startPoint.getLocation(), firstTarget);
            double distBack = restService.calculateDistance(lastTarget, startPoint.getLocation());
            double minEuclideanDistance = distOut + routeFixedDist + distBack;
            int minPossibleMoves = (int) Math.ceil(minEuclideanDistance / 0.00015);

            if (minPossibleMoves > drone.getCapability().getMaxMoves()) {
                logger.debug("Skipping drone {}: Range insufficient (Min moves {} > Max {})",
                        droneId, minPossibleMoves, drone.getCapability().getMaxMoves());
                continue;
            }

            // build route
            logger.debug("Attempting route with drone {}", droneId);
            DronePathDetails pathDetails = buildDroneRoute(
                    drone,
                    startPoint,
                    dispatches,
                    restrictedAreas
            );

            if (pathDetails == null) {
                logger.debug("Drone {} failed pathfinding (unreachable). Skipping.", droneId);
                continue; // Pathfinding failed (unreachable)
            }

            // validate Moves
            int totalMoves = pathDetails.getDeliveries().stream()
                    .mapToInt(d -> d.getFlightPath().size())
                    .sum();

            if (totalMoves <= drone.getCapability().getMaxMoves()) {
                // Validate Max Cost Constraints
                double cost = calculateDroneCost(drone, totalMoves);
                double costPerDispatch = cost / dispatches.size();

                boolean costOk = dispatches.stream()
                        .allMatch(d -> d.getRequirements().getMaxCost() == null ||
                                costPerDispatch <= d.getRequirements().getMaxCost());

                if (costOk) {
                    logger.info("Selected drone {} (Cost: {}, Moves: {})", droneId, cost, totalMoves);
                    return Optional.of(pathDetails);
                } else {
                    logger.debug("Drone {} valid path but too expensive ({})", droneId, costPerDispatch);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Finds the best split point in a sorted list of dispatches based on gaps
     *
     * @param sorted   list of sorted dispatch records
     * @param lngRange range of longitudes
     * @param latRange range of latitudes
     * @return index to split the list at
     */
    private int findBestSplitPoint(List<MedDispatchRec> sorted, double lngRange, double latRange) {
        if (sorted.size() <= 2) return 1;

        double maxGapScore = 0;
        int bestSplit = sorted.size() / 2;  // default to middle

        boolean isLongitude = lngRange >= latRange;

        // look for the biggest gap near the middle
        for (int i = 1; i < sorted.size(); i++) {
            double gap = isLongitude
                    ? sorted.get(i).getDelivery().getLongitude() -
                    sorted.get(i - 1).getDelivery().getLongitude()
                    : sorted.get(i).getDelivery().getLatitude() -
                    sorted.get(i - 1).getDelivery().getLatitude();

            // normalize gap by total range
            double normalizedGap = gap / (isLongitude ? lngRange : latRange);

            // prefer splits near middle (penalty for extreme splits)
            double distanceFromMiddle = Math.abs(i - sorted.size() / 2.0);
            double balancePenalty = 1.0 - (distanceFromMiddle / sorted.size());

            // combined score: larger gaps near middle are best
            double score = normalizedGap * balancePenalty;

            if (score > maxGapScore) {
                maxGapScore = score;
                bestSplit = i;
            }
        }

        logger.info("Split at index {} (gap score: {})", bestSplit, maxGapScore);
        return bestSplit;
    }


    /**
     * creates a flight path for a drone (SP -> D1 -> D2 -> ... -> SP) with hover points
     *
     * @param drone drone to use
     * @param startPoint starting service point
     * @param dispatches list of dispatch records
     * @param restrictedAreas list of restricted areas
     * @return DronePathDetails with flight paths, or null if pathfinding fails
     */
    private DronePathDetails buildDroneRoute(
            Drone drone,
            ServicePoints startPoint,
            List<MedDispatchRec> dispatches,
            List<RestrictedArea> restrictedAreas) {

        List<Deliveries> deliveries = new ArrayList<>();
        LngLat currentPos = startPoint.getLocation();

        // 1. process all dispatches
        for (MedDispatchRec dispatch : dispatches) {
            LngLat deliveryLocation = dispatch.getDelivery();

            // find path
            List<LngLat> pathToDelivery = pathfindingService.findPath(
                    currentPos,
                    deliveryLocation,
                    restrictedAreas
            );

            if (pathToDelivery == null || pathToDelivery.isEmpty()) {
                return null;
            }

            // create full path list
            List<LngLat> fullPath = new ArrayList<>(pathToDelivery);

            // add Hover
            LngLat arrivedPos = fullPath.get(fullPath.size() - 1);
            fullPath.add(arrivedPos);

            // add delivery record
            deliveries.add(Deliveries.builder()
                    .deliveryId(dispatch.getId())
                    .flightPath(fullPath)
                    .build());

            // update current position for the next leg
            currentPos = arrivedPos;
        }

        // 2. Handle Return Leg
        List<LngLat> returnPath = pathfindingService.findPath(
                currentPos,
                startPoint.getLocation(),
                restrictedAreas
        );

        // If the drone can't get home, the route is invalid.
        if (returnPath == null || returnPath.isEmpty()) {
            return null;
        }

        List<LngLat> fullReturnPath = new ArrayList<>(returnPath);

        // add Hover at Service Point
        LngLat arrivedAtService = fullReturnPath.get(fullReturnPath.size() - 1);
        fullReturnPath.add(arrivedAtService);

        // add return leg
        deliveries.add(Deliveries.builder()
                .deliveryId(null)
                .flightPath(fullReturnPath)
                .build());

        return DronePathDetails.builder()
                .droneId(drone.getId())
                .deliveries(deliveries)
                .build();
    }


    /**
     * Calculates the total cost for a drone given the number of moves
     *
     * @param drone drone to calculate cost for
     * @param moves number of moves made
     * @return total cost
     */
    private double calculateDroneCost(Drone drone, int moves) {
        DroneCapability cap = drone.getCapability();
        return cap.getCostInitial() + cap.getCostFinal() + (moves * cap.getCostPerMove());
    }

    /**
     * Calculates the pro-rata cost per delivery for a drone
     *
     * @param drone        drone to calculate cost for
     * @param totalMoves   total number of moves made
     * @param numDeliveries number of deliveries made
     * @return pro-rata cost per delivery
     */
    private double calculateProRataCost(Drone drone, int totalMoves, int numDeliveries) {
        double totalCost = calculateDroneCost(drone, totalMoves);
        return totalCost / numDeliveries;
    }


    /**
     * Checks if the pro-rata cost violates any maxCost constraints
     *
     * @param newProRataCost calculated pro-rata cost
     * @param existingRoute  list of existing dispatch records
     * @param newDispatch    new dispatch record to check
     * @return true if any maxCost constraint is violated, false otherwise
     */
    private boolean violatesMaxCost(double newProRataCost,
                                    List<MedDispatchRec> existingRoute,
                                    MedDispatchRec newDispatch) {
        // Check existing dispatches
        for (MedDispatchRec dispatch : existingRoute) {
            Double maxCost = dispatch.getRequirements().getMaxCost();
            if (maxCost != null && newProRataCost > maxCost) {
                return true;
            }
        }

        // Check new dispatch
        Double maxCost = newDispatch.getRequirements().getMaxCost();
        if (maxCost != null && newProRataCost > maxCost) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to map drones to their service points
     *
     * @param droneIds               list of drone IDs
     * @param dronesForServicePoints location of drones at service points
     * @param servicePoints          list of service points
     * @return map of drone ID to ServicePoints
     */
    private Map<String, ServicePoints> mapDronesToServicePoints(List<String> droneIds,
                                                                List<DroneServicePointRequest> dronesForServicePoints,
                                                                List<ServicePoints> servicePoints) {
        Map<String, ServicePoints> droneServicePointMap = new HashMap<>();

        // build a map of service point ID to ServicePoints object for quick lookup
        Map<Integer, ServicePoints> servicePointMap = servicePoints.stream()
                .collect(Collectors.toMap(ServicePoints::getId, sp -> sp));

        for (String droneId : droneIds) {
            for (DroneServicePointRequest dsp : dronesForServicePoints) {
                for (DronesAtServicePoint droneAtSP : dsp.getDrones()) {
                    if (droneAtSP.getId().equals(droneId)) {
                        ServicePoints sp = servicePointMap.get(dsp.getServicePointId());
                        if (sp != null) {
                            droneServicePointMap.put(droneId, sp);
                        }
                    }
                }
            }
        }

        return droneServicePointMap;
    }

    /**
     * Calculates the delivery path for a list of dispatches as a GeoJSON LineString
     * @param dispatches list of dispatch records
     * @return GeoJsonLineString representing the delivery path
     */
        public GeoJsonLineString calcDeliveryPathAsGeoJson(List<MedDispatchRec> dispatches) {
        logger.info("Calculating GeoJSON path for {} dispatch records", dispatches.size());

        // 1. Fetch all necessary data
        List<Drone> allDrones = ilpServiceClient.getAllDrones();
        List<DroneServicePointRequest> dronesForServicePoints = ilpServiceClient.getDroneAvailability();
        List<ServicePoints> servicePoints = ilpServiceClient.getServicePoints();
        List<RestrictedArea> restrictedAreas = ilpServiceClient.getRestrictedAreas();

        // 2. Prepare Maps (reuse existing helpers)
        Map<String, List<DroneAvailabilityDetails>> availabilityMap = buildAvailabilityMap(dronesForServicePoints);
        Map<String, Drone> droneLookup = allDrones.stream()
                .collect(Collectors.toMap(Drone::getId, drone -> drone));
        Map<String, ServicePoints> droneToServicePoint = mapDronesToServicePoints(
                droneLookup.keySet().stream().toList(),
                dronesForServicePoints,
                servicePoints
        );

        // 3. Find a single drone for all dispatches
        // passing an empty set for ignoredDroneIds as we want to consider all drones
        Optional<DronePathDetails> pathResult = findSingleDroneForAllDispatches(
                dispatches,
                droneLookup,
                droneToServicePoint,
                restrictedAreas,
                availabilityMap,
                new HashSet<>()
        );

        // 4. Handle Failure
        if (pathResult.isEmpty()) {
            logger.warn("No single drone could be found to execute the given dispatches for GeoJSON.");
            throw new InvalidRequestException("No single drone available to handle all dispatches in a single sequence");
        }

        // 5. Convert Path to GeoJSON Coordinates
        List<List<Double>> coordinates = new ArrayList<>();
        DronePathDetails details = pathResult.get();

        if (details.getDeliveries() != null) {
            for (Deliveries delivery : details.getDeliveries()) {
                if (delivery.getFlightPath() != null) {
                    for (LngLat point : delivery.getFlightPath()) {
                        // GeoJSON uses [longitude, latitude] format
                        coordinates.add(List.of(point.getLongitude(), point.getLatitude()));
                    }
                }
            }
        }
        return GeoJsonLineString.builder()
                .coordinates(coordinates)
                .build();

    }
}