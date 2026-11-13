
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
 * WebDAV Protocol Fragment Reference
 * <p>
 * allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "httpHeaders",
    "rename",
    "postProcessing"
})
public class WebdavFragmentRef
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
    @JsonProperty("postProcessing")
    private PostProcessing postProcessing;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WebdavFragmentRef() {
    }

    /**
     * 
     * @param postProcessing
     * @param ref
     * @param rename
     * @param httpHeaders
     */
    public WebdavFragmentRef(HttpHeaders httpHeaders, Rename rename, PostProcessing postProcessing, String ref) {
        super(ref);
        this.httpHeaders = httpHeaders;
        this.rename = rename;
        this.postProcessing = postProcessing;
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

    @JsonProperty("postProcessing")
    public PostProcessing getPostProcessing() {
        return postProcessing;
    }

    @JsonProperty("postProcessing")
    public void setPostProcessing(PostProcessing postProcessing) {
        this.postProcessing = postProcessing;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("httpHeaders", httpHeaders).append("rename", rename).append("postProcessing", postProcessing).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(postProcessing).append(httpHeaders).append(rename).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WebdavFragmentRef) == false) {
            return false;
        }
        WebdavFragmentRef rhs = ((WebdavFragmentRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(postProcessing, rhs.postProcessing).append(httpHeaders, rhs.httpHeaders).append(rename, rhs.rename).isEquals();
    }

}
