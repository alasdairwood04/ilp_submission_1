package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
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
    private DayOfWeek dayOfWeek;

    @JsonProperty("from")
    private LocalTime from;

    @JsonProperty("until")
    private LocalTime until;
}
