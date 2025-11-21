
package com.sos.joc.model.inventory.dependencies.get;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    "references",
    "referencedBy",
    "enforcedReferences",
    "enforcedReferencedBy"
})
public class ResponseObject
    extends ConfigurationObject
{

    @JsonProperty("references")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> references = new LinkedHashSet<Long>();
    @JsonProperty("referencedBy")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> referencedBy = new LinkedHashSet<Long>();
    @JsonProperty("enforcedReferences")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> enforcedReferences = new LinkedHashSet<Long>();
    @JsonProperty("enforcedReferencedBy")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> enforcedReferencedBy = new LinkedHashSet<Long>();

    @JsonProperty("references")
    public Set<Long> getReferences() {
        return references;
    }

    @JsonProperty("references")
    public void setReferences(Set<Long> references) {
        this.references = references;
    }

    @JsonProperty("referencedBy")
    public Set<Long> getReferencedBy() {
        return referencedBy;
    }

    @JsonProperty("referencedBy")
    public void setReferencedBy(Set<Long> referencedBy) {
        this.referencedBy = referencedBy;
    }

    @JsonProperty("enforcedReferences")
    public Set<Long> getEnforcedReferences() {
        return enforcedReferences;
    }

    @JsonProperty("enforcedReferences")
    public void setEnforcedReferences(Set<Long> enforcedReferences) {
        this.enforcedReferences = enforcedReferences;
    }

    @JsonProperty("enforcedReferencedBy")
    public Set<Long> getEnforcedReferencedBy() {
        return enforcedReferencedBy;
    }

    @JsonProperty("enforcedReferencedBy")
    public void setEnforcedReferencedBy(Set<Long> enforcedReferencedBy) {
        this.enforcedReferencedBy = enforcedReferencedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("references", references).append("referencedBy", referencedBy).append("enforcedReferences", enforcedReferences).append("enforcedReferencedBy", enforcedReferencedBy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(referencedBy).append(references).append(enforcedReferences).append(enforcedReferencedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseObject) == false) {
            return false;
        }
        ResponseObject rhs = ((ResponseObject) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(referencedBy, rhs.referencedBy).append(references, rhs.references).append(enforcedReferences, rhs.enforcedReferences).append(enforcedReferencedBy, rhs.enforcedReferencedBy).isEquals();
    }

}
