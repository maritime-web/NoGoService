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
package dk.dma.nogoservice.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import dk.dma.common.dto.JSonWarning;
import dk.dma.common.dto.JsonErrorException;
import dk.dma.dmiweather.dto.*;
import dk.dma.nogoservice.algo.NoGoMatcher;
import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.entity.SouthKattegat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Query area for south Kattegat as defined by the area. If the request contains a time parameter, it will contact the weather service,
 * to obtain tidal information, which is then added when calculating nogo areas
 * @author Klaus Groenbaek
 *         Created 13/03/17.
 */
@Component
@Slf4j
public class SouthKattegatQueryArea implements QueryArea {

    /**
     * Since the DB contains specific points, we need to add a little padding to be sure to find the area which contains the
     * specified rectangle. This is half the distance between each measuring point
     */
    private static final double latOffset = 0.00055504;
    private static final double lonOffset = 0.00055504;

    private final AtomicInteger nextRequestId = new AtomicInteger(0);
    private final Geometry sydKattegat;
    private final NoGoAlgorithmFacade noGoAlgorithm;
    private final WeatherService weatherService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    @SneakyThrows(ParseException.class)
    public SouthKattegatQueryArea(NoGoAlgorithmFacade noGoAlgorithm, WeatherService weatherService) {
        this.noGoAlgorithm = noGoAlgorithm;
        this.weatherService = weatherService;
        WKTReader reader = new WKTReader();
        sydKattegat = reader.read("POLYGON((9.419409 54.36294,  13.149009 54.36294, 13.149009 56.36316, 9.419409 56.36316, 9.419409 54.36294))");

    }

    @Override
    public String getName() {
        return "South Kattegat";
    }

    @Override
    public boolean matches(Geometry area) {
        return sydKattegat.contains(area);
    }

    @Override
    public NoGoResponse getNogoAreas(NoGoRequest request) {
        NoGoResponse noGoResponse = new NoGoResponse();

        int requestId = this.nextRequestId.incrementAndGet();
        log.info("processing request {}, input ", requestId, request);

        Optional<TidalQueryObject> optionalWeather = Optional.empty();
        if (request.getTime() != null) {
            Stopwatch tidal = Stopwatch.createStarted();
            try {
                GridResponse weather = weatherService.getWeather(new GridRequest().setNorthWest(request.getNorthWest()).setSouthEast(request.getSouthEast())
                        .setTime(request.getTime()).setParameters(new GridParameters().setSealevel(true)));
                optionalWeather = Optional.of(new TidalQueryObject(weather));
                log.info("loaded tidal info {}x{} for request {} in {} ms", weather.getNy(), weather.getNx(), requestId, tidal.stop().elapsed(TimeUnit.MILLISECONDS));
            } catch (JsonErrorException e) {
                WarningMessage warn = WarningMessage.MISSING_TIDAL_INFO;
                noGoResponse.setWarning(new JSonWarning().setId(warn.getId()).setMessage(warn.getMessage()).setDetails(e.getJSonError().getMessage()));
                log.warn("Failed to invoke remote weather service", e);
            }
        }

        // need to enlarge the area, whit half the size between measuring points to be sure we don't overlook anything at the edges
        request = request.plusPadding(lonOffset, latOffset);
        Double draught = request.getDraught();
        Stopwatch boundaryTime = Stopwatch.createStarted();
        // find the min/max n,m values allowing us to select a rectangle. We find n,m to e sure we get a rectangle in case the lon/lat were not 100%
        // accurate, which would cause a jagged grid, making further operations harder
        QueryBoundary boundary = em.createQuery("select new dk.dma.nogoservice.dto.QueryBoundary(min(s.n), max(s.n), min(s.m), max(s.m)) " +
                "from SouthKattegat s where s.lon > :west and s.lon < :east and s.lat > :south and s.lat < :north", QueryBoundary.class)
                .setParameter("west", request.getNorthWest().getLon())
                .setParameter("east", request.getSouthEast().getLon())
                .setParameter("south", request.getSouthEast().getLat())
                .setParameter("north", request.getNorthWest().getLat())
                .getSingleResult();


        log.info("boundary query, request {} in {} ms", requestId, boundaryTime.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch dataQuery = Stopwatch.createStarted();
        // we order by m(lat), n(lon) because this can be mapped to y, x and we prefer to handle the data in rows
        List<SouthKattegat> result = em.createQuery("select s from SouthKattegat s where s.n >= :minN and s.n <= :maxN and s.m >= :minM and s.m <= :maxM order by s.m, s.n", SouthKattegat.class)
                .setParameter("minN", boundary.getMinN()).setParameter("maxN", boundary.getMaxN())
                .setParameter("minM", boundary.getMinM()).setParameter("maxM", boundary.getMaxM())
                .getResultList();

        log.info("data query, request {} in {} ms", requestId, dataQuery.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch nogoCalculation = Stopwatch.createStarted();
        // we can now turn the list into a grid, by splitting into rows of length maxN-minN
        List<List<SouthKattegat>> grid = Lists.partition(result, boundary.getColumnCount());

        NoGoMatcher<SouthKattegat> noGoMatcher;
        if (optionalWeather.isPresent()) {
            TidalQueryObject tidalQueryObject = optionalWeather.get();
            noGoMatcher = southKattegat -> {
                return southKattegat.getDepth() == null || -southKattegat.getDepth() + tidalQueryObject.getTidalHeight(southKattegat) > draught; // DB has altitude values so depth is negative
            };
        } else {
            noGoMatcher = southKattegat -> {
                return southKattegat.getDepth() == null || -southKattegat.getDepth() > draught; // DB has altitude values so depth is negative
            };
        }


        List<NoGoPolygon> polygons = noGoAlgorithm.getNoGo(grid, noGoMatcher);

        log.info("Nogo grouping {}x{}, request {} in {} ms", grid.size(), grid.get(0).size(), requestId,  nogoCalculation.stop().elapsed(TimeUnit.MILLISECONDS));


        return noGoResponse.setPolygons(polygons);
    }

}
