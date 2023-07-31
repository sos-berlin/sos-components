package com.sos.auth.classes;

import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.model.security.identityservice.IdentityServiceAuthenticationScheme;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSIdentityService {

    private Long identityServiceId;
    private String identityServiceName;
    private IdentityServiceTypes identyServiceType;
    private DBItemIamIdentityService dbItemIamIdentityService;

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
        this.dbItemIamIdentityService = dbItemIamIdentityService;
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
        if (dbItemIamIdentityService == null) {
            return IdentityServiceAuthenticationScheme.SINGLE_FACTOR;
        } else {
            return IdentityServiceAuthenticationScheme.fromValue(dbItemIamIdentityService.getAuthenticationScheme());
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
