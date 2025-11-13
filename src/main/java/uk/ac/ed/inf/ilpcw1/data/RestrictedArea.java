package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a restricted area
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestrictedArea {
    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("limits")
    private Limits limits;

    @JsonProperty("vertices")
    private List<LngLat> vertices;
}
