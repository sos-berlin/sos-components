
package com.sos.inventory.model.descriptor.agent;

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
    "controller",
    "certificates",
    "templates"
})
public class Configuration {

    @JsonProperty("controller")
    private ControllerRef controller;
    /**
     * Deployment Descriptor Certificates Schema
     * <p>
     * JS7 JOC Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    @JsonPropertyDescription("JS7 JOC Descriptor Certificates Schema")
    private Certificates certificates;
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
     * @param controller
     * @param certificates
     * @param templates
     */
    public Configuration(ControllerRef controller, Certificates certificates, List<String> templates) {
        super();
        this.controller = controller;
        this.certificates = certificates;
        this.templates = templates;
    }

    @JsonProperty("controller")
    public ControllerRef getController() {
        return controller;
    }

    @JsonProperty("controller")
    public void setController(ControllerRef controller) {
        this.controller = controller;
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
        return new ToStringBuilder(this).append("controller", controller).append("certificates", certificates).append("templates", templates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controller).append(certificates).append(templates).toHashCode();
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
        return new EqualsBuilder().append(controller, rhs.controller).append(certificates, rhs.certificates).append(templates, rhs.templates).isEquals();
    }

}
