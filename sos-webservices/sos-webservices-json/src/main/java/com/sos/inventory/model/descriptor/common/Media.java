
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Media Schema
 * <p>
 * JS7 JOC Descriptor Media Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "release",
    "tarball"
})
public class Media {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("release")
    private String release;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tarball")
    private String tarball;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Media() {
    }

    /**
     * 
     * @param release
     * @param tarball
     */
    public Media(String release, String tarball) {
        super();
        this.release = release;
        this.tarball = tarball;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("release")
    public String getRelease() {
        return release;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("release")
    public void setRelease(String release) {
        this.release = release;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tarball")
    public String getTarball() {
        return tarball;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tarball")
    public void setTarball(String tarball) {
        this.tarball = tarball;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("release", release).append("tarball", tarball).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tarball).append(release).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Media) == false) {
            return false;
        }
        Media rhs = ((Media) other);
        return new EqualsBuilder().append(tarball, rhs.tarball).append(release, rhs.release).isEquals();
    }

}
