package dk.dma.dmiweather.service;

import dk.dma.common.dto.GeoCoordinate;
import dk.dma.dmiweather.grib.AbstractDataProvider;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Klaus Groenbaek
 *         Created 06/04/2017.
 */
public class SamplerTest {

    /**
     * Create a 12 x 12 grid sampled it down to 6x6, this this should give us (1,1) (3,3) etc which all have data
     */
    @Test
    public void sixBySix() {

        AbstractDataProvider dataProvider = createGrid();


        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 12), new GeoCoordinate(12, 0), 6, 6);
        for (float v : sampled) {
            assertNotEquals(v, GribFileWrapper.GRIB_NOT_DEFINED, 0.0000001);
        }
    }

    /**
     * Create a 12 x 12 grid, sample it down to 4 x 4, this should give us (2,2),(5,2),(8,2),(11,2),(4,2)
     * the direct coordinates have no data, but the searching of neighbouring cells should find some data
     */
    @Test
    public void fourByFour() {
        AbstractDataProvider dataProvider = createGrid();

        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 12), new GeoCoordinate(12, 0), 4, 4);
        for (float v : sampled) {
            assertNotEquals(v, GribFileWrapper.GRIB_NOT_DEFINED, 0.0000001);
        }
    }


    private AbstractDataProvider createGrid() {
        float[] data = new float[144];
        for (int i = 0; i < data.length; i++) {
            int row = i / 12;
            int col = i % 12;
            if ( row%2 != 0 && col%2 != 0){
                // add a value the expected sample points
                data[i] = i;
            }
            else {
                data[i] = GribFileWrapper.GRIB_NOT_DEFINED;
            }
        }

        return new AbstractDataProvider(5, 1, 1, 12, 12, 0, 12, 0, 12) {
            @Override
            public float[] getData() {
                return data;
            }
        };
    }

}
