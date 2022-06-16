//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.03.31 at 01:07:41 PM CEST 
//


package com.sos.auth.classes.permission.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sos.auth.rest.permission.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SOSPermissionRole_QNAME = new QName("", "SOSPermissionRole");
    private final static QName _SOSPermission_QNAME = new QName("", "SOSPermission");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sos.auth.rest.permission.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SOSPermissionShiro }
     * 
     */
    public SOSPermissionShiro createSOSPermissionShiro() {
        return new SOSPermissionShiro();
    }

    /**
     * Create an instance of {@link SOSPermissionRoles }
     * 
     */
    public SOSPermissionRoles createSOSPermissionRoles() {
        return new SOSPermissionRoles();
    }

    /**
     * Create an instance of {@link SOSPermissions }
     * 
     */
    public SOSPermissions createSOSPermissions() {
        return new SOSPermissions();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "SOSPermissionRole")
    public JAXBElement<String> createSOSPermissionRole(String value) {
        return new JAXBElement<String>(_SOSPermissionRole_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "SOSPermission")
    public JAXBElement<String> createSOSPermission(String value) {
        return new JAXBElement<String>(_SOSPermission_QNAME, String.class, null, value);
    }

}
