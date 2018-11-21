package com.sos.auth.shiro.db;

import com.sos.commons.hibernate.SOSHibernateFilter;

public class SOSUserFilter extends SOSHibernateFilter {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSUserFilter";
    private String userName;

    public SOSUserFilter() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
