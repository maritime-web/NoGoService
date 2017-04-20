package dk.dma.nogoservice.service;

import dk.dma.nogoservice.dto.GridData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Looks for JSON based grid data on S3. the files should be located in the bucket named "maritime-web-nogo", and address by file name (not URL)
 * To see the possible files check https://console.aws.amazon.com/s3/buckets/maritime-web-nogo/?region=us-east-1
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */
@Slf4j
public class S3FileBackedQueryArea extends GridDataQueryArea {
    /**
     *
     * @param areaName the display name of the area
     * @param dataLoader loads data from S3
     * @param key the fileName of the data file inside the bucket
     * @param weatherService a service that can provide weather data
     * @param noGoAlgorithm an algorithm for calculating noGo polygons
     */
    public S3FileBackedQueryArea(String areaName, S3DataLoader dataLoader, String key, WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm) throws IOException {
        super(weatherService, noGoAlgorithm, areaName, dataLoader.loadData(key, GridData.class));
    }

}
