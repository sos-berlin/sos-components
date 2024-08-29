
package com.sos.joc.model.inventory.dependencies;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "type",
    "references",
    "referencedBy"
})
public class ResponseItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private String type;
    @JsonProperty("references")
    private List<ConfigurationObject> references = new ArrayList<ConfigurationObject>();
    @JsonProperty("referencedBy")
    private List<ConfigurationObject> referencedBy = new ArrayList<ConfigurationObject>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("references")
    public List<ConfigurationObject> getReferences() {
        return references;
    }

    @JsonProperty("references")
    public void setReferences(List<ConfigurationObject> references) {
        this.references = references;
    }

    @JsonProperty("referencedBy")
    public List<ConfigurationObject> getReferencedBy() {
        return referencedBy;
    }

    @JsonProperty("referencedBy")
    public void setReferencedBy(List<ConfigurationObject> referencedBy) {
        this.referencedBy = referencedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("type", type).append("references", references).append("referencedBy", referencedBy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(referencedBy).append(type).append(references).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseItem) == false) {
            return false;
        }
        ResponseItem rhs = ((ResponseItem) other);
        return new EqualsBuilder().append(name, rhs.name).append(referencedBy, rhs.referencedBy).append(type, rhs.type).append(references, rhs.references).isEquals();
    }

}
