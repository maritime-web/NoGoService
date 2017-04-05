package dk.dma.dmiweather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dk.dma.common.dto.JSonWarning;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * A sub result in a RTZ route weather request, contains either data or an error
 * @author Klaus Groenbake
 *         Created 05/04/2017.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WeatherPoint {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    Instant time;

    GridDataPoint data;
    JSonWarning error;

}
