
package com.sos.inventory.model.filetransfer.fragments;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Collection of all Protocol fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "fragments"
})
public class ProtocolFragments {

    /**
     * List of protocol fragments ( Azure Blob Storage, FTP/FTPS, HTTP/HTTPS, Jump, SFTP, SMB, WebDAV).
     * 
     */
    @JsonProperty("fragments")
    @JsonPropertyDescription("List of protocol fragments ( Azure Blob Storage, FTP/FTPS, HTTP/HTTPS, Jump, SFTP, SMB, WebDAV).")
    private List<AProtocolFragment> fragments = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolFragments() {
    }

    /**
     * 
     * @param fragments
     */
    public ProtocolFragments(List<AProtocolFragment> fragments) {
        super();
        this.fragments = fragments;
    }

    /**
     * List of protocol fragments ( Azure Blob Storage, FTP/FTPS, HTTP/HTTPS, Jump, SFTP, SMB, WebDAV).
     * 
     */
    @JsonProperty("fragments")
    public List<AProtocolFragment> getFragments() {
        return fragments;
    }

    /**
     * List of protocol fragments ( Azure Blob Storage, FTP/FTPS, HTTP/HTTPS, Jump, SFTP, SMB, WebDAV).
     * 
     */
    @JsonProperty("fragments")
    public void setFragments(List<AProtocolFragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fragments", fragments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fragments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProtocolFragments) == false) {
            return false;
        }
        ProtocolFragments rhs = ((ProtocolFragments) other);
        return new EqualsBuilder().append(fragments, rhs.fragments).isEquals();
    }

}
