
package com.sos.joc.model.board;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "noticeBoardPath",
    "workflowPaths"
})
public class ExpectedNoticesPerBoard {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    private String noticeBoardPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowPaths = new LinkedHashSet<String>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public String getNoticeBoardPath() {
        return noticeBoardPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public void setNoticeBoardPath(String noticeBoardPath) {
        this.noticeBoardPath = noticeBoardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPaths")
    public Set<String> getWorkflowPaths() {
        return workflowPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(Set<String> workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("noticeBoardPath", noticeBoardPath).append("workflowPaths", workflowPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(noticeBoardPath).append(workflowPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectedNoticesPerBoard) == false) {
            return false;
        }
        ExpectedNoticesPerBoard rhs = ((ExpectedNoticesPerBoard) other);
        return new EqualsBuilder().append(noticeBoardPath, rhs.noticeBoardPath).append(workflowPaths, rhs.workflowPaths).isEquals();
    }

}
