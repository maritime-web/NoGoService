package dk.dma.nogoservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nogoRequest", propOrder = {})
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

    public String toWKT() {
        return "POLYGON((" + northWest.toWKT() + ", " + southEast.getLon() + " " + northWest.getLat() + ", " +
                southEast.toWKT() + ", " + northWest.getLon() + " " + southEast.getLat() + ", " + northWest.toWKT() + "))";
    }


    /**
     * Creates a new request with the following padding. The padding is added is added around the rectangle in all 4 directions
     * @param lonPadding the padding to add north, south
     * @param latPadding the padding to add west and east
     * @return a new request with including the padding
     */
    public NoGoRequest plusPadding(double lonPadding, double latPadding) {
        return new NoGoRequest()
                .setNorthWest(new GeoCoordinate(northWest.getLon() + lonPadding, northWest.getLat() + latPadding))
                .setSouthEast(new GeoCoordinate(southEast.getLon() + lonPadding, southEast.getLat() + latPadding))
                .setDraught(draught);
    }
}
