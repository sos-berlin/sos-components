
package com.sos.inventory.model.script;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;


/**
 * script
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "version",
    "title",
    "documentationName",
    "script"
})
public class Script implements IInventoryObject, IConfigurationObject, IReleaseObject
{

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.2.0";
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    private String script;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Script() {
    }

    /**
     * 
     * @param documentationName
     * @param title
     * @param version
     * @param script
     */
    public Script(String version, String title, String documentationName, String script) {
        super();
        this.version = version;
        this.title = title;
        this.documentationName = documentationName;
        this.script = script;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    public String getScript() {
        return script;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("title", title).append("documentationName", documentationName).append("script", script).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationName).append(title).append(version).append(script).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Script) == false) {
            return false;
        }
        Script rhs = ((Script) other);
        return new EqualsBuilder().append(documentationName, rhs.documentationName).append(title, rhs.title).append(version, rhs.version).append(script, rhs.script).isEquals();
    }

}
