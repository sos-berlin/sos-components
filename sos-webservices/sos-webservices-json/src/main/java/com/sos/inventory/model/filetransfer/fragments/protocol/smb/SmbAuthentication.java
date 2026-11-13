
package com.sos.inventory.model.filetransfer.fragments.protocol.smb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SMB authentication. Provide exactly one of 'anonymous', 'guest', 'ntlm', 'kerberos', 'spnego'.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "anonymous",
    "guest",
    "ntlm",
    "kerberos",
    "spnego"
})
public class SmbAuthentication {

    /**
     * Anonymous authentication (no credentials).
     * 
     */
    @JsonProperty("anonymous")
    @JsonPropertyDescription("Anonymous authentication (no credentials).")
    private SmbAuthAnonymous anonymous;
    /**
     * Guest authentication.
     * 
     */
    @JsonProperty("guest")
    @JsonPropertyDescription("Guest authentication.")
    private SmbAuthGuest guest;
    /**
     * NTLM authentication.
     * 
     */
    @JsonProperty("ntlm")
    @JsonPropertyDescription("NTLM authentication.")
    private SmbAuthNtlm ntlm;
    /**
     * Kerberos authentication.
     * 
     */
    @JsonProperty("kerberos")
    @JsonPropertyDescription("Kerberos authentication.")
    private SmbAuthKerberos kerberos;
    /**
     * SPNEGO authentication.
     * 
     */
    @JsonProperty("spnego")
    @JsonPropertyDescription("SPNEGO authentication.")
    private SmbAuthSpnego spnego;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SmbAuthentication() {
    }

    /**
     * 
     * @param ntlm
     * @param spnego
     * @param anonymous
     * @param guest
     * @param kerberos
     */
    public SmbAuthentication(SmbAuthAnonymous anonymous, SmbAuthGuest guest, SmbAuthNtlm ntlm, SmbAuthKerberos kerberos, SmbAuthSpnego spnego) {
        super();
        this.anonymous = anonymous;
        this.guest = guest;
        this.ntlm = ntlm;
        this.kerberos = kerberos;
        this.spnego = spnego;
    }

    /**
     * Anonymous authentication (no credentials).
     * 
     */
    @JsonProperty("anonymous")
    public SmbAuthAnonymous getAnonymous() {
        return anonymous;
    }

    /**
     * Anonymous authentication (no credentials).
     * 
     */
    @JsonProperty("anonymous")
    public void setAnonymous(SmbAuthAnonymous anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * Guest authentication.
     * 
     */
    @JsonProperty("guest")
    public SmbAuthGuest getGuest() {
        return guest;
    }

    /**
     * Guest authentication.
     * 
     */
    @JsonProperty("guest")
    public void setGuest(SmbAuthGuest guest) {
        this.guest = guest;
    }

    /**
     * NTLM authentication.
     * 
     */
    @JsonProperty("ntlm")
    public SmbAuthNtlm getNtlm() {
        return ntlm;
    }

    /**
     * NTLM authentication.
     * 
     */
    @JsonProperty("ntlm")
    public void setNtlm(SmbAuthNtlm ntlm) {
        this.ntlm = ntlm;
    }

    /**
     * Kerberos authentication.
     * 
     */
    @JsonProperty("kerberos")
    public SmbAuthKerberos getKerberos() {
        return kerberos;
    }

    /**
     * Kerberos authentication.
     * 
     */
    @JsonProperty("kerberos")
    public void setKerberos(SmbAuthKerberos kerberos) {
        this.kerberos = kerberos;
    }

    /**
     * SPNEGO authentication.
     * 
     */
    @JsonProperty("spnego")
    public SmbAuthSpnego getSpnego() {
        return spnego;
    }

    /**
     * SPNEGO authentication.
     * 
     */
    @JsonProperty("spnego")
    public void setSpnego(SmbAuthSpnego spnego) {
        this.spnego = spnego;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("anonymous", anonymous).append("guest", guest).append("ntlm", ntlm).append("kerberos", kerberos).append("spnego", spnego).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(anonymous).append(guest).append(kerberos).append(ntlm).append(spnego).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SmbAuthentication) == false) {
            return false;
        }
        SmbAuthentication rhs = ((SmbAuthentication) other);
        return new EqualsBuilder().append(anonymous, rhs.anonymous).append(guest, rhs.guest).append(kerberos, rhs.kerberos).append(ntlm, rhs.ntlm).append(spnego, rhs.spnego).isEquals();
    }

}
