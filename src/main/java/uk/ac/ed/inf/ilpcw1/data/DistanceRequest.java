package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // generates getters, setters, toString, equals, and hashCode methods
@AllArgsConstructor // generates a constructor with all fields as parameters
@NoArgsConstructor // generates a no-argument constructor
public class DistanceRequest {
    @JsonProperty("position1")
    private LngLat position1;

    @JsonProperty("position2")
    private LngLat position2;
}
