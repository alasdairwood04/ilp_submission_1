package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a GeoJSON LineString structure
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeoJsonLineString {
    @Builder.Default
    @JsonProperty("type")
    private String type = "LineString";

    @JsonProperty("coordinates")
    private List<List<Double>> coordinates;
}