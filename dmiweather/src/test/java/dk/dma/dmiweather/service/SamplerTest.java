package dk.dma.dmiweather.service;

import dk.dma.common.dto.GeoCoordinate;
import dk.dma.dmiweather.grib.AbstractDataProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static dk.dma.dmiweather.service.GribFileWrapper.GRIB_NOT_DEFINED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

        AbstractDataProvider dataProvider = createGrid(12);


        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 11), new GeoCoordinate(11, 0), 6, 6, 2, 1, 2, 1);
        for (float v : sampled) {
            assertNotEquals(v, GRIB_NOT_DEFINED, 0.0000001);
        }
    }

    /**
     * Create a 12 x 12 grid, sample it down to 4 x 4, this should give us (1,1),(4,1),(7,1),(10,1),(1,4),(4,4)...
     * the direct coordinates have no data, but the searching of neighbouring cells should find some data
     */
    @Test
    public void fourByFour() {
        AbstractDataProvider dataProvider = createGrid(12);
        float spacing = 12f / 4;
        float offset =  (11 % 3) / 2;
        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 11), new GeoCoordinate(11, 0), 4, 4, spacing, offset, spacing, offset);
        for (float v : sampled) {
            assertNotEquals(v, GRIB_NOT_DEFINED, 0.0000001);
        }
    }

    /**
     * Take a 8x8 of the 12x12 grid and down sample it to a 4x4 grid
     */
    @Test
    public void subGridFourByFour() {
        AbstractDataProvider dataProvider = createGrid(12);
        float spacing = 12f / 4;
        float offset =  (11 % 3) / 2;
        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 7), new GeoCoordinate(7, 0), 4, 4, spacing, offset, spacing, offset);
        for (float v : sampled) {
            assertNotEquals(v, GRIB_NOT_DEFINED, 0.0000001);
        }

    }

    /**
     * We take an 9x9 of a 12x12 grid, and down sample to 5x5.
     * Sampling 9 to 5 should give us 0,2,4,6,8 with spacing 2
     */
    @Test
    public void subGridFiveByFive() {
        AbstractDataProvider dataProvider = createGrid(12);

        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 8), new GeoCoordinate(8, 0), 5, 5, 2, 0, 2, 0);
        for (float v : sampled) {
            assertNotEquals(v, GRIB_NOT_DEFINED, 0.0000001);
        }

    }

    /**
     * 2x2 up to 4x4
     */
    @Test
    public void upToFourByFour() {
        List<Float> found = upSampling(4);
        assertEquals("The (1,1) point should have a value and when upscaled it should be there four times", 4, found.size());

    }

    /**
     * 2x2 up to 6x6
     */
    @Test
    public void upToSixBySix() {
        List<Float> found = upSampling(6);
        assertEquals("The (1,1) point should have a value and when upscaled it should be there four times", 9, found.size());
    }

    private List<Float> upSampling(int size) {
        AbstractDataProvider dataProvider = createGrid(2);
        float spacing =  2f / size;
        float[] sampled = dataProvider.getData(new GeoCoordinate(0, 1), new GeoCoordinate(1, 0), size, size, spacing, 0, spacing, 0);
        int expected = size * size;
        assertEquals("length", expected, sampled.length);
        List<Float> found = new ArrayList<>();
        for (float v : sampled) {
            if (v != GRIB_NOT_DEFINED) {
                found.add(v);
            }
        }
        return found;
    }


    private AbstractDataProvider createGrid(final int size) {
        float[] data = new float[size*size];
        for (int i = 0; i < data.length; i++) {
            int row = i / size;
            int col = i % size;
            if ( row%2 != 0 && col%2 != 0){
                // add a value the expected sample points
                data[i] = i;
            }
            else {
                data[i] = GRIB_NOT_DEFINED;
            }
        }

        return new AbstractDataProvider(5, 1, 1, size, size, 0, size-1, 0, size-1) {
            @Override
            public float[] getData() {
                return data;
            }
        };
    }

}
