
package com.sos.inventory.model.descriptor.joc;

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
    "responseDir",
    "certificates",
    "jocCert",
    "templates",
    "startFiles"
})
public class Configuration {

    @JsonProperty("responseDir")
    private String responseDir;
    /**
     * Deployment Descriptor Certificates Schema
     * <p>
     * JS7 JOC Descriptor Certificates Schema
     * 
     */
    @JsonProperty("certificates")
    @JsonPropertyDescription("JS7 JOC Descriptor Certificates Schema")
    private Certificates certificates;
    @JsonProperty("jocCert")
    private String jocCert;
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
     * @param jocCert
     * @param startFiles
     * @param certificates
     * @param templates
     */
    public Configuration(String responseDir, Certificates certificates, String jocCert, List<String> templates, StartFiles startFiles) {
        super();
        this.responseDir = responseDir;
        this.certificates = certificates;
        this.jocCert = jocCert;
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

    @JsonProperty("jocCert")
    public String getJocCert() {
        return jocCert;
    }

    @JsonProperty("jocCert")
    public void setJocCert(String jocCert) {
        this.jocCert = jocCert;
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
        return new ToStringBuilder(this).append("responseDir", responseDir).append("certificates", certificates).append("jocCert", jocCert).append("templates", templates).append("startFiles", startFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(responseDir).append(jocCert).append(startFiles).append(certificates).append(templates).toHashCode();
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
        return new EqualsBuilder().append(responseDir, rhs.responseDir).append(jocCert, rhs.jocCert).append(startFiles, rhs.startFiles).append(certificates, rhs.certificates).append(templates, rhs.templates).isEquals();
    }

}
