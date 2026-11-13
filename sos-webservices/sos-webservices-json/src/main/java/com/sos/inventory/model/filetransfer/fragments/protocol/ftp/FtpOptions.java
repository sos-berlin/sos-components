
package com.sos.inventory.model.filetransfer.fragments.protocol.ftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTP-specific options.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "passiveMode"
})
public class FtpOptions {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("passiveMode")
    private String passiveMode;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpOptions() {
    }

    /**
     * 
     * @param passiveMode
     */
    public FtpOptions(String passiveMode) {
        super();
        this.passiveMode = passiveMode;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("passiveMode")
    public String getPassiveMode() {
        return passiveMode;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("passiveMode")
    public void setPassiveMode(String passiveMode) {
        this.passiveMode = passiveMode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("passiveMode", passiveMode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(passiveMode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FtpOptions) == false) {
            return false;
        }
        FtpOptions rhs = ((FtpOptions) other);
        return new EqualsBuilder().append(passiveMode, rhs.passiveMode).isEquals();
    }

}
