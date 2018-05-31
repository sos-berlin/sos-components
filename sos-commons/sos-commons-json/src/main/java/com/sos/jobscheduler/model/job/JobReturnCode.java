
package com.sos.jobscheduler.model.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "success",
    "failure"
})
public class JobReturnCode {

    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "success")
    private List<Integer> success = null;
    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "failure")
    private List<Integer> failure = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    public List<Integer> getSuccess() {
        return success;
    }

    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    public void setSuccess(List<Integer> success) {
        this.success = success;
    }

    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    public List<Integer> getFailure() {
        return failure;
    }

    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    public void setFailure(List<Integer> failure) {
        this.failure = failure;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("success", success).append("failure", failure).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(success).append(failure).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReturnCode) == false) {
            return false;
        }
        JobReturnCode rhs = ((JobReturnCode) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(success, rhs.success).append(failure, rhs.failure).isEquals();
    }

}
