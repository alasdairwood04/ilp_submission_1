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
public class DeliveryPathResponse {
    @JsonProperty("totalCost")
    private Double totalCost;

    @JsonProperty("totalMoves")
    private Integer totalMoves;

    @JsonProperty("dronePaths")
    private List<DronePathDetails> dronePaths;
}
