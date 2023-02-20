
package com.sos.inventory.model.descriptor.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.common.Media;
import com.sos.inventory.model.descriptor.common.Target;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Joc Instance of a Deployment Descriptor
 * <p>
 * JS7 JOC Instance Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "instanceId",
    "target",
    "media",
    "installation",
    "configuration"
})
public class JocInstanceDescriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instanceId")
    private Integer instanceId;
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
    public JocInstanceDescriptor() {
    }

    /**
     * 
     * @param instanceId
     * @param configuration
     * @param installation
     * @param media
     * @param target
     */
    public JocInstanceDescriptor(Integer instanceId, Target target, Media media, JocInstallation installation, Configuration configuration) {
        super();
        this.instanceId = instanceId;
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
    @JsonProperty("instanceId")
    public Integer getInstanceId() {
        return instanceId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instanceId")
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
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
        return new ToStringBuilder(this).append("instanceId", instanceId).append("target", target).append("media", media).append("installation", installation).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instanceId).append(media).append(configuration).append(target).append(installation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocInstanceDescriptor) == false) {
            return false;
        }
        JocInstanceDescriptor rhs = ((JocInstanceDescriptor) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(media, rhs.media).append(configuration, rhs.configuration).append(target, rhs.target).append(installation, rhs.installation).isEquals();
    }

}
