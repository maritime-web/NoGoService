package dk.dma.nogoservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import dk.dma.nogoservice.dto.GridData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Looks for JSON based grid data on S3. the files should be located in the bucket named "maritime-web-nogo", and address by file name (not URL)
 * To see the possible files check https://console.aws.amazon.com/s3/buckets/maritime-web-nogo/?region=us-east-1
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */
@Slf4j
public abstract class S3FileBackedQueryArea extends GridDataQueryArea {


    @SneakyThrows(IOException.class)
    private static GridData getGridData(AmazonS3 amazonS3, String key) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        S3Object object = amazonS3.getObject(DATA_BUCKET, key);
        GridData gridData = new ObjectMapper().readValue(object.getObjectContent(), GridData.class);
        log.info("Loaded file {} from Amazon S3 in {} ms", key, stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return gridData;
    }

    public static final String DATA_BUCKET = "maritime-web-nogo";
    /**
     *
     * @param areaName the display name of the area
     * @param amazonS3 the aws client
     * @param key the fileName of the data file inside the bucket
     * @param weatherService a service that can provide weather data
     * @param noGoAlgorithm an algorithm for calculating noGo polygons
     */

    public S3FileBackedQueryArea(String areaName, AmazonS3 amazonS3, String key, WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm) {
        super(weatherService, noGoAlgorithm, areaName, getGridData(amazonS3, key));
    }

}
