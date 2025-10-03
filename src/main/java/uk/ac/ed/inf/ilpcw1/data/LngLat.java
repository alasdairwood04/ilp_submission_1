package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;


@Data // generates getters, setters, toString, equals, and hashCode methods
@AllArgsConstructor // generates a constructor with all fields as parameters
@NoArgsConstructor // generates a no-argument constructor
public class LngLat {

    @NotNull @JsonProperty("lng")
    private double longitude;

    @NotNull @JsonProperty("lat")
    private double latitude;
}
