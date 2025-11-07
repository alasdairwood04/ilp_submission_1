package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Represents the capabilities of a drone
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneCapability {
    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("maxMoves")
    private Integer maxMoves;

    @JsonProperty("costPerMove")
    private Double costPerMove;

    @JsonProperty("costInitial")
    private Double costInitial;

    @JsonProperty("costFinal")
    private Double costFinal;

}
