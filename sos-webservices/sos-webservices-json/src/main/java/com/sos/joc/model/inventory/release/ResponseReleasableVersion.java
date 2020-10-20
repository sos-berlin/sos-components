
package com.sos.joc.model.inventory.release;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for joe requests
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "releaseId",
    "releasePath",
    "versionDate",
    "versions"
})
public class ResponseReleasableVersion {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    private Long releaseId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("releasePath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String releasePath;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date versionDate;
    @JsonProperty("versions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseItemRelease> versions = new LinkedHashSet<ResponseItemRelease>();

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    public Long getReleaseId() {
        return releaseId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("releasePath")
    public String getReleasePath() {
        return releasePath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("releasePath")
    public void setReleasePath(String releasePath) {
        this.releasePath = releasePath;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public Date getVersionDate() {
        return versionDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    @JsonProperty("versions")
    public Set<ResponseItemRelease> getVersions() {
        return versions;
    }

    @JsonProperty("versions")
    public void setVersions(Set<ResponseItemRelease> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("releaseId", releaseId).append("releasePath", releasePath).append("versionDate", versionDate).append("versions", versions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(releasePath).append(id).append(releaseId).append(versionDate).append(versions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseReleasableVersion) == false) {
            return false;
        }
        ResponseReleasableVersion rhs = ((ResponseReleasableVersion) other);
        return new EqualsBuilder().append(releasePath, rhs.releasePath).append(id, rhs.id).append(releaseId, rhs.releaseId).append(versionDate, rhs.versionDate).append(versions, rhs.versions).isEquals();
    }

}
