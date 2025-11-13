
package com.sos.joc.model.note.common;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * metadata
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "created",
    "createdBy",
    "modified",
    "modifiedBy",
    "postCount",
    "participantCount",
    "severity",
    "displayPreferences"
})
public class Metadata {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("createdBy")
    private Author createdBy;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("modifiedBy")
    private Author modifiedBy;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postCount")
    private Integer postCount;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("participantCount")
    private Integer participantCount;
    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Severity severity = Severity.fromValue("NORMAL");
    /**
     * DisplayPreferences
     * <p>
     * 
     * 
     */
    @JsonProperty("displayPreferences")
    private DisplayPreferences displayPreferences;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("createdBy")
    public Author getCreatedBy() {
        return createdBy;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("createdBy")
    public void setCreatedBy(Author createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("modifiedBy")
    public Author getModifiedBy() {
        return modifiedBy;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("modifiedBy")
    public void setModifiedBy(Author modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postCount")
    public Integer getPostCount() {
        return postCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postCount")
    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("participantCount")
    public Integer getParticipantCount() {
        return participantCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("participantCount")
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public Severity getSeverity() {
        return severity;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * DisplayPreferences
     * <p>
     * 
     * 
     */
    @JsonProperty("displayPreferences")
    public DisplayPreferences getDisplayPreferences() {
        return displayPreferences;
    }

    /**
     * DisplayPreferences
     * <p>
     * 
     * 
     */
    @JsonProperty("displayPreferences")
    public void setDisplayPreferences(DisplayPreferences displayPreferences) {
        this.displayPreferences = displayPreferences;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("created", created).append("createdBy", createdBy).append("modified", modified).append("modifiedBy", modifiedBy).append("postCount", postCount).append("participantCount", participantCount).append("severity", severity).append("displayPreferences", displayPreferences).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(createdBy).append(created).append(participantCount).append(modified).append(postCount).append(displayPreferences).append(modifiedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Metadata) == false) {
            return false;
        }
        Metadata rhs = ((Metadata) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(createdBy, rhs.createdBy).append(created, rhs.created).append(participantCount, rhs.participantCount).append(modified, rhs.modified).append(postCount, rhs.postCount).append(displayPreferences, rhs.displayPreferences).append(modifiedBy, rhs.modifiedBy).isEquals();
    }

}
