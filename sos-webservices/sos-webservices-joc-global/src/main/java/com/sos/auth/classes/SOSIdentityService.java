package com.sos.auth.classes;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.model.security.identityservice.IdentityServiceAuthenticationScheme;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSIdentityService {

    private final Long identityServiceId;
    private final String identityServiceName;
    private final IdentityServiceTypes identyServiceType;
    private final String authenticationScheme;

    public SOSIdentityService(DBItemIamIdentityService dbItemIamIdentityService) {
        super();
        this.identityServiceName = dbItemIamIdentityService.getIdentityServiceName();
        this.identyServiceType = dbItemIamIdentityService.getIdentityServiceTypeAsEnum();
        this.identityServiceId = dbItemIamIdentityService.getId();
        this.authenticationScheme = dbItemIamIdentityService.getAuthenticationScheme();
    }

    public String getIdentityServiceName() {
        return identityServiceName;
    }

    public IdentityServiceTypes getIdentyServiceType() {
        return identyServiceType;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public IdentityServiceAuthenticationScheme getIdentityServiceAuthenticationScheme() {
        if (authenticationScheme == null) {
            return IdentityServiceAuthenticationScheme.SINGLE_FACTOR;
        } else {
            return IdentityServiceAuthenticationScheme.fromValue(authenticationScheme);
        }
    }

    public boolean isTwoFactor() {
        return (this.getIdentityServiceAuthenticationScheme() == IdentityServiceAuthenticationScheme.TWO_FACTOR);
    }

    public boolean isSingleFactor() {
        return (this.getIdentityServiceAuthenticationScheme() == IdentityServiceAuthenticationScheme.SINGLE_FACTOR);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SOSIdentityService) == false) {
            return false;
        }
        SOSIdentityService rhs = ((SOSIdentityService) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).isEquals();
    }
     
}
