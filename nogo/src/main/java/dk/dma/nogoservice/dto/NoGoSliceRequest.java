package dk.dma.nogoservice.dto;

import dk.dma.common.dto.GeoCoordinate;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;


@Data
@Accessors(chain = true)
public class NoGoSliceRequest {

	@NotNull
    @Valid
	private GeoCoordinate northWest;
    @NotNull
    @Valid
	private GeoCoordinate southEast;
    @NotNull
    @Valid
    private Double draught;
    @NotNull
    private Instant start;

    @NotNull
    @Min(message = "The number of slices must be between 1-20.", value = 1)
    @Max(message = "The number of slices must be between 1-20.", value = 20)
    private Integer slices;

    @NotNull
    @Min(message = "The interval between slices must be 1-5 (hours).", value = 1)
    @Max(message = "The interval between slices must be 1-5 (hours).", value = 5)
    private Integer interval = 1;

    public String toWKT() {
        return "POLYGON((" + northWest.toWKT() + ", " + southEast.getLon() + " " + northWest.getLat() + ", " +
                southEast.toWKT() + ", " + northWest.getLon() + " " + southEast.getLat() + ", " + northWest.toWKT() + "))";
    }



}
