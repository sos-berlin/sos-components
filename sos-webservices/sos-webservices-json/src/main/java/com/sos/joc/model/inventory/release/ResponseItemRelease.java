
package com.sos.joc.model.inventory.release;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * include
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "releaseId",
    "path",
    "controllerId",
    "version",
    "releaseDate"
})
public class ResponseItemRelease {

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
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("releaseDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date releaseDate;

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
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("releaseDate")
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("releaseDate")
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("releaseId", releaseId).append("path", path).append("controllerId", controllerId).append("version", version).append("releaseDate", releaseDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(controllerId).append(releaseId).append(version).append(releaseDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseItemRelease) == false) {
            return false;
        }
        ResponseItemRelease rhs = ((ResponseItemRelease) other);
        return new EqualsBuilder().append(path, rhs.path).append(controllerId, rhs.controllerId).append(releaseId, rhs.releaseId).append(version, rhs.version).append(releaseDate, rhs.releaseDate).isEquals();
    }

}
