package dk.dma.dmiweather.controller;

import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.dto.JSonWarning;
import dk.dma.dmiweather.dto.*;
import dk.dma.dmiweather.generated.Route;
import dk.dma.dmiweather.generated.Schedule;
import dk.dma.dmiweather.generated.ScheduleElement;
import dk.dma.dmiweather.generated.Waypoint;
import dk.dma.dmiweather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

/**
 * @author Klaus Groenbaek
 *         Created 05/04/17.
 */
@Controller
public class RouteWeatherController {

    private final WeatherService service;

    @Autowired
    public RouteWeatherController(WeatherService service) {
        this.service = service;
    }

    @ResponseBody
    @PostMapping("/rtz")
    public RouteResponse route(@RequestBody Route route, GridParameters parameters) {

        if (parameters.getParamTypes().isEmpty()) {
           throw new WeatherException(ErrorMessage.NO_PARAMETERS);
        }

        List<Schedule> schedules = route.getSchedules().getSchedule();
        Optional<Schedule> first = schedules.stream().filter(s -> s.getId().intValue() == 1).findFirst();
        if (first.isPresent()) {
            Schedule schedule = first.get();
            List<ScheduleElement> scheduleElements = schedule.getCalculated().getSheduleElement();
            List<Waypoint> waypoints = route.getWaypoints().getWaypoint();
            if (scheduleElements.size() != waypoints.size()) {
                throw new WeatherException(ErrorMessage.INVALID_RTZ, "the number of scheduleElements and WayPoints are not the same size");
            }

            ArrayList<WeatherPoint> weatherPoints = new ArrayList<>();
            for (int i = 0; i < waypoints.size(); i++) {
                Waypoint waypoint = waypoints.get(i);
                BigDecimal lon = waypoint.getPosition().getLon();
                BigDecimal lat = waypoint.getPosition().getLat();
                GridRequest request = new GridRequest();
                GeoCoordinate geoCoordinate = new GeoCoordinate(lon.doubleValue(), lat.doubleValue());
                request.setNorthWest(geoCoordinate).setSouthEast(geoCoordinate);
                ScheduleElement element = scheduleElements.get(i);
                XMLGregorianCalendar eta = element.getEta();
                if (i == 0) {
                    if (element.getEtd() != null) {
                        eta = element.getEtd();
                    }
                }
                GregorianCalendar calendar = eta.toGregorianCalendar();
                request.setTime(calendar.getTime().toInstant());
                request.setParameters(parameters);
                WeatherPoint wp = new WeatherPoint();
                try {
                    GridResponse response = service.request(request, false, false);
                    GridDataPoint gridDataPoint = response.getPoints().get(0);
                    wp.setData(gridDataPoint);
                    wp.setTime(response.getForecastDate());
                } catch (WeatherException e) {
                    ErrorMessage error = e.getError();
                    wp.setError(new JSonWarning().setId(error.getId()).setMessage(error.getMessage()).setDetails(e.getDetails()));
                }
                weatherPoints.add(wp);
            }
            return new RouteResponse().setWayPoints(weatherPoints);
        }
        else {
            throw new WeatherException(ErrorMessage.INVALID_RTZ, "Missing schedule element with id=1");
        }
    }
}
