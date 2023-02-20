
package com.sos.inventory.model.descriptor.joc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.ExtendedCertificates;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "responseDir",
    "certificates",
    "templates",
    "startFiles"
})
public class Configuration {

    @JsonProperty("responseDir")
    private String responseDir;
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
    @JsonProperty("startFiles")
    private StartFiles startFiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Configuration() {
    }

    /**
     * 
     * @param responseDir
     * @param startFiles
     * @param certificates
     * @param templates
     */
    public Configuration(String responseDir, ExtendedCertificates certificates, List<String> templates, StartFiles startFiles) {
        super();
        this.responseDir = responseDir;
        this.certificates = certificates;
        this.templates = templates;
        this.startFiles = startFiles;
    }

    @JsonProperty("responseDir")
    public String getResponseDir() {
        return responseDir;
    }

    @JsonProperty("responseDir")
    public void setResponseDir(String responseDir) {
        this.responseDir = responseDir;
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

    @JsonProperty("startFiles")
    public StartFiles getStartFiles() {
        return startFiles;
    }

    @JsonProperty("startFiles")
    public void setStartFiles(StartFiles startFiles) {
        this.startFiles = startFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("responseDir", responseDir).append("certificates", certificates).append("templates", templates).append("startFiles", startFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(responseDir).append(startFiles).append(certificates).append(templates).toHashCode();
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
        return new EqualsBuilder().append(responseDir, rhs.responseDir).append(startFiles, rhs.startFiles).append(certificates, rhs.certificates).append(templates, rhs.templates).isEquals();
    }

}
