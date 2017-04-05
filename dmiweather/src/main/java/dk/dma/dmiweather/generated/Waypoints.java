//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.04.05 at 12:14:01 PM CEST 
//


package dk.dma.dmiweather.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for Waypoints complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Waypoints">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="defaultWaypoint" type="{http://www.cirm.org/RTZ/1/0}DefaultWaypoint" minOccurs="0"/>
 *         &lt;element name="waypoint" type="{http://www.cirm.org/RTZ/1/0}Waypoint" maxOccurs="unbounded" minOccurs="2"/>
 *         &lt;element name="extensions" type="{http://www.cirm.org/RTZ/1/0}Extensions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Waypoints", propOrder = {
    "defaultWaypoint",
    "waypoint",
    "extensions"
})
public class Waypoints {

    protected DefaultWaypoint defaultWaypoint;
    @XmlElement(required = true)
    protected List<Waypoint> waypoint;
    protected Extensions extensions;

    /**
     * Gets the value of the defaultWaypoint property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultWaypoint }
     *     
     */
    public DefaultWaypoint getDefaultWaypoint() {
        return defaultWaypoint;
    }

    /**
     * Sets the value of the defaultWaypoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultWaypoint }
     *     
     */
    public void setDefaultWaypoint(DefaultWaypoint value) {
        this.defaultWaypoint = value;
    }

    /**
     * Gets the value of the waypoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the waypoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWaypoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Waypoint }
     * 
     * 
     */
    public List<Waypoint> getWaypoint() {
        if (waypoint == null) {
            waypoint = new ArrayList<Waypoint>();
        }
        return this.waypoint;
    }

    /**
     * Gets the value of the extensions property.
     * 
     * @return
     *     possible object is
     *     {@link Extensions }
     *     
     */
    public Extensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Extensions }
     *     
     */
    public void setExtensions(Extensions value) {
        this.extensions = value;
    }

}
