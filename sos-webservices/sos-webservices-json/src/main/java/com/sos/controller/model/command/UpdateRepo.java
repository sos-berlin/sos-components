
package com.sos.controller.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.sign.SignedObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Update Repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "versionId",
    "change",
    "delete"
})
public class UpdateRepo
    extends Command
{

    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("change")
    private List<SignedObject> change = null;
    @JsonProperty("delete")
    private List<IDeployObject> delete = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public UpdateRepo() {
    }

    /**
     * 
     * @param versionId
     * @param change
     * @param delete
     */
    public UpdateRepo(String versionId, List<SignedObject> change, List<IDeployObject> delete) {
        super();
        this.versionId = versionId;
        this.change = change;
        this.delete = delete;
    }

    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @JsonProperty("change")
    public List<SignedObject> getChange() {
        return change;
    }

    @JsonProperty("change")
    public void setChange(List<SignedObject> change) {
        this.change = change;
    }

    @JsonProperty("delete")
    public List<IDeployObject> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<IDeployObject> delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("versionId", versionId).append("change", change).append("delete", delete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(versionId).append(delete).append(change).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UpdateRepo) == false) {
            return false;
        }
        UpdateRepo rhs = ((UpdateRepo) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(versionId, rhs.versionId).append(delete, rhs.delete).append(change, rhs.change).isEquals();
    }

}
