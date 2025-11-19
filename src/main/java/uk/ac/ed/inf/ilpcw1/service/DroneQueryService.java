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
     * Implements logic for 4: Query available drones for a list of dispatch records.
     * @param dispatches List of dispatch records
     * @return List of available drone IDs
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


    /**
     * Helper method to check if a drone can serve all dispatches
     * @param drone used to compare its capabilities
     * @param dispatches list of dispatch records
     * @param aggregatedRequirements the aggregated requirements from all dispatches
     * @param availabilityMap map of drone availability details
     * @return true if drone can serve all dispatches, false otherwise
     */
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

    /**
     * Helper method to check if a drone meets the aggregated requirements
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
     * @param droneId id of the drone
     * @param dispatch dispatch record
     * @param availabilityMap map of drone availability details
     * @return true if drone is available for the dispatch, false otherwise
     */
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

    /**
     * Helper method to build a map of drone availability
     * @param allAvailabilityData list of drone availability data from service points
     * @return map of drone ID to list of availability details
     */
    private Map<Integer, List<DroneAvailabilityDetails>> buildAvailabilityMap(List<DroneServicePointRequest> allAvailabilityData) {
        Map<Integer, List<DroneAvailabilityDetails>> availabilityMap = new HashMap<>();
        for (DroneServicePointRequest servicePoint : allAvailabilityData) {
            for (DronesAtServicePoint drone : servicePoint.getDrones()) {
                availabilityMap.put(drone.getId(), drone.getAvailable());
            }
        }
        return availabilityMap;
    }

    /**
     * Helper method to aggregate requirements from multiple dispatch records
     * @param dispatches
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


    public DeliveryPathResponse calcDeliveryPath(List<MedDispatchRec> dispatches) {
        logger.info("Calculating delivery path for {} dispatch records", dispatches.size());

        // fetch all drones
        List<Drone> allDrones = ilpServiceClient.getAllDrones();
        logger.info("Fetched {} drones", allDrones.size());

        // fetch drone availability
        List<DroneServicePointRequest> dronesForServicePoints = ilpServiceClient.getDroneAvailability();
        logger.info("Fetched drone availability for {} service points", dronesForServicePoints.size());

        // fetch service points locations
        List<ServicePoints> servicePoints = ilpServiceClient.getServicePoints();
        logger.info("Fetched {} service points", servicePoints.size());

        // fetch restricted areas
        List<RestrictedArea> restrictedAreas = ilpServiceClient.getRestrictedAreas();
        logger.info("Fetched {} restricted areas", restrictedAreas.size());


        // build availability map <droneID, availabilityDetails>
        Map<Integer, List<DroneAvailabilityDetails>> availabilityMap = buildAvailabilityMap(dronesForServicePoints);

        // ID to Drone object map
        Map<Integer, Drone> droneLookup = allDrones.stream()
                .collect(Collectors.toMap(Drone::getId, drone -> drone));

        // ID to home service point map
        Map<Integer, ServicePoints> droneToServicePoint = mapDronesToServicePoints(
                droneLookup.keySet().stream().toList(),
                dronesForServicePoints,
                servicePoints
        );


        // using a set for easy removal of assigned dispatches
        Set<MedDispatchRec> unassignedDispatches = new LinkedHashSet<>(dispatches);
        List<DronePathDetails> finalDronePaths = new ArrayList<>();


        // Simple case - one drone can handle all dispatches
        logger.info("Checking if a single drone can handle all dispatches");

        Optional<DronePathDetails> singleDronePathOpt = findSingleDroneForAllDispatches(
                new ArrayList<>(unassignedDispatches),
                droneLookup,
                droneToServicePoint,
                restrictedAreas
        );

        if (singleDronePathOpt.isPresent()) {
            logger.info("Simple case successful: single drone handles all dispatches");
            finalDronePaths.add(singleDronePathOpt.get());
            unassignedDispatches.clear();
        } else {
            // Phase 3: Complex Case - Multi-drone assignment
            logger.info("Simple case failed, proceeding to complex multi-drone assignment");
            finalDronePaths = assignDispatchesToMultipleDrones(
                    unassignedDispatches,
                    droneLookup,
                    availabilityMap,
                    droneToServicePoint,
                    restrictedAreas
            );
        }

        logger.info("Assigned dispatches to {} drones", finalDronePaths.size());


        // 4. Calculate Totals (The Fix)
        double totalCost = 0.0;
        int totalMoves = 0;

        for (DronePathDetails pathDetails : finalDronePaths) {
            if (pathDetails == null) continue;

            Drone drone = droneLookup.get(pathDetails.getDroneId());
            int flightMoves = 0;

            for (Deliveries delivery : pathDetails.getDeliveries()) {
                // CRITICAL FIX:
                // A list [A, B, B] represents: Move A->B (1) + Hover (2) = 3 moves.
                // The list size is 3. Therefore, moves = size().
                // Do NOT subtract 1.
                if (delivery.getFlightPath() != null) {
                    flightMoves += delivery.getFlightPath().size();
                }
            }

            // Aggregate totals
            totalMoves += flightMoves;

            // Calculate cost PER DRONE flight (Fixed Costs + Variable Costs)
            // Assuming calculateDroneCost handles (Initial + Final + (moves * perMove))
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
     * Helper method to find a single drone that can handle all dispatches
     * @param dispatches list of dispatch records
     * @param droneLookup map of drone ID to Drone object
     * @param droneToServicePoint map of drone ID to service point
     * @param restrictedAreas list of restricted areas (drones cant fly in)
     * @return Optional of DronePathDetails if a single drone can handle all dispatches, empty otherwise
     */
    private Optional<DronePathDetails> findSingleDroneForAllDispatches(
            List<MedDispatchRec> dispatches,
            Map<Integer, Drone> droneLookup,
            Map<Integer, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas) {


        // 1. Get candidates
        List<Integer> candidateDrones = queryAvailableDrones(dispatches);

        // 2. OPTIMIZATION: Sort candidates to check "best" drones first
        if (!dispatches.isEmpty()) {
            // We use the first dispatch location as the target for our proximity heuristic
            // (Minimizing the approach flight is usually the biggest variable factor)
            LngLat targetLocation = dispatches.getFirst().getDelivery();

            candidateDrones.sort((id1, id2) -> {
                Drone drone1 = droneLookup.get(id1);
                Drone drone2 = droneLookup.get(id2);
                ServicePoints sp1 = droneToServicePoint.get(id1);
                ServicePoints sp2 = droneToServicePoint.get(id2);

                // Factor 1: Proximity (Distance from Base to First Order)
                // We want the drone closest to the start point (Ascending order)
                double dist1 = restService.calculateDistance(sp1.getLocation(), targetLocation);
                double dist2 = restService.calculateDistance(sp2.getLocation(), targetLocation);
                int distanceComparison = Double.compare(dist1, dist2);

                if (distanceComparison != 0) {
                    return distanceComparison;
                }

                // Factor 2: Capacity (Max Moves)
                // If distances are roughly equal, prefer the drone with MORE range (Descending order)
                // This increases the likelihood of completing a long path.
                return Integer.compare(
                        drone2.getCapability().getMaxMoves(),
                        drone1.getCapability().getMaxMoves()
                );
            });
        }

        logger.info("Found {} candidate drones. Sorted by proximity to dispatch #{}",
                candidateDrones.size(),
                dispatches.isEmpty() ? "?" : dispatches.get(0).getId());

        logger.info("The candidate drone IDs are: {}", candidateDrones);

        // 3. Try each candidate drone
        for (Integer droneId : candidateDrones) {
            Drone drone = droneLookup.get(droneId);
            ServicePoints startPoint = droneToServicePoint.get(droneId);

            if (startPoint == null) {
                logger.warn("No service point found for drone {}", droneId);
                continue;
            }

            // Try to build a route with this drone
            DronePathDetails pathDetails = buildDroneRoute(
                    drone,
                    startPoint,
                    dispatches,
                    restrictedAreas
            );

            logger.info("Built route for drone {}: {} deliveries", droneId,
                    pathDetails != null ? pathDetails.getDeliveries().size() : 0);

            if (pathDetails.getDeliveries().size() != dispatches.size() + 1) {
                logger.debug("Drone {} could not find paths for all dispatches. Skipping.", droneId);
                continue;
            }

            // calculate total moves - size - 1 steps + 1 extra for hover at each delivery
            int totalMoves = pathDetails.getDeliveries().stream()
                    .mapToInt(d -> d.getFlightPath().size())
                    .sum();


            if (totalMoves <= drone.getCapability().getMaxMoves()) {

                double costPerMove = drone.getCapability().getCostPerMove();
                double fixedCost = drone.getCapability().getCostInitial() + drone.getCapability().getCostFinal();
                double totalCost = fixedCost + (costPerMove * totalMoves);

                // pro-rota cost (total cost / number of dispatches)
                double costPerDispatch = totalCost / dispatches.size();

                boolean meetsAllCostConstraints = dispatches.stream()
                        .allMatch(d -> d.getRequirements().getMaxCost() == null ||
                                costPerDispatch <= d.getRequirements().getMaxCost());

                if (meetsAllCostConstraints) {
                    logger.info("Single drone {} can handle all dispatches", droneId);
                    return Optional.of(pathDetails);
                }
            }
        }
        return Optional.empty();
    }


    /**
     * creates a flight path for a drone (SP -> D1 -> D2 -> ... -> SP) with hover points
     * @param drone
     * @param startPoint
     * @param dispatches
     * @param restrictedAreas
     * @return
     */
    private DronePathDetails buildDroneRoute(
            Drone drone,
            ServicePoints startPoint,
            List<MedDispatchRec> dispatches,
            List<RestrictedArea> restrictedAreas) {

        List<Deliveries> deliveries = new ArrayList<>();
        LngLat currentPos = startPoint.getLocation();

        // 1. Process all dispatches
        for (MedDispatchRec dispatch : dispatches) {
            // Assumption: dispatch.getDelivery() returns the target LngLat
            LngLat deliveryLocation = dispatch.getDelivery();

            // Find path
            List<LngLat> pathToDelivery = pathfindingService.findPath(
                    currentPos,
                    deliveryLocation,
                    restrictedAreas
            );

//            logger.info("Path to delivery {}: {}", dispatch.getId(), pathToDelivery);

            // Safety check if no path found (handle as needed, here assuming valid)
            if (pathToDelivery == null || pathToDelivery.isEmpty()) continue;

            // Create full path list
            List<LngLat> fullPath = new ArrayList<>(pathToDelivery);

            // Add Hover: Append the LAST point of the path again.
            // This creates the pair [..., ArrivedPos, ArrivedPos] which represents the hover.
            // Note: We use the actual arrived position to ensure path continuity.
            LngLat arrivedPos = fullPath.get(fullPath.size() - 1);
            fullPath.add(arrivedPos);

            // Add delivery record
            deliveries.add(Deliveries.builder()
                    .deliveryId(dispatch.getId())
                    .flightPath(fullPath)
                    .build());

            // Update current position for the next leg
            currentPos = arrivedPos;
        }

        // 2. Handle Return Leg as a distinct "Delivery"
        List<LngLat> returnPath = pathfindingService.findPath(
                currentPos,
                startPoint.getLocation(),
                restrictedAreas
        );

        if (returnPath != null && !returnPath.isEmpty()) {
            List<LngLat> fullReturnPath = new ArrayList<>(returnPath);

            // Add Hover at Service Point (required by spec for the return leg too)
            LngLat arrivedAtService = fullReturnPath.get(fullReturnPath.size() - 1);
            fullReturnPath.add(arrivedAtService);

            // Add as a distinct item with null ID
            deliveries.add(Deliveries.builder()
                    .deliveryId(null) // Explicitly null for return leg
                    .flightPath(fullReturnPath)
                    .build());
        }

        return DronePathDetails.builder()
                .droneId(drone.getId())
                .deliveries(deliveries)
                .build();
    }

    private List<DronePathDetails> assignDispatchesToMultipleDrones(
            Set<MedDispatchRec> unassignedDispatches,
            Map<Integer, Drone> droneLookup,
            Map<Integer, List<DroneAvailabilityDetails>> availabilityMap,
            Map<Integer, ServicePoints> droneToServicePoint,
            List<RestrictedArea> restrictedAreas) {

        // Placeholder implementation - returns empty list
        return new ArrayList<>();
    }

        private double calculateDroneCost(Drone drone, int moves) {
            DroneCapability cap = drone.getCapability();
            return cap.getCostInitial() + cap.getCostFinal() + (moves * cap.getCostPerMove());
        }

        private double calculateProRataCost(Drone drone, int totalMoves, int numDeliveries) {
            double totalCost = calculateDroneCost(drone, totalMoves);
            return totalCost / numDeliveries;
        }

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
     * @param droneIds list of drone IDs
     * @param dronesForServicePoints location of drones at service points
     * @param servicePoints list of service points
     * @return map of drone ID to ServicePoints
     */
    private Map<Integer, ServicePoints> mapDronesToServicePoints(List<Integer> droneIds,
                                                                 List<DroneServicePointRequest> dronesForServicePoints,
                                                                 List<ServicePoints> servicePoints) {
        Map<Integer, ServicePoints> droneServicePointMap = new HashMap<>();

        // build a map of service point ID to ServicePoints object for quick lookup
        Map<Integer, ServicePoints> servicePointMap = servicePoints.stream()
                .collect(Collectors.toMap(ServicePoints::getId, sp -> sp));

        for (Integer droneId : droneIds) {
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
}