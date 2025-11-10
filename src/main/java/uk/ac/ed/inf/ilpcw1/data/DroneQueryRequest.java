package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a query attribute for dynamic drone queries
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneQueryRequest {
    @JsonProperty("attribute")
    private String attribute;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private String value;
}
