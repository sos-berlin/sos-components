package com.sos.commons.hibernate;

import java.sql.DatabaseMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public class SOSHibernateDatabaseMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateDatabaseMetaData.class);

    // TODO declare and use the Dbms here instead of SOSHibernateFactory.Dbms
    private final Dbms dbms;

    private String productName;
    private String productVersion;
    private int majorVersion;
    private int minorVersion;
    private boolean supportJsonReturningClob;
    private boolean isSet;

    protected SOSHibernateDatabaseMetaData(Dbms dbms) {
        this.dbms = dbms == null ? Dbms.UNKNOWN : dbms;
        setSupportJsonReturningClob();
    }

    protected void set(DatabaseMetaData metaData) {
        if (metaData == null || isSet) {
            isSet = true;// ??? when metaData null
            return;
        }
        doSet(metaData);
    }

    private void doSet(DatabaseMetaData metaData) {
        isSet = true;
        try {
            productName = metaData.getDatabaseProductName();
            productVersion = metaData.getDatabaseProductVersion();
            majorVersion = metaData.getDatabaseMajorVersion();
            minorVersion = metaData.getDatabaseMinorVersion();
        } catch (Throwable e) {
            LOGGER.warn(String.format("[doSet]%s", e.toString()), e);
        }
        setSupportJsonReturningClob();
    }

    private void setSupportJsonReturningClob() {
        supportJsonReturningClob = true;

        if (dbms.equals(Dbms.ORACLE)) {
            supportJsonReturningClob = false;
            if (majorVersion >= 18) {
                supportJsonReturningClob = true;
            }
        }
    }

    public Dbms getDbms() {
        return dbms;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public boolean supportJsonReturningClob() {
        return supportJsonReturningClob;
    }

    protected boolean isSet() {
        return isSet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("majorVersion=").append(majorVersion);
        sb.append(",minorVersion=").append(minorVersion);
        sb.append(",productName=").append(productName);
        sb.append(",productVersion=").append(productVersion);
        sb.append(",supportJsonReturningClob=").append(supportJsonReturningClob);
        return sb.toString();
    }

}
