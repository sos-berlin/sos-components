
package com.sos.inventory.model.descriptor.controller;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.ExtendedCertificates;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "certificates",
    "templates"
})
public class Configuration {

    /**
     * Deployment Descriptor for extended Certificates Schema
     * <p>
     * JS7 Deployment Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    @JsonPropertyDescription("JS7 Deployment Descriptor Certificates Schema")
    private ExtendedCertificates certificates;
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
     */
    public Configuration(ExtendedCertificates certificates, List<String> templates) {
        super();
        this.certificates = certificates;
        this.templates = templates;
    }

    /**
     * Deployment Descriptor for extended Certificates Schema
     * <p>
     * JS7 Deployment Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    public ExtendedCertificates getCertificates() {
        return certificates;
    }

    /**
     * Deployment Descriptor for extended Certificates Schema
     * <p>
     * JS7 Deployment Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    public void setCertificates(ExtendedCertificates certificates) {
        this.certificates = certificates;
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
        return new ToStringBuilder(this).append("certificates", certificates).append("templates", templates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certificates).append(templates).toHashCode();
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
        return new EqualsBuilder().append(certificates, rhs.certificates).append(templates, rhs.templates).isEquals();
    }

}