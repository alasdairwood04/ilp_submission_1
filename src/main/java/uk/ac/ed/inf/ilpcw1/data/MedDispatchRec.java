package uk.ac.ed.inf.ilpcw1.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a query attribute for dynamic drone queries
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedDispatchRec {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("date")
    private LocalDate date; // parsed into LocalDate in service

    @JsonProperty("time")
    private LocalTime time; // parsed into LocalTime in service

    @JsonProperty("requirements")
    private Requirements requirements;
}
