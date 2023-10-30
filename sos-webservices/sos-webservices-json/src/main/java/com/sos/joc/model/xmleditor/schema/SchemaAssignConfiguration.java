
package com.sos.joc.model.xmleditor.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * xmleditor assign schema configuration in
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objectType",
    "uri",
    "fileName",
    "fileContent"
})
public class SchemaAssignConfiguration {

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
        return new ToStringBuilder(this).append("objectType", objectType).append("uri", uri).append("fileName", fileName).append("fileContent", fileContent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileName).append(uri).append(fileContent).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaAssignConfiguration) == false) {
            return false;
        }
        SchemaAssignConfiguration rhs = ((SchemaAssignConfiguration) other);
        return new EqualsBuilder().append(fileName, rhs.fileName).append(uri, rhs.uri).append(fileContent, rhs.fileContent).append(objectType, rhs.objectType).isEquals();
    }

}
