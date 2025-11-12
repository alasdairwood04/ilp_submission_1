package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents requirements for medical dispatch
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Requirements {
    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("maxCost")
    private Double maxCost;
}
