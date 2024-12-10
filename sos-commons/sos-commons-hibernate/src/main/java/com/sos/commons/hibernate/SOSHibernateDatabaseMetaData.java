package com.sos.commons.hibernate;

import java.sql.DatabaseMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;

public class SOSHibernateDatabaseMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateDatabaseMetaData.class);

    /** Used with JSON_QUERY to retrieve JSON result longer than 4000 characters<br/>
     * - If such case is not handled separately, Oracle will return null in this case,<br/>
     * -- because JSON_QUERY uses the return type VARCHAR2(4000) by default<br/>
     * TODO introduction of ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_NCLOB<br/>
     * - 18c - ORA-40449 invalid data type for return value<br/>
     * - 21c - accepts RETURNING NCLOB - from which version is RETURNING NCLOB supported?<br/>
     */
    private static final int ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_CLOB = 18;

    private final Dbms dbms;

    /** can be null/0
     * 
     * dependent of SOSHibernateFactory.build(readDatabaseMetaData) */
    private String productName;
    private String productVersion;
    private int majorVersion;
    private int minorVersion;
    private boolean supportJsonReturningClob;
    private boolean metaDataAvailable;

    protected SOSHibernateDatabaseMetaData(Dbms dbms) {
        this(dbms, null);
    }

    public SOSHibernateDatabaseMetaData(Dbms dbms, DatabaseMetaData metaData) {
        this.dbms = dbms;
        setMetaData(metaData);
        setSupportJsonReturningClob();
    }

    private void setMetaData(DatabaseMetaData metaData) {
        metaDataAvailable = false;
        if (metaData == null) {
            return;
        }
        try {
            productName = metaData.getDatabaseProductName();
            productVersion = metaData.getDatabaseProductVersion();
            majorVersion = metaData.getDatabaseMajorVersion();
            minorVersion = metaData.getDatabaseMinorVersion();

            metaDataAvailable = true;
        } catch (Throwable e) {
            LOGGER.warn(String.format("[setMetaData][%s]%s", dbms, e.toString()), e);
        }
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

    public boolean metaDataAvailable() {
        return metaDataAvailable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[metaDataAvailable=").append(metaDataAvailable).append("]");
        sb.append("majorVersion=").append(majorVersion);
        sb.append(",minorVersion=").append(minorVersion);
        sb.append(",productName=").append(productName);
        sb.append(",productVersion=").append(productVersion);
        if (Dbms.ORACLE.equals(dbms)) {
            sb.append(",supportJsonReturningClob=").append(supportJsonReturningClob);
        }
        return sb.toString();
    }

}
