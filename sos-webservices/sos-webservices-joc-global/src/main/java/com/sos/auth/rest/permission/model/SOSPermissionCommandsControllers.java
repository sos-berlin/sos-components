//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.09.18 at 06:59:05 PM CEST 
//


package com.sos.auth.rest.permission.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element ref="{}SOSPermissionCommandsController" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sosPermissionCommandsController"
})
@XmlRootElement(name = "SOSPermissionCommandsControllers")
public class SOSPermissionCommandsControllers
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "SOSPermissionCommandsController")
    protected List<SOSPermissionCommandsController> sosPermissionCommandsController;

    /**
     * Gets the value of the sosPermissionCommandsController property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sosPermissionCommandsController property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSOSPermissionCommandsController().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SOSPermissionCommandsController }
     * 
     * 
     */
    public List<SOSPermissionCommandsController> getSOSPermissionCommandsController() {
        if (sosPermissionCommandsController == null) {
            sosPermissionCommandsController = new ArrayList<SOSPermissionCommandsController>();
        }
        return this.sosPermissionCommandsController;
    }

    public boolean isSetSOSPermissionCommandsController() {
        return ((this.sosPermissionCommandsController!= null)&&(!this.sosPermissionCommandsController.isEmpty()));
    }

    public void unsetSOSPermissionCommandsController() {
        this.sosPermissionCommandsController = null;
    }

}
