
package com.sos.inventory.model.descriptor.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.Installation;
import com.sos.inventory.model.descriptor.common.Media;
import com.sos.inventory.model.descriptor.common.Target;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller Item of a Deployment Descriptor
 * <p>
 * JS7 Controller Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "target",
    "media",
    "installation",
    "configuration"
})
public class ControllerDescriptor {

    /**
     * Deployment Descriptor Target Schema
     * <p>
     * JS7 Deployment Descriptor Target Schema
     * 
     */
    @JsonProperty("target")
    @JsonPropertyDescription("JS7 Deployment Descriptor Target Schema")
    private Target target;
    /**
     * Deployment Descriptor Media Schema
     * <p>
     * JS7 Deployment Descriptor Media Schema
     * (Required)
     * 
     */
    @JsonProperty("media")
    @JsonPropertyDescription("JS7 Deployment Descriptor Media Schema")
    private Media media;
    /**
     * Deployment Descriptor Installation Schema
     * <p>
     * JS7 Deployment Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    @JsonPropertyDescription("JS7 Deployment Descriptor Installation Schema")
    private Installation installation;
    @JsonProperty("configuration")
    private Configuration configuration;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerDescriptor() {
    }

    /**
     * 
     * @param configuration
     * @param installation
     * @param media
     * @param target
     */
    public ControllerDescriptor(Target target, Media media, Installation installation, Configuration configuration) {
        super();
        this.target = target;
        this.media = media;
        this.installation = installation;
        this.configuration = configuration;
    }

    /**
     * Deployment Descriptor Target Schema
     * <p>
     * JS7 Deployment Descriptor Target Schema
     * 
     */
    @JsonProperty("target")
    public Target getTarget() {
        return target;
    }

    /**
     * Deployment Descriptor Target Schema
     * <p>
     * JS7 Deployment Descriptor Target Schema
     * 
     */
    @JsonProperty("target")
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Deployment Descriptor Media Schema
     * <p>
     * JS7 Deployment Descriptor Media Schema
     * (Required)
     * 
     */
    @JsonProperty("media")
    public Media getMedia() {
        return media;
    }

    /**
     * Deployment Descriptor Media Schema
     * <p>
     * JS7 Deployment Descriptor Media Schema
     * (Required)
     * 
     */
    @JsonProperty("media")
    public void setMedia(Media media) {
        this.media = media;
    }

    /**
     * Deployment Descriptor Installation Schema
     * <p>
     * JS7 Deployment Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    public Installation getInstallation() {
        return installation;
    }

    /**
     * Deployment Descriptor Installation Schema
     * <p>
     * JS7 Deployment Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    public void setInstallation(Installation installation) {
        this.installation = installation;
    }

    @JsonProperty("configuration")
    public Configuration getConfiguration() {
        return configuration;
    }

    @JsonProperty("configuration")
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("target", target).append("media", media).append("installation", installation).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(media).append(configuration).append(target).append(installation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerDescriptor) == false) {
            return false;
        }
        ControllerDescriptor rhs = ((ControllerDescriptor) other);
        return new EqualsBuilder().append(media, rhs.media).append(configuration, rhs.configuration).append(target, rhs.target).append(installation, rhs.installation).isEquals();
    }

}
