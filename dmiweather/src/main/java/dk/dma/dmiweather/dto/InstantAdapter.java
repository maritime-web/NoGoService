package dk.dma.dmiweather.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Klaus Groenbaek
 *         Created 05/04/2017.
 */
public class InstantAdapter extends XmlAdapter<String, Instant> {
    private static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000Z").withZone(ZoneId.of("UTC"));

    @Override
    public Instant unmarshal(String v) throws Exception {
        return Instant.parse(v);
    }

    @Override
    public String marshal(Instant v) throws Exception {
        return df.format(v);
    }
}
