
package com.sos.joc.model.inventory.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commitId",
    "withPositions"
})
public class RequestFilter
    extends com.sos.joc.model.inventory.common.RequestFilter
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    private String commitId;
    /**
     * only for Workflows. If true then the Workflow instructions are sent with positions
     * 
     */
    @JsonProperty("withPositions")
    @JsonPropertyDescription("only for Workflows. If true then the Workflow instructions are sent with positions")
    private Boolean withPositions;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    public String getCommitId() {
        return commitId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    /**
     * only for Workflows. If true then the Workflow instructions are sent with positions
     * 
     */
    @JsonProperty("withPositions")
    public Boolean getWithPositions() {
        return withPositions;
    }

    /**
     * only for Workflows. If true then the Workflow instructions are sent with positions
     * 
     */
    @JsonProperty("withPositions")
    public void setWithPositions(Boolean withPositions) {
        this.withPositions = withPositions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("commitId", commitId).append("withPositions", withPositions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(withPositions).append(commitId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilter) == false) {
            return false;
        }
        RequestFilter rhs = ((RequestFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(withPositions, rhs.withPositions).append(commitId, rhs.commitId).isEquals();
    }

}
