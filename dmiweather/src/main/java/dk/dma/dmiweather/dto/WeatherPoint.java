package dk.dma.dmiweather.dto;

import dk.dma.common.dto.JSonWarning;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.Instant;

/**
 * A sub result in a RTZ route weather request, contains either data or an error
 * @author Klaus Groenbake
 *         Created 05/04/2017.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class WeatherPoint {
    private Instant forecastTime;
    private Instant wayPointTime;

    private GridDataPoint data;
    private JSonWarning error;

}
