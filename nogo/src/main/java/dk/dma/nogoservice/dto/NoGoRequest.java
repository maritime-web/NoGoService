package dk.dma.nogoservice.dto;

import dk.dma.common.dto.GeoCoordinate;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class NoGoRequest {

	@NotNull
    @Valid
	private GeoCoordinate northWest;
    @NotNull
    @Valid
	private GeoCoordinate southEast;
    @NotNull
    @Valid
    private Double draught;
    /**
     * Optional, when included we must include tidal information
     */
    private Instant time;

    public String toWKT() {
        return "POLYGON((" + northWest.toWKT() + ", " + southEast.getLon() + " " + northWest.getLat() + ", " +
                southEast.toWKT() + ", " + northWest.getLon() + " " + southEast.getLat() + ", " + northWest.toWKT() + "))";
    }
}
