
package com.sos.controller.model.cluster;

import java.net.URI;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * CLuster Watcher
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "uri"
})
public class ClusterWatcher {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    private URI uri;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterWatcher() {
    }

    /**
     * 
     * @param uri
     */
    public ClusterWatcher(URI uri) {
        super();
        this.uri = uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public URI getUri() {
        return uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("uri", uri).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uri).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterWatcher) == false) {
            return false;
        }
        ClusterWatcher rhs = ((ClusterWatcher) other);
        return new EqualsBuilder().append(uri, rhs.uri).isEquals();
    }

}
