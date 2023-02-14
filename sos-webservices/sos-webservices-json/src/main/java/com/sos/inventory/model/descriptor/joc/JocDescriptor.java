
package com.sos.inventory.model.descriptor.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.Media;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Joc Item of a Deployment Descriptor
 * <p>
 * JS7 JOC Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "ordering",
    "target",
    "media",
    "installation",
    "configuration"
})
public class JocDescriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;
    @JsonProperty("target")
    private Target target;
    /**
     * Deployment Descriptor Media Schema
     * <p>
     * JS7 JOC Descriptor Media Schema
     * (Required)
     * 
     */
    @JsonProperty("media")
    @JsonPropertyDescription("JS7 JOC Descriptor Media Schema")
    private Media media;
    /**
     * Deployment Descriptor Installation Schema
     * <p>
     * JS7 JOC Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    @JsonPropertyDescription("JS7 JOC Descriptor Installation Schema")
    private JocInstallation installation;
    @JsonProperty("configuration")
    private Configuration configuration;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JocDescriptor() {
    }

    /**
     * 
     * @param configuration
     * @param ordering
     * @param installation
     * @param media
     * @param target
     */
    public JocDescriptor(Integer ordering, Target target, Media media, JocInstallation installation, Configuration configuration) {
        super();
        this.ordering = ordering;
        this.target = target;
        this.media = media;
        this.installation = installation;
        this.configuration = configuration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @JsonProperty("target")
    public Target getTarget() {
        return target;
    }

    @JsonProperty("target")
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Deployment Descriptor Media Schema
     * <p>
     * JS7 JOC Descriptor Media Schema
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
     * JS7 JOC Descriptor Media Schema
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
     * JS7 JOC Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    public JocInstallation getInstallation() {
        return installation;
    }

    /**
     * Deployment Descriptor Installation Schema
     * <p>
     * JS7 JOC Descriptor Installation Schema
     * (Required)
     * 
     */
    @JsonProperty("installation")
    public void setInstallation(JocInstallation installation) {
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
        return new ToStringBuilder(this).append("ordering", ordering).append("target", target).append("media", media).append("installation", installation).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(media).append(configuration).append(ordering).append(target).append(installation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocDescriptor) == false) {
            return false;
        }
        JocDescriptor rhs = ((JocDescriptor) other);
        return new EqualsBuilder().append(media, rhs.media).append(configuration, rhs.configuration).append(ordering, rhs.ordering).append(target, rhs.target).append(installation, rhs.installation).isEquals();
    }

}
