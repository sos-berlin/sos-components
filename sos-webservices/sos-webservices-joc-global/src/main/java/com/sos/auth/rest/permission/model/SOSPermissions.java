//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.07.07 at 05:29:35 PM CEST 
//


package com.sos.auth.rest.permission.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}SOSPermissionListCommands"/>
 *         &lt;element ref="{}SOSPermissionListJoc"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sosPermissionListCommands",
    "sosPermissionListJoc"
})
@XmlRootElement(name = "SOSPermissions")
public class SOSPermissions {

    @XmlElement(name = "SOSPermissionListCommands", required = true)
    protected SOSPermissionListCommands sosPermissionListCommands;
    @XmlElement(name = "SOSPermissionListJoc", required = true)
    protected SOSPermissionListJoc sosPermissionListJoc;

    /**
     * Gets the value of the sosPermissionListCommands property.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionListCommands }
     *     
     */
    public SOSPermissionListCommands getSOSPermissionListCommands() {
        return sosPermissionListCommands;
    }

    /**
     * Sets the value of the sosPermissionListCommands property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionListCommands }
     *     
     */
    public void setSOSPermissionListCommands(SOSPermissionListCommands value) {
        this.sosPermissionListCommands = value;
    }

    /**
     * Gets the value of the sosPermissionListJoc property.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionListJoc }
     *     
     */
    public SOSPermissionListJoc getSOSPermissionListJoc() {
        return sosPermissionListJoc;
    }

    /**
     * Sets the value of the sosPermissionListJoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionListJoc }
     *     
     */
    public void setSOSPermissionListJoc(SOSPermissionListJoc value) {
        this.sosPermissionListJoc = value;
    }

}
