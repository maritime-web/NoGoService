/**
 * Java 8 time types are not supported by JAXB, so we need a package file to register XML adapters
 * @author Klaus Groenbaek
 * Created 05/04/2017.
 */
@XmlJavaTypeAdapters(value = { @XmlJavaTypeAdapter(type=Instant.class, value=InstantAdapter.class)})
package dk.dma.dmiweather.dto;


import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.Instant;


