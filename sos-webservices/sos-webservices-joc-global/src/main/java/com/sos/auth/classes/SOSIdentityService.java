package com.sos.auth.classes;

import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.model.security.identityservice.IdentityServiceAuthenticationScheme;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSIdentityService {

    private Long identityServiceId;
    private String identityServiceName;
    private IdentityServiceTypes identyServiceType;
    private IdentityServiceAuthenticationScheme identityServiceAuthenticationScheme;
    private boolean secondFactor;

    public SOSIdentityService(Long identityServiceId, String identityServiceName, IdentityServiceTypes identyServiceType) {
        super();
        this.identityServiceName = identityServiceName;
        this.identyServiceType = identyServiceType;
        this.identityServiceId = identityServiceId;
    }

    public SOSIdentityService(String identityServiceName, IdentityServiceTypes identyServiceType) {
        super();
        this.identityServiceName = identityServiceName;
        this.identyServiceType = identyServiceType;
    }

    public SOSIdentityService(DBItemIamIdentityService dbItemIamIdentityService) {
        super();
        this.identityServiceName = dbItemIamIdentityService.getIdentityServiceName();
        try {
            this.identyServiceType = IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType());
        } catch (IllegalArgumentException e) {
            this.identyServiceType = IdentityServiceTypes.UNKNOWN;
        }
        this.identityServiceId = dbItemIamIdentityService.getId();
        this.identityServiceAuthenticationScheme = IdentityServiceAuthenticationScheme.fromValue(dbItemIamIdentityService.getAuthenticationScheme());

    }

    public String getIdentityServiceName() {
        return identityServiceName;
    }

    public IdentityServiceTypes getIdentyServiceType() {
        return identyServiceType;
    }

    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    public void setIdentyServiceType(IdentityServiceTypes identyServiceType) {
        this.identyServiceType = identyServiceType;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public IdentityServiceAuthenticationScheme getIdentityServiceAuthenticationScheme() {
        return identityServiceAuthenticationScheme;
    }

    public void setIdentityServiceAuthenticationScheme(IdentityServiceAuthenticationScheme identityServiceAuthenticationScheme) {
        this.identityServiceAuthenticationScheme = identityServiceAuthenticationScheme;
    }

    public boolean isSecondFactor() {
        return secondFactor;
    }

    public void setSecondFactor(boolean secondFactor) {
        this.secondFactor = secondFactor;
    }


    public boolean isTwoFactor() {
        return (identityServiceAuthenticationScheme == IdentityServiceAuthenticationScheme.TWO_FACTOR);
    }

    public boolean isSingleFactor() {
        return (identityServiceAuthenticationScheme == IdentityServiceAuthenticationScheme.SINGLE_FACTOR);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identityServiceName == null) ? 0 : identityServiceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SOSIdentityService other = (SOSIdentityService) obj;
        if (identityServiceName == null) {
            if (other.identityServiceName != null)
                return false;
        } else if (!identityServiceName.equals(other.identityServiceName))
            return false;
        return true;
    }

  
}
