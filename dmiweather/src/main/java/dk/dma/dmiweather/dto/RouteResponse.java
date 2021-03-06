package dk.dma.dmiweather.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Response to a weather request with an RTZ route
 * @author Klaus Groenbaek
 *         Created 05/04/2017.
 */
@Data
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class RouteResponse {
    List<WeatherPoint> wayPoints;
}
