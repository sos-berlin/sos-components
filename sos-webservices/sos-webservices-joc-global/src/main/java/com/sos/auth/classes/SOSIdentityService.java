package com.sos.auth.classes;

import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.model.security.IdentityServiceAuthenticationScheme;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSIdentityService {

    private Long identityServiceId;
    private String identityServiceName;
    private IdentityServiceTypes identyServiceType;
    private IdentityServiceAuthenticationScheme identityServiceAuthenticationScheme;
    private boolean singleFactorCert;
    private boolean singleFactorPwd;

    public SOSIdentityService(Long identityServiceId, String identityServiceName, IdentityServiceTypes identyServiceType) {
        super();
        this.identityServiceName = identityServiceName;
        this.identyServiceType = identyServiceType;
        this.identityServiceId = identityServiceId;
    }

    public SOSIdentityService(DBItemIamIdentityService dbItemIamIdentityService) {
        super();
        this.identityServiceName = dbItemIamIdentityService.getIdentityServiceName();
        this.identyServiceType = IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType());
        this.identityServiceId = dbItemIamIdentityService.getId();
        if (identyServiceType == IdentityServiceTypes.SHIRO) {
            this.identityServiceAuthenticationScheme = IdentityServiceAuthenticationScheme.SINGLE_FACTOR;
            this.singleFactorCert = false;
            this.singleFactorPwd = true;
        } else {
            this.identityServiceAuthenticationScheme = IdentityServiceAuthenticationScheme.fromValue(dbItemIamIdentityService
                    .getAuthenticationScheme());
            this.singleFactorCert = dbItemIamIdentityService.getSingleFactorCert();
            this.singleFactorPwd = dbItemIamIdentityService.getSingleFactorPwd();
        }
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

    public boolean isSingleFactorCert() {
        return singleFactorCert;
    }

    public void setSingleFactorCert(boolean singleFactorCert) {
        this.singleFactorCert = singleFactorCert;
    }

    public boolean isSingleFactorPwd() {
        return singleFactorPwd;
    }

    public void setSingleFactorPwd(boolean singleFactorPwd) {
        this.singleFactorPwd = singleFactorPwd;
    }

    public boolean isTwoFactor() {
        return (identityServiceAuthenticationScheme == IdentityServiceAuthenticationScheme.TWO_FACTOR);
    }

    public boolean isSingleFactor() {
        return (identityServiceAuthenticationScheme == IdentityServiceAuthenticationScheme.SINGLE_FACTOR);
    }

}
