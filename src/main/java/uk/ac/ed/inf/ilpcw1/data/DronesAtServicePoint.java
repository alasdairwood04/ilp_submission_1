package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request for drone availability
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DronesAtServicePoint {
    @JsonProperty("id")
    private String id;

    @JsonProperty("availability")
    private List<DroneAvailabilityDetails> available;
}
