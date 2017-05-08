package dk.dma.nogoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.nogoservice.dto.GridData;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * looks for a JSon fil eon disk
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */
@Slf4j
public class FileBackedQueryArea extends GridDataQueryArea {

    public FileBackedQueryArea(File file, WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm) throws IOException {
        super(weatherService, noGoAlgorithm, new ObjectMapper().readValue(file, GridData.class));
    }

}
