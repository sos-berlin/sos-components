
package com.sos.inventory.model.filetransfer.fragments.protocol.jump;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.Processing;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SFTP processing options on the jump host.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "processing",
    "platform"
})
public class JumpProcessing {

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    private Processing processing;
    /**
     * Specifies the operating system platform of the Jump host: 'unix' or 'windows'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("platform")
    private String platform;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JumpProcessing() {
    }

    /**
     * 
     * @param processing
     * @param platform
     */
    public JumpProcessing(Processing processing, String platform) {
        super();
        this.processing = processing;
        this.platform = platform;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public Processing getProcessing() {
        return processing;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }

    /**
     * Specifies the operating system platform of the Jump host: 'unix' or 'windows'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    /**
     * Specifies the operating system platform of the Jump host: 'unix' or 'windows'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("platform")
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("processing", processing).append("platform", platform).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(processing).append(platform).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JumpProcessing) == false) {
            return false;
        }
        JumpProcessing rhs = ((JumpProcessing) other);
        return new EqualsBuilder().append(processing, rhs.processing).append(platform, rhs.platform).isEquals();
    }

}
