
package com.sos.inventory.model.descriptor.controller;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.Certificates;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "certificates",
    "controllerCert",
    "templates"
})
public class Configuration {

    /**
     * Deployment Descriptor Certificates Schema
     * <p>
     * JS7 JOC Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    @JsonPropertyDescription("JS7 JOC Descriptor Certificates Schema")
    private Certificates certificates;
    @JsonProperty("controllerCert")
    private String controllerCert;
    @JsonProperty("templates")
    private List<String> templates = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Configuration() {
    }

    /**
     * 
     * @param certificates
     * @param templates
     * @param controllerCert
     */
    public Configuration(Certificates certificates, String controllerCert, List<String> templates) {
        super();
        this.certificates = certificates;
        this.controllerCert = controllerCert;
        this.templates = templates;
    }

    /**
     * Deployment Descriptor Certificates Schema
     * <p>
     * JS7 JOC Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    public Certificates getCertificates() {
        return certificates;
    }

    /**
     * Deployment Descriptor Certificates Schema
     * <p>
     * JS7 JOC Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    public void setCertificates(Certificates certificates) {
        this.certificates = certificates;
    }

    @JsonProperty("controllerCert")
    public String getControllerCert() {
        return controllerCert;
    }

    @JsonProperty("controllerCert")
    public void setControllerCert(String controllerCert) {
        this.controllerCert = controllerCert;
    }

    @JsonProperty("templates")
    public List<String> getTemplates() {
        return templates;
    }

    @JsonProperty("templates")
    public void setTemplates(List<String> templates) {
        this.templates = templates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certificates", certificates).append("controllerCert", controllerCert).append("templates", templates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerCert).append(certificates).append(templates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Configuration) == false) {
            return false;
        }
        Configuration rhs = ((Configuration) other);
        return new EqualsBuilder().append(controllerCert, rhs.controllerCert).append(certificates, rhs.certificates).append(templates, rhs.templates).isEquals();
    }

}
