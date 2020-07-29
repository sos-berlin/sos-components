
package com.sos.jobscheduler.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.SignedObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Replace Repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "versionId",
    "objects"
})
public class ReplaceRepo
    extends Command
{

    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("objects")
    private List<SignedObject> objects = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ReplaceRepo() {
    }

    /**
     * 
     * @param versionId
     * @param objects
     */
    public ReplaceRepo(String versionId, List<SignedObject> objects) {
        super();
        this.versionId = versionId;
        this.objects = objects;
    }

    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @JsonProperty("objects")
    public List<SignedObject> getObjects() {
        return objects;
    }

    @JsonProperty("objects")
    public void setObjects(List<SignedObject> objects) {
        this.objects = objects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("versionId", versionId).append("objects", objects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(versionId).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReplaceRepo) == false) {
            return false;
        }
        ReplaceRepo rhs = ((ReplaceRepo) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(versionId, rhs.versionId).append(objects, rhs.objects).isEquals();
    }

}
