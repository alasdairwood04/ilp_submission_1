package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents drone availability details
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneAvailabilityDetails {
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;

    @JsonProperty("from")
    private String from;

    @JsonProperty("until")
    private String until;
}
