package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Represents a query attribute for dynamic drone queries
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DronePathDetails {
    @JsonProperty("droneId")
    private Integer droneId;

    @JsonProperty("deliveries")
    private List<Deliveries> deliveries;
}
