
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "successful",
    "failed"
})
public class JobsHistoricSummary {

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    @JacksonXmlProperty(localName = "successful")
    private Integer successful;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    @JacksonXmlProperty(localName = "failed")
    private Integer failed;

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    @JacksonXmlProperty(localName = "successful")
    public Integer getSuccessful() {
        return successful;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    @JacksonXmlProperty(localName = "successful")
    public void setSuccessful(Integer successful) {
        this.successful = successful;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    @JacksonXmlProperty(localName = "failed")
    public Integer getFailed() {
        return failed;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    @JacksonXmlProperty(localName = "failed")
    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("successful", successful).append("failed", failed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(successful).append(failed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobsHistoricSummary) == false) {
            return false;
        }
        JobsHistoricSummary rhs = ((JobsHistoricSummary) other);
        return new EqualsBuilder().append(successful, rhs.successful).append(failed, rhs.failed).isEquals();
    }

}
