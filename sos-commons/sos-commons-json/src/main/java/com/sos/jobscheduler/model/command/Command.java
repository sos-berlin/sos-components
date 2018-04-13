
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * commands such as terminate or restart
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "sigtermProcesses",
    "sigkillProcessesAfter"
})
public class Command {

    /**
     * command types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private Type tYPE;
    @JsonProperty("sigtermProcesses")
    @JacksonXmlProperty(localName = "sigtermProcesses")
    private Boolean sigtermProcesses;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sigkillProcessesAfter")
    @JacksonXmlProperty(localName = "sigkillProcessesAfter")
    private Integer sigkillProcessesAfter;

    /**
     * command types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public Type getTYPE() {
        return tYPE;
    }

    /**
     * command types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(Type tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("sigtermProcesses")
    @JacksonXmlProperty(localName = "sigtermProcesses")
    public Boolean getSigtermProcesses() {
        return sigtermProcesses;
    }

    @JsonProperty("sigtermProcesses")
    @JacksonXmlProperty(localName = "sigtermProcesses")
    public void setSigtermProcesses(Boolean sigtermProcesses) {
        this.sigtermProcesses = sigtermProcesses;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sigkillProcessesAfter")
    @JacksonXmlProperty(localName = "sigkillProcessesAfter")
    public Integer getSigkillProcessesAfter() {
        return sigkillProcessesAfter;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sigkillProcessesAfter")
    @JacksonXmlProperty(localName = "sigkillProcessesAfter")
    public void setSigkillProcessesAfter(Integer sigkillProcessesAfter) {
        this.sigkillProcessesAfter = sigkillProcessesAfter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("sigtermProcesses", sigtermProcesses).append("sigkillProcessesAfter", sigkillProcessesAfter).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(sigtermProcesses).append(sigkillProcessesAfter).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Command) == false) {
            return false;
        }
        Command rhs = ((Command) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(sigtermProcesses, rhs.sigtermProcesses).append(sigkillProcessesAfter, rhs.sigkillProcessesAfter).isEquals();
    }

}
