
package com.sos.jobscheduler.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.Deletable;
import com.sos.jobscheduler.model.deploy.SignedObject;
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
    "versionId",
    "change",
    "delete"
})
public class UpdateRepo
    extends Command
    implements ICommandable
{

    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("change")
    private List<SignedObject> change = null;
    @JsonProperty("delete")
    private List<Deletable> delete = null;

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
    public List<Deletable> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<Deletable> delete) {
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
