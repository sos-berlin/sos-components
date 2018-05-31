
package com.sos.jobscheduler.model.workflow;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.common.IInstruction;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "instructions"
})
public class Branch {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private String id;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    @JacksonXmlProperty(localName = "instructions")
    private IInstruction instructions;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    @JacksonXmlProperty(localName = "instructions")
    public IInstruction getInstructions() {
        return instructions;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    @JacksonXmlProperty(localName = "instructions")
    public void setInstructions(IInstruction instructions) {
        this.instructions = instructions;
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
        return new ToStringBuilder(this).append("id", id).append("instructions", instructions).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instructions).append(id).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Branch) == false) {
            return false;
        }
        Branch rhs = ((Branch) other);
        return new EqualsBuilder().append(instructions, rhs.instructions).append(id, rhs.id).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
