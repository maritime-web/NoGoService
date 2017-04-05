package dk.dma.dmiweather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dk.dma.common.dto.JSonWarning;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    private Instant forecastTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    private Instant wayPointTime;

    private GridDataPoint data;
    private JSonWarning error;

}
