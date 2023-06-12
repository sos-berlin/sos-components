package com.sos.joc.db.security;

public class IamFidoRequestsFilter extends SOSHibernateFilter {

    private Long id;
    private Long identityServiceId;
    private String requestId;

    public IamFidoRequestsFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
