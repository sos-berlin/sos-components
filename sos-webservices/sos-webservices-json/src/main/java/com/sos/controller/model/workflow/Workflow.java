
package com.sos.controller.model.workflow;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;


/**
 * workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "isCurrentVersion"
})
public class Workflow
    extends com.sos.inventory.model.workflow.Workflow
{

    @JsonProperty("isCurrentVersion")
    private Boolean isCurrentVersion = true;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflow() {
    }

    /**
     * 
     * @param documentationPath
     * @param path
     * @param instructions
     * @param versionId
     * @param isCurrentVersion
     * @param orderRequirements
     * @param jobs
     * @param tYPE
     * @param title
     */
    public Workflow(Boolean isCurrentVersion, String path, String versionId, Requirements orderRequirements, List<Instruction> instructions, String title, String documentationPath, Jobs jobs) {
        super(path, versionId, orderRequirements, instructions, title, documentationPath, jobs);
        this.isCurrentVersion = isCurrentVersion;
    }

    @JsonProperty("isCurrentVersion")
    public Boolean getIsCurrentVersion() {
        return isCurrentVersion;
    }

    @JsonProperty("isCurrentVersion")
    public void setIsCurrentVersion(Boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("isCurrentVersion", isCurrentVersion).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(isCurrentVersion).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Workflow) == false) {
            return false;
        }
        Workflow rhs = ((Workflow) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(isCurrentVersion, rhs.isCurrentVersion).isEquals();
    }

}
