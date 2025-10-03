package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NextPositionRequest {
    @JsonProperty("start")
    private LngLat start;

    @JsonProperty("angle")
    private Double angle;
}
