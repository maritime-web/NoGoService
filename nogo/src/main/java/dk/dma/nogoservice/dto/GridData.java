package dk.dma.nogoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * A class that models a single data series and meta data, similar to a GRIB file with a single parameter
 * The dx, dy values are calculated as dx = (lo2-lo1)/nx; dy = (la2-la1)/ny
 *
 * @author Klaus Groenbaek
 *         Created 18/04/2017.
 */

@Setter
@Getter
@Accessors(chain = true)
@ToString(exclude = "data")
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})    // We don't care that the float array can be externally manipulated
@JsonIgnoreProperties(ignoreUnknown = true)
public class GridData {
    /**
     * As with GRIB we use this value to indicate no value
     */
    public static final float NO_DATA = -9999.0f;

    private String description;

    private String name;
    /**
     * The lowest longitude value
     */
    private float lo1;
    /**
     * The highest longitude value
     */
    private float lo2;
    /**
     * the lowest latitude value
     */
    private float la1;
    /**
     * the highest latitude value
     */
    private float la2;
    /**
     * The number of element on the longitude axis
     */
    private int nx;
    /**
     * the number of elements on the latitude axis alc
     */
    private int ny;
    /**
     * the data array.
     */
    private float[] data;

    @JsonIgnore
    public float getDx() {
        return (lo2-lo1) / nx;
    }

    @JsonIgnore
    public float getDy() {
        return (la2 - la1) / ny;
    }


}
