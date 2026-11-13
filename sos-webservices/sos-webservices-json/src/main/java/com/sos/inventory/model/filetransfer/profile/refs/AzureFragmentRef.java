
package com.sos.inventory.model.filetransfer.profile.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.HttpHeaders;
import com.sos.inventory.model.filetransfer.profile.common.Rename;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Azure Protocol Fragment Reference
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "httpHeaders",
    "rename"
})
public class AzureFragmentRef
    extends FragmentRefBase
{

    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    private HttpHeaders httpHeaders;
    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    @JsonPropertyDescription("Rename rule. Usable by any profile.")
    private Rename rename;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AzureFragmentRef() {
    }

    /**
     * 
     * @param ref
     * @param rename
     * @param httpHeaders
     */
    public AzureFragmentRef(HttpHeaders httpHeaders, Rename rename, String ref) {
        super(ref);
        this.httpHeaders = httpHeaders;
        this.rename = rename;
    }

    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public Rename getRename() {
        return rename;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public void setRename(Rename rename) {
        this.rename = rename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("httpHeaders", httpHeaders).append("rename", rename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(rename).append(httpHeaders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AzureFragmentRef) == false) {
            return false;
        }
        AzureFragmentRef rhs = ((AzureFragmentRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(rename, rhs.rename).append(httpHeaders, rhs.httpHeaders).isEquals();
    }

}
