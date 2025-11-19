package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import uk.ac.ed.inf.ilpcw1.data.DroneCapability;

/*
 * Represents a drone with its capabilities
*/


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Drone {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("capability")
    private DroneCapability capability;

}
