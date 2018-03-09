
package com.sos.joc.model.jobChain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fileOrderSource (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "directory",
    "nextNode",
    "regex"
})
public class FileWatchingNodeP {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    private String directory;
    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    private String nextNode;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    private String regex;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    public String getDirectory() {
        return directory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    @JacksonXmlProperty(localName = "directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    public String getNextNode() {
        return nextNode;
    }

    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public String getRegex() {
        return regex;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("directory", directory).append("nextNode", nextNode).append("regex", regex).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(directory).append(nextNode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileWatchingNodeP) == false) {
            return false;
        }
        FileWatchingNodeP rhs = ((FileWatchingNodeP) other);
        return new EqualsBuilder().append(regex, rhs.regex).append(directory, rhs.directory).append(nextNode, rhs.nextNode).isEquals();
    }

}
