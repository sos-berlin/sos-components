
package com.sos.joc.model.xmleditor.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor reassign schema configuration in
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "objectType",
    "configuration",
    "uri",
    "fileName",
    "fileContent"
})
public class SchemaReassignConfiguration {

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
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private String configuration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("uri")
    private String uri;
    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("fileName")
    private String fileName;
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("fileContent")
    private String fileContent;

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

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public String getConfiguration() {
        return configuration;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("fileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("fileContent")
    public String getFileContent() {
        return fileContent;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("fileContent")
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("objectType", objectType).append("configuration", configuration).append("uri", uri).append("fileName", fileName).append("fileContent", fileContent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileName).append(configuration).append(jobschedulerId).append(uri).append(fileContent).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaReassignConfiguration) == false) {
            return false;
        }
        SchemaReassignConfiguration rhs = ((SchemaReassignConfiguration) other);
        return new EqualsBuilder().append(fileName, rhs.fileName).append(configuration, rhs.configuration).append(jobschedulerId, rhs.jobschedulerId).append(uri, rhs.uri).append(fileContent, rhs.fileContent).append(objectType, rhs.objectType).isEquals();
    }

}
