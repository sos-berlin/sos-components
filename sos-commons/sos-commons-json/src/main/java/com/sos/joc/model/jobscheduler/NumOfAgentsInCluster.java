
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * num of agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "any",
    "running"
})
public class NumOfAgentsInCluster {

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("any")
    @JacksonXmlProperty(localName = "any")
    private Integer any;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("running")
    @JacksonXmlProperty(localName = "running")
    private Integer running;

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("any")
    @JacksonXmlProperty(localName = "any")
    public Integer getAny() {
        return any;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("any")
    @JacksonXmlProperty(localName = "any")
    public void setAny(Integer any) {
        this.any = any;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("running")
    @JacksonXmlProperty(localName = "running")
    public Integer getRunning() {
        return running;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("running")
    @JacksonXmlProperty(localName = "running")
    public void setRunning(Integer running) {
        this.running = running;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("any", any).append("running", running).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(running).append(any).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NumOfAgentsInCluster) == false) {
            return false;
        }
        NumOfAgentsInCluster rhs = ((NumOfAgentsInCluster) other);
        return new EqualsBuilder().append(running, rhs.running).append(any, rhs.any).isEquals();
    }

}
