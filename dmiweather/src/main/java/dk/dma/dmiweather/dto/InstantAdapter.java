package dk.dma.dmiweather.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

/**
 * @author Klaus Groenbaek
 *         Created 05/04/2017.
 */
public class InstantAdapter extends XmlAdapter<String, Instant> {

    @Override
    public Instant unmarshal(String v) throws Exception {
        return Instant.parse(v);
    }

    @Override
    public String marshal(Instant v) throws Exception {
        return v.toString();
    }
}
