package com.sos.joc.db.security;

import com.sos.commons.hibernate.SOSHibernateFilter;

public class IamAccountFilter extends SOSHibernateFilter {

    private Long id;
    private String accountName;

    public IamAccountFilter() {

    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
