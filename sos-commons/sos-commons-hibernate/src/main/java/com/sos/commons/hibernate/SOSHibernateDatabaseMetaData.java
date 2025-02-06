package com.sos.commons.hibernate;

import java.sql.DatabaseMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;

public class SOSHibernateDatabaseMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateDatabaseMetaData.class);

    private final Dbms dbms;

    /** can be null/0
     * 
     * dependent of SOSHibernateFactory.build(readDatabaseMetaData) */
    private String productName;
    private String productVersion;
    private int majorVersion;
    private int minorVersion;
    private boolean metaDataAvailable;
    private Oracle oracle = null;

    protected SOSHibernateDatabaseMetaData(Dbms dbms) {
        this(dbms, null);
    }

    public SOSHibernateDatabaseMetaData(Dbms dbms, DatabaseMetaData metaData) {
        this.dbms = dbms;
        setMetaData(metaData);
        setJsonSupport();
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

    private void setJsonSupport() {
        if (dbms.equals(Dbms.ORACLE)) {
            oracle = new Oracle();
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
        if (oracle != null) {
            sb.append(",[json]");
            sb.append("returningClobEnabled=").append(oracle.json.returningClobEnabled);
            sb.append(",fallbackToRegex=").append(oracle.json.fallbackToRegex);
        }
        return sb.toString();
    }

    public Oracle getOracle() {
        return oracle;
    }

    public class Oracle {

        private Json json = null;

        private Oracle() {
            json = new Json(majorVersion, minorVersion);
        }

        public Json getJson() {
            return json;
        }

        public class Json {

            /** Used with JSON_QUERY to retrieve JSON result longer than 4000 characters<br/>
             * - If such case is not handled separately, Oracle will return null in this case,<br/>
             * -- because JSON_QUERY uses the return type VARCHAR2(4000) by default<br/>
             * TODO introduction of ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_NCLOB<br/>
             * - 18c - ORA-40449 invalid data type for return value<br/>
             * - 21c - accepts RETURNING NCLOB - from which version is RETURNING NCLOB supported?<br/>
             */
            private static final int ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_CLOB = 18;

            private boolean returningClobEnabled;
            private boolean fallbackToRegex;

            private Json(int majorVersion, int minorVersion) {
                if (majorVersion >= 12) {
                    if (majorVersion == 12) {
                        // e.g. in 12.1.0.20, JSON_EXISTS has limited functionality and does not include the advanced filtering features introduced in 12.2.x
                        if (minorVersion <= 1) {
                            fallbackToRegex = true;
                        }
                    } else {
                        if (majorVersion >= ORACLE_MIN_VERSION_SUPPORT_JSON_RETURNING_CLOB) {
                            returningClobEnabled = true;
                        }
                    }
                }
            }

            public boolean returningClobEnabled() {
                return returningClobEnabled;
            }

            public boolean fallbackToRegex() {
                return fallbackToRegex;
            }

        }
    }

}
