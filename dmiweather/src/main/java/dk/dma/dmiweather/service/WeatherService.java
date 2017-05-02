/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.dmiweather.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.io.WKTWriter;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.exception.APIException;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.dmiweather.dto.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.concurrent.GuardedBy;
import java.io.File;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * A service that provide different weather forecast data extracted from GRIB files, the original fileset was provided by DMI
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Component
@Slf4j
public class WeatherService {

    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000Z").withZone(ZoneId.of("UTC"));

    private static APIException dataOutOfRange(Instant start, Instant end) {
        return new APIException(ErrorMessage.OUT_OF_DATE_RANGE, String.format("Current data set starts at %s and ends at %s, and is updated every %s hours" ,
                TIME_FORMATTER.format(start), TIME_FORMATTER.format(end), 6));
    }

    private final int dataRounding;
    private final int coordinateRounding;

    @GuardedBy("$lock")
    private NavigableMap<Instant, ForecastContainer> cache = new TreeMap<>();

    @Setter
    private volatile ErrorMessage errorMessage;

    @Autowired
    public WeatherService(@Value("${rounding.data}") int dataRounding, @Value("${rounding.coordinates}")int coordinateRounding) {
        this.dataRounding = dataRounding;
        this.coordinateRounding = coordinateRounding;
    }

    public GridResponse request(GridRequest request, boolean removeEmpty, boolean gridMetrics) {

        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();

        if (northWest.getLon() > southEast.getLon()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LOT);
        }
        if (northWest.getLat() < southEast.getLat()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LAT);
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return findForecastData(request.getTime()).getData(request, removeEmpty, gridMetrics);

        } finally {
            log.info("Completed weather request in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Find the container that holds forcasts for the time we are interested in.
     * @param instant the time we need a forecast for
     * @return the container
     */
    @Synchronized
    private ForecastContainer findForecastData(Instant instant) {

        if (cache.isEmpty()) {
            if (errorMessage != null) {
                throw new APIException(errorMessage);
            }
            else {
                throw new APIException(ErrorMessage.DATA_NOT_LOADED);
            }
        }

        // copy to a local to prevent a swap during the process
        if (instant.isBefore(this.cache.firstKey()) || instant.isAfter(this.cache.lastKey())) {
            throw dataOutOfRange(this.cache.firstKey(), this.cache.lastKey());
        }
        ForecastContainer wrapper = this.cache.get(instant);
        if (wrapper != null) {
            return wrapper;
        } else {
            // Not on the hour request, find the nearest
            Map.Entry<Instant, ForecastContainer> ceilingEntry = this.cache.ceilingEntry(instant);
            Map.Entry<Instant, ForecastContainer> floorEntry = this.cache.floorEntry(instant);

            Duration ceilDuration = Duration.between(instant, ceilingEntry.getKey()).abs();
            Duration floorDuration = Duration.between(instant, floorEntry.getKey()).abs();
            if (ceilDuration.compareTo(floorDuration) < 0) {
                return ceilingEntry.getValue();
            } else {
                return floorEntry.getValue();
            }
        }
    }

    /**
     * called when a new list of file have been loaded from FTP.
     * Access must be synchronized, as we update the map
     * @param files A map of files, and the associated creation time from the FTP server
     * @param configuration a particular set of Grib Files
     */
    @SneakyThrows(ParseException.class)
    @Synchronized
    void newFiles(Map<File, Instant> files, ForecastConfiguration configuration) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        DateFormat df = new SimpleDateFormat("yyyyMMddHHZ");

        // Mark all files from the previous load as old
        for (ForecastContainer forecastData : cache.values()) {
            forecastData.markOld(configuration);
        }
        for (Map.Entry<File, Instant> entry : files.entrySet()) {
            File file = entry.getKey();
            Instant creation = entry.getValue();
            Matcher matcher = configuration.getFilePattern().matcher(file.getName());
            if (matcher.matches()) {
                String dateString = matcher.group(1);
                Date date = df.parse(dateString + "+0000");
                Instant instant = date.toInstant();
                GribFileWrapper wrapper = new GribFileWrapper(configuration, creation, file, dataRounding, coordinateRounding);

                ForecastContainer forecastData = new ForecastContainer(instant, coordinateRounding);
                ForecastContainer previous = cache.putIfAbsent(instant, forecastData);
                if (previous != null) {
                    forecastData = previous;
                }
                forecastData.newFile(configuration, wrapper);
            }
        }

        // remove all outdated ForecastData (we have to collect to avoid ConcurrentModificationException)
        List<Instant> old = cache.entrySet().stream()
                .filter(e -> e.getValue().isOld())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (Instant instant : old) {
            cache.remove(instant);
        }

        errorMessage = null;
        log.info("Loading {} GRIB files in {} ms", files.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    public WeatherAreaInfos info() {

        ArrayList<WeatherAreaInfo> areas = new ArrayList<>();
        WKTWriter writer = new WKTWriter();
        List<InfoHelper> infos = getInfo();
        for (InfoHelper info : infos) {
            GribFileWrapper wrapper = info.getWrapper();
            WeatherAreaInfo area = new WeatherAreaInfo().setName(info.configuration.name()).setStart(info.start).setEnd(info.end)
                    .setDx(wrapper.getDx()).setDy(wrapper.getDy()).setNx(wrapper.getNx()).setNy(wrapper.getNy()).setWkt(writer.write(wrapper.getArea()));
            areas.add(area);
        }
        return new WeatherAreaInfos().setAreas(areas);
    }

    /**
     * Run through the cach and check when each configuration starts and ends. Since we only want to loop the cache a single time,
     * finding the end time is a little tricky.
     * This could be created when new files are added, and cached if needed
     */
    @Synchronized
    private List<InfoHelper> getInfo() {

        Instant previous = null;
        Map<ForecastConfiguration, Instant> starts = new HashMap<>();
        Map<ForecastConfiguration, InfoHelper> helpers = new HashMap<>();

        for (Map.Entry<Instant, ForecastContainer> entry : cache.entrySet()) {
            Instant instant = entry.getKey();
            Map<ForecastConfiguration, GribFileWrapper> configurations = entry.getValue().getGribFiles();
            for (Map.Entry<ForecastConfiguration, GribFileWrapper> fileEntry : configurations.entrySet()) {
                ForecastConfiguration config = fileEntry.getKey();
                if (!starts.containsKey(config)) {
                    helpers.put(config, new InfoHelper().setStart(instant).setConfiguration(config).setWrapper(fileEntry.getValue()));
                    starts.put(config, instant);
                }
            }
            // if the start map has a configuration which is not in the configurations keyset, then the previous one was the end.
            Sets.SetView<ForecastConfiguration> finished = Sets.difference(starts.keySet(), configurations.keySet());
            for (ForecastConfiguration forecastConfiguration : finished) {
                helpers.get(forecastConfiguration).setEnd(previous);
            }
            previous = instant;
        }
        for (InfoHelper helper : helpers.values()) {
            if (helper.getEnd() == null) {
                helper.setEnd(previous);
            }
        }
        return Lists.newArrayList(helpers.values());
    }

    /**
     * Helper for building Info
     */
    @Data
    @Accessors(chain = true)
    private static class InfoHelper {
        private ForecastConfiguration configuration;
        private GribFileWrapper wrapper;
        private Instant start;
        private Instant end;
    }
}
