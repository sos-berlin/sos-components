package com.sos.commons.hibernate;

import java.sql.DatabaseMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;

public class SOSHibernateDatabaseMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateDatabaseMetaData.class);

    private static final int MAX_SET_RETRY = 5;
    private static final int ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_CLOB = 18;

    // TODO declare the Dbms enum here instead of SOSHibernateFactory.Dbms
    /** not null - evaluated from the hibernate configuration */
    private final Dbms dbms;

    /** can be null/0
     * 
     * dependent of SOSHibernateFactory.build(readDatabaseMetaData) */
    private String productName;
    private String productVersion;
    private int majorVersion;
    private int minorVersion;
    private boolean supportJsonReturningClob;

    private boolean isSet;
    private int setRetryCounter;

    protected SOSHibernateDatabaseMetaData(Dbms dbms) {
        this.dbms = dbms == null ? Dbms.UNKNOWN : dbms;
        this.setRetryCounter = 0;
        setSupportJsonReturningClob();
    }

    protected void set(DatabaseMetaData metaData) {
        if (isSet) {
            return;
        }
        if (metaData == null) {
            setRetryCounter++;
            if (setRetryCounter >= MAX_SET_RETRY) {
                isSet = true;
            }
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
            if (majorVersion >= ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_CLOB) {
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
