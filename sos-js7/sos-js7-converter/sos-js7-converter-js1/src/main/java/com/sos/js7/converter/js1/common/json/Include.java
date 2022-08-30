
package com.sos.js7.converter.js1.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * include
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "include")
@JsonPropertyOrder({
    "file",
    "liveFile"
})
public class Include {

    @JsonProperty("file")
    @JacksonXmlProperty(localName = "file", isAttribute = true)
    private String file;
    @JsonProperty("liveFile")
    @JacksonXmlProperty(localName = "live_file", isAttribute = true)
    private String liveFile;

    @JsonProperty("file")
    @JacksonXmlProperty(localName = "file", isAttribute = true)
    public String getFile() {
        return file;
    }

    @JsonProperty("file")
    @JacksonXmlProperty(localName = "file", isAttribute = true)
    public void setFile(String file) {
        this.file = file;
    }

    @JsonProperty("liveFile")
    @JacksonXmlProperty(localName = "live_file", isAttribute = true)
    public String getLiveFile() {
        return liveFile;
    }

    @JsonProperty("liveFile")
    @JacksonXmlProperty(localName = "live_file", isAttribute = true)
    public void setLiveFile(String liveFile) {
        this.liveFile = liveFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("file", file).append("liveFile", liveFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(liveFile).append(file).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Include) == false) {
            return false;
        }
        Include rhs = ((Include) other);
        return new EqualsBuilder().append(liveFile, rhs.liveFile).append(file, rhs.file).isEquals();
    }

}
