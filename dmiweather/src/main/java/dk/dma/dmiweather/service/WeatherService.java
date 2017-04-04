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
import dk.dma.dmiweather.dto.*;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * A service that provide different weather data based loaded from GRIB files
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Component
@Slf4j
public class WeatherService {

    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000Z").withZone(ZoneId.of("UTC"));

    private static WeatherException dataOutOfRange(Instant start, Instant end) {
        return new WeatherException(ErrorMessage.OUT_OF_RANGE, String.format("Current data set starts at %s and ends at %s, and is updated every %s hours" ,
                TIME_FORMATTER.format(start), TIME_FORMATTER.format(end), 6));
    }

    private final int dataRounding;
    private final int coordinateRouding;

    private volatile ConcurrentSkipListMap<Instant, GribFileWrapper> cache;
    @Setter
    private volatile ErrorMessage errorMessage;

    @Autowired
    public WeatherService(@Value("${rounding.data}") int dataRounding, @Value("${rounding.coordinates}")int coordinateRounding) {
        this.dataRounding = dataRounding;
        this.coordinateRouding = coordinateRounding;
    }

    public GridResponse request(GridRequest request, boolean removeEmpty, boolean gridMetrics) {

        if (cache == null) {
            if (errorMessage != null) {
                throw new WeatherException(errorMessage);
            }
            else {
                throw new WeatherException(ErrorMessage.DATA_NOT_LOADED);
            }
        } else {
            // copy to a local to prevent a swap during the process
            ConcurrentSkipListMap<Instant, GribFileWrapper> local = this.cache;
            if (request.getTime().isBefore(local.firstKey()) || request.getTime().isAfter(local.lastKey())) {
                throw dataOutOfRange(local.firstKey(), local.lastKey());
            }
            GribFileWrapper wrapper = local.get(request.getTime());
            if (wrapper != null) {
               return wrapper.getData(request, removeEmpty, gridMetrics);
            } else {
                // Not on the hour request, find the nearest
                Map.Entry<Instant, GribFileWrapper> ceilingEntry = local.ceilingEntry(request.getTime());
                Map.Entry<Instant, GribFileWrapper> floorEntry = local.floorEntry(request.getTime());

                Duration ceilDuration = Duration.between(request.getTime(), ceilingEntry.getKey()).abs();
                Duration floorDuration = Duration.between(request.getTime(), floorEntry.getKey()).abs();
                if (ceilDuration.compareTo(floorDuration) < 0) {
                    return ceilingEntry.getValue().getData(request, removeEmpty, gridMetrics);
                } else {
                    return floorEntry.getValue().getData(request, removeEmpty, gridMetrics);
                }
            }
        }
    }

    /**
     * called when a new list of file have been loaded from FTP.
     *
     * @param files new grib files
     */
    @SneakyThrows(ParseException.class)
    void newFiles(List<File> files) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        DateFormat df = new SimpleDateFormat("yyyyMMddHHZ");
        ConcurrentSkipListMap<Instant, GribFileWrapper> newCache = new ConcurrentSkipListMap<>();
        for (File file : files) {
            Matcher matcher = FTPLoader.DENMARK_FILE_PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                String dateString = matcher.group(1);
                Date date = df.parse(dateString + "+0000");
                Instant instant = date.toInstant();
                GribFileWrapper wrapper = new GribFileWrapper(instant, file, dataRounding, coordinateRouding);
                newCache.put(instant, wrapper);
            }
        }
        cache = newCache;
        errorMessage = null;
        log.info("Loading {} GRIB files in {} ms", files.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
