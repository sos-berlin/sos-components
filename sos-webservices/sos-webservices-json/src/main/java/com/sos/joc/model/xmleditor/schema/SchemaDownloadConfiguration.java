
package com.sos.joc.model.xmleditor.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor schema download configuration in
 * <p>
 * schemaIdentifier only for OTHER
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "objectType",
    "show",
    "schemaIdentifier"
})
public class SchemaDownloadConfiguration {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ObjectType objectType;
    @JsonProperty("show")
    private Boolean show;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("show")
    public Boolean getShow() {
        return show;
    }

    @JsonProperty("show")
    public void setShow(Boolean show) {
        this.show = show;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schemaIdentifier")
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schemaIdentifier")
    public void setSchemaIdentifier(String schemaIdentifier) {
        this.schemaIdentifier = schemaIdentifier;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("objectType", objectType).append("show", show).append("schemaIdentifier", schemaIdentifier).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(show).append(schemaIdentifier).append(jobschedulerId).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaDownloadConfiguration) == false) {
            return false;
        }
        SchemaDownloadConfiguration rhs = ((SchemaDownloadConfiguration) other);
        return new EqualsBuilder().append(show, rhs.show).append(schemaIdentifier, rhs.schemaIdentifier).append(jobschedulerId, rhs.jobschedulerId).append(objectType, rhs.objectType).isEquals();
    }

}
