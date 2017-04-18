package dk.dma.nogoservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import dk.dma.nogoservice.dto.GridData;
import dk.dma.nogoservice.dto.NoGoRequest;
import dk.dma.nogoservice.dto.NoGoResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Looks for JSON based grid data on S3. the files should be located in the bucket named "maritime-web-nogo", and address by file name (not URL)
 * To see the possible files check https://console.aws.amazon.com/s3/buckets/maritime-web-nogo/?region=us-east-1
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */
public abstract class S3FileBackedQueryArea implements QueryArea {

    public static final String DATA_BUCKET = "maritime-web-nogo";
    private final String areaName;

    private final GridData gridData;
    private final Geometry supports;

    /**
     *
     * @param areaName the display name of the area
     * @param amazonS3 the aws client
     * @param key the fileName of the data file inside the bucket
     */
    @SneakyThrows(IOException.class)
    public S3FileBackedQueryArea(String areaName, AmazonS3 amazonS3, String key) {
        this.areaName = areaName;

        S3Object object = amazonS3.getObject(DATA_BUCKET, key);
        gridData = new ObjectMapper().readValue(object.getObjectContent(), GridData.class);
        supports = fromGridData(gridData);

    }


    @Override
    public String getName() {
        return areaName;
    }

    @Override
    public boolean matches(Geometry area) {
        return supports.contains(area);
    }

    @Override
    public NoGoResponse getNogoAreas(NoGoRequest request) {



        return null;
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
