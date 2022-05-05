
package com.sos.joc.model.security.ldap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Ldap Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "simple",
    "expert"
})
public class LdapProperties {

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("simple")
    private LdapSimpleProperties simple;
    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("expert")
    private LdapExpertProperties expert;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LdapProperties() {
    }

    /**
     * 
     * @param expert
     * @param simple
     */
    public LdapProperties(LdapSimpleProperties simple, LdapExpertProperties expert) {
        super();
        this.simple = simple;
        this.expert = expert;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("simple")
    public LdapSimpleProperties getSimple() {
        return simple;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("simple")
    public void setSimple(LdapSimpleProperties simple) {
        this.simple = simple;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("expert")
    public LdapExpertProperties getExpert() {
        return expert;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("expert")
    public void setExpert(LdapExpertProperties expert) {
        this.expert = expert;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("simple", simple).append("expert", expert).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(simple).append(expert).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LdapProperties) == false) {
            return false;
        }
        LdapProperties rhs = ((LdapProperties) other);
        return new EqualsBuilder().append(simple, rhs.simple).append(expert, rhs.expert).isEquals();
    }

}
