package dk.dma.nogoservice.service;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.WKTWriter;
import dk.dma.common.dto.*;
import dk.dma.dmiweather.dto.*;
import dk.dma.nogoservice.algo.NoGoMatcher;
import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.entity.SouthKattegat;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Looks for JSON based grid data on S3. the files should be located in the bucket named "maritime-web-nogo", and address by file name (not URL)
 * To see the possible files check https://console.aws.amazon.com/s3/buckets/maritime-web-nogo/?region=us-east-1
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */
@SuppressWarnings("Duplicates")
@Slf4j
public abstract class GridDataQueryArea implements QueryArea {

    private final AtomicInteger nextRequestId = new AtomicInteger(0);
    private final WeatherService weatherService;
    private final NoGoAlgorithmFacade noGoAlgorithm;
    private final GridData gridData;
    private final Geometry supports;

    /**
     * @param weatherService service that can provide weather info
     * @param noGoAlgorithm facade that provides an algo that can create polygons from a grid
     */
    GridDataQueryArea(WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm, GridData gridData) {
        this.weatherService = weatherService;
        this.noGoAlgorithm = noGoAlgorithm;
        this.gridData = gridData;
        supports = fromGridData(gridData);
    }


    @Override
    public String getName() {
        return gridData.getName();
    }

    @Override
    public boolean matches(Geometry area) {
        return supports.contains(area);
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
                        .setTime(request.getTime()).setParameters(new GridParameters().setSeaLevel(true)));
                optionalWeather = Optional.of(new TidalQueryObject(weather));
                log.info("loaded tidal info {}x{} for request {} in {} ms", weather.getNy(), weather.getNx(), requestId, tidal.stop().elapsed(TimeUnit.MILLISECONDS));
            } catch (JsonErrorException e) {
                WarningMessage warn = WarningMessage.MISSING_TIDAL_INFO;
                noGoResponse.setWarning(new JSonWarning().setId(warn.getId()).setMessage(warn.getMessage()).setDetails(e.getJSonError().getMessage()));
                log.warn("Failed to invoke remote weather service", e);
            }
        }
        Stopwatch createGrid = Stopwatch.createStarted();
        List<List<SouthKattegat>> grid = createGrid(request);
        log.info("created {}x{} grid, request {} in {} ms", grid.size(), grid.get(0).size(), requestId,  createGrid.stop().elapsed(TimeUnit.MILLISECONDS));

        Double draught = request.getDraught();

        Stopwatch nogoCalculation = Stopwatch.createStarted();

        NoGoMatcher<SouthKattegat> noGoMatcher;
        if (optionalWeather.isPresent()) {
            TidalQueryObject tidalQueryObject = optionalWeather.get();
            noGoMatcher = southKattegat -> {
                return southKattegat.getDepth() == null || -southKattegat.getDepth() + tidalQueryObject.getTidalHeight(southKattegat) < draught; // DB has altitude values so depth is negative
            };
        } else {
            noGoMatcher = southKattegat -> {
                return southKattegat.getDepth() == null || -southKattegat.getDepth() < draught; // DB has altitude values so depth is negative
            };
        }

        List<NoGoPolygon> polygons = noGoAlgorithm.getNoGo(grid, noGoMatcher);
        log.info("Nogo grouping {}x{}, request {} in {} ms", grid.size(), grid.get(0).size(), requestId,  nogoCalculation.stop().elapsed(TimeUnit.MILLISECONDS));
        return noGoResponse.setPolygons(polygons);
    }

    @Override
    public AreaInfo getInfo() {
        String wkt = new WKTWriter().write(supports);
        return new AreaInfo().setDx(gridData.getDx()).setDy(gridData.getDy()).setName(getName()).setWkt(wkt);
    }

    /**
     * Creates a grid of coordinate points with depth from the data file.
     */
    private List<List<SouthKattegat>> createGrid(NoGoRequest request) {

        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();
        double lonDistance = southEast.getLon() - northWest.getLon();
        double latDistance = northWest.getLat() - southEast.getLat();
        double dy = gridData.getDy();
        double dx = gridData.getDx();
        int Nx = (int) Math.round(lonDistance / dx) +1;
        int Ny = (int) Math.round(latDistance / dy) +1;
        float[] data = gridData.getData();

        int startY = (int) Math.floor((southEast.getLat() - gridData.getLa1()) / dy);
        int startX = (int) Math.floor((northWest.getLon() - gridData.getLo1()) / dx);

        List<List<SouthKattegat>> grid = new ArrayList<>();
        for (int row = 0; row < Ny; row++) {
            ArrayList<SouthKattegat> rowData = new ArrayList<>();
            for (int col = 0; col < Nx; col++) {
                float datum = data[(row + startY) * gridData.getNx() + (startX + col)];
                SouthKattegat southKattegat = new SouthKattegat();
                if (datum != GridData.NO_DATA) {
                    southKattegat.setDepth((double) datum);
                }
                southKattegat.setLat((startY+row) * dy + gridData.getLa1());
                southKattegat.setLon( (startX + col) * dx + gridData.getLo1());
                rowData.add(southKattegat);
            }
            grid.add(rowData);
        }

        return grid;
    }

    private Geometry fromGridData(GridData gridData) {
        GeometryFactory factory = new GeometryFactory();
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(gridData.getLo1(), gridData.getLa1()));
        coordinates.add(new Coordinate(gridData.getLo2(), gridData.getLa1()));
        coordinates.add(new Coordinate(gridData.getLo2(), gridData.getLa2()));
        coordinates.add(new Coordinate(gridData.getLo1(), gridData.getLa2()));
        coordinates.add(new Coordinate(gridData.getLo1(), gridData.getLa1()));

        return new Polygon(new LinearRing(new CoordinateArraySequence(coordinates.toArray(new Coordinate[5])),factory),new LinearRing[0],factory);
    }

}
