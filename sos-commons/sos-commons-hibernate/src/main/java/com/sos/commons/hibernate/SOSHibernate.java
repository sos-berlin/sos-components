package com.sos.commons.hibernate;

import java.lang.reflect.Field;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.cfg.JpaComplianceSettings;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.cfg.ValidationSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import jakarta.persistence.Parameter;

/** Bug - not fixed<br/>
 * - https://hibernate.atlassian.net/browse/HHH-14694<br/>
 * - https://hibernate.atlassian.net/browse/HHH-17946<br/>
 * -- https://hibernate.atlassian.net/browse/HHH-17818<br/>
 * Hibernate releases:<br />
 * - https://hibernate.org/orm/releases/6.5/<br/>
 * -- see "Releases in this series" -> "Resolved issues"<br/>
 * Hibernate constant values:<br/>
 * - https://docs.jboss.org/hibernate/orm/6.5/javadocs/org/hibernate/cfg/<br/>
 * - https://docs.jboss.org/hibernate/orm/6.5/javadocs/constant-values.html<br/>
 * Hibernate documentation:<br/>
 * - https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html<br />
 * SOSHibernate Removed: <br/>
 * - 2024-05-06<br/>
 * -- hibernate.sos.mssql_lock_timeout<br/>
 * ---- SOSHibernateSession.onOpenSession getSQLExecutor().execute("set LOCK_TIMEOUT " + value); only created overhead for each session and had no effect<br/>
 * ---- For MSSQL, the JDBC URL lockTimeout=30000 should be used instead<br/>
 * -- hibernate.jdbc.fetch_size HIBERNATE_PROPERTY_JDBC_FETCH_SIZE<br/>
 * --- not used<br/>
 */
public class SOSHibernate {

    public static final String DEFAULT_DIALECT_PACKAGE = Dialect.class.getPackageName();

    public static final String DEFAULT_DIALECT_MYSQL = MySQLDialect.class.getName();
    public static final String DEFAULT_DIALECT_MARIADB = MariaDBDialect.class.getName();
    public static final String DEFAULT_DIALECT_ORACLE = OracleDialect.class.getName();
    public static final String DEFAULT_DIALECT_PGSQL = PostgreSQLDialect.class.getName();
    public static final String DEFAULT_DIALECT_MSSQL = SQLServerDialect.class.getName();
    public static final String DEFAULT_DIALECT_H2 = H2Dialect.class.getName();

    public enum Dbms {
        H2, MSSQL, MYSQL, ORACLE, PGSQL, UNKNOWN
    }

    /** ---- JdbcSettings ------------------------------------------------------------------------------- */
    /** hibernate.connection.driver_class - The JPA-standard setting {@link #HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS} is now preferred */
    @SuppressWarnings("deprecation")
    protected static final String HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS_DEPRECATED = JdbcSettings.DRIVER;
    /** hibernate.connection.url - The JPA-standard setting {@link #HIBERNATE_PROPERTY_CONNECTION_URL} is now preferred */
    @SuppressWarnings("deprecation")
    protected static final String HIBERNATE_PROPERTY_CONNECTION_URL_DEPRECATED = JdbcSettings.URL;
    /** hibernate.connection.username - The JPA-standard setting {@link #HIBERNATE_PROPERTY_CONNECTION_USERNAME} is now preferred */
    @SuppressWarnings("deprecation")
    protected static final String HIBERNATE_PROPERTY_CONNECTION_USERNAME_DEPRECATED = JdbcSettings.USER;
    /** hibernate.connection.password - The JPA-standard setting {@link #HIBERNATE_PROPERTY_CONNECTION_PASSWORD} is now preferred */
    @SuppressWarnings("deprecation")
    protected static final String HIBERNATE_PROPERTY_CONNECTION_PASSWORD_DEPRECATED = JdbcSettings.PASS;

    /** jakarta.persistence.jdbc.driver */
    public static final String HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS = JdbcSettings.JAKARTA_JDBC_DRIVER;
    /** jakarta.persistence.jdbc.url */
    public static final String HIBERNATE_PROPERTY_CONNECTION_URL = JdbcSettings.JAKARTA_JDBC_URL;
    /** jakarta.persistence.jdbc.user */
    public static final String HIBERNATE_PROPERTY_CONNECTION_USERNAME = JdbcSettings.JAKARTA_JDBC_USER;
    /** jakarta.persistence.jdbc.password */
    public static final String HIBERNATE_PROPERTY_CONNECTION_PASSWORD = JdbcSettings.JAKARTA_JDBC_PASSWORD;

    /** hibernate.dialect - deprecated<br/>
     * - HHH90000025: ...Dialect does not need to be specified explicitly using 'hibernate.dialect' */
    public static final String HIBERNATE_PROPERTY_DIALECT = JdbcSettings.DIALECT;
    /** hibernate.boot.allow_jdbc_metadata_access - SOS default: true <br/>
     * true - automatically detects Dialect if hibernate.dialect is not configured<br/>
     * --- sets org.hibernate.dialect.MariaDBDialect instead of MySQLDialect when using a MariaDB driver is used */
    public static final String HIBERNATE_PROPERTY_ALLOW_METADATA_ON_BOOT = JdbcSettings.ALLOW_METADATA_ON_BOOT;
    /** hibernate.connection.autocommit - SOS default: false */
    public static final String HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT = JdbcSettings.AUTOCOMMIT;
    /** hibernate.connection.isolation - SOS default: Connection.TRANSACTION_READ_COMMITTED */
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = JdbcSettings.ISOLATION;
    /** hibernate.jdbc.use_scrollable_resultset SOS default: true */
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = JdbcSettings.USE_SCROLLABLE_RESULTSET;

    /** ---- AvailableSettings ------------------------------------------------------------------------------- */
    /** hibernate.current_session_context_class - SOS default: jta */
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS;
    /** hibernate.dialect_resolvers - SOS default: SOSHibernateDefaultDialectResolver.<br/>
     * see SOSHibernateFactory, SOSHibernateFinalPropertiesResolver */
    public static final String HIBERNATE_PROPERTY_DIALECT_RESOLVERS = AvailableSettings.DIALECT_RESOLVERS;

    /** ---- MappingSettings ------------------------------------------------------------------------------- */
    /** hibernate.id.db_structure_naming_strategy - SOS default: legacy */
    public static final String HIBERNATE_PROPERTY_ID_STRUCTURE_NAMING_STRATEGY = MappingSettings.ID_DB_STRUCTURE_NAMING_STRATEGY;

    /** ---- ValidationSettings ------------------------------------------------------------------------------- */
    /** jakarta.persistence.validation.mode - SOS default: none */
    public static final String HIBERNATE_PROPERTY_PERSISTENCE_VALIDATION_MODE = ValidationSettings.JAKARTA_VALIDATION_MODE;

    /** ---- JpaComplianceSettings ------------------------------------------------------------------------------- */
    /** hibernate.jpa.compliance.global_id_generators - SOS default: false */
    public static final String HIBERNATE_PROPERTY_JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE =
            JpaComplianceSettings.JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE;

    /** ---- SOS Settings ------------------------------------------------------------------------------- */
    public static final String HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES = "hibernate.sos.show_configuration_properties";
    // SOS Settings: credential store
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE = "hibernate.sos.credential_store_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE = "hibernate.sos.credential_store_key_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD = "hibernate.sos.credential_store_password";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH = "hibernate.sos.credential_store_entry_path";
    // SOS Settings: encryption
    public static final String HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEY = "hibernate.sos.decryption_key";
    public static final String HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEYPWD = "hibernate.sos.decryption_keypassword";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE = "hibernate.sos.keystore_path";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_TYPE = "hibernate.sos.keystore_type";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_PWD = "hibernate.sos.keystore_password";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYPWD = "hibernate.sos.keystore_keypassword";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYALIAS = "hibernate.sos.keystore_keyalias";
    // SOS Settings: dbms product
    public static final String HIBERNATE_SOS_PROPERTY_DBMS_PRODUCT = "hibernate.sos.dbms_product";

    /** SOSHibernate variables */
    public static final Map<String, Dbms> JDBC_TO_DBMS = new HashMap<>();
    public static final Map<String, Dbms> DIALECT_PART_TO_DBMS = new HashMap<>();
    public static final Map<String, Dbms> SOS_DBMS_PRODUCT_TO_DBMS = new HashMap<>();

    public static final int LIMIT_IN_CLAUSE = 1000;

    /** is set on the Hibernate SessionFactory-Object */
    public static final String SESSION_FACTORY_VAR_DBMS = "sos.session_factory_var.dbms";
    public static final String SESSION_FACTORY_VAR_DATABASE_METADATA = "sos.session_factory_var.database_metadata";

    // the later usage/check is not case-sensitive
    static {
        for (Dbms dbms : Dbms.values()) {
            switch (dbms) {
            case H2:
                JDBC_TO_DBMS.put("jdbc:h2", dbms);
                DIALECT_PART_TO_DBMS.put("H2", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("H2", dbms);
                break;

            case MSSQL:
                JDBC_TO_DBMS.put("jdbc:sqlserver", dbms);
                DIALECT_PART_TO_DBMS.put("SQLServer", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("MSSQL", dbms);
                break;

            case MYSQL:
                // MySQL
                JDBC_TO_DBMS.put("jdbc:mysql", dbms);
                DIALECT_PART_TO_DBMS.put("MySQL", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("MySQL", dbms);

                // MariaDB
                JDBC_TO_DBMS.put("jdbc:mariadb", dbms);
                DIALECT_PART_TO_DBMS.put("MariaDB", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("MariaDB", dbms);
                break;

            case ORACLE:
                JDBC_TO_DBMS.put("jdbc:oracle", dbms);
                DIALECT_PART_TO_DBMS.put("Oracle", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("Oracle", dbms);
                break;

            case PGSQL:
                JDBC_TO_DBMS.put("jdbc:postgresql", dbms);
                DIALECT_PART_TO_DBMS.put("Postgre", dbms);
                SOS_DBMS_PRODUCT_TO_DBMS.put("PgSQL", dbms);
                break;

            case UNKNOWN:
            default:
                break;
            }
        }
    }

    public static Exception findLockException(Exception cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSHibernateLockAcquisitionException) {
                return (SOSHibernateLockAcquisitionException) e;
            } else if (e instanceof LockAcquisitionException) {
                return (LockAcquisitionException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    public static Exception findConstraintViolationException(Exception cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof ConstraintViolationException) {
                return (ConstraintViolationException) e;
            } else if (e instanceof SQLIntegrityConstraintViolationException) {
                return (SQLIntegrityConstraintViolationException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    public static boolean isConnectException(Throwable cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSHibernateFactoryBuildException) {
                return true;
            } else if (e instanceof SOSHibernateInvalidSessionException) {
                return true;
            } else if (e instanceof SOSHibernateOpenSessionException) {
                return true;
            } else if (e instanceof org.hibernate.exception.JDBCConnectionException) {
                return true;
            } else if (e instanceof java.net.ConnectException || e instanceof java.net.SocketException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public static Object getId(Object item) throws SOSHibernateException {
        if (item == null) {
            return null;
        }

        List<Field> l = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).collect(Collectors.toList());
        int size = l.size();
        if (size < 1) {
            l = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(EmbeddedId.class)).collect(Collectors.toList());
            size = l.size();
        }
        if (size < 1) {
            return null;
        }

        Object[] id = new Object[size];
        for (int i = 0; i < size; i++) {
            Field field = l.get(i);
            try {
                field.setAccessible(true);
                id[i] = field.get(item);
            } catch (Throwable e) {
                throw new SOSHibernateException(String.format("[getId][can't get field]%s", item.getClass().getSimpleName(), e.toString()), e);
            }
        }
        if (size == 1) {
            return id[0];
        }
        return id;
    }

    public static void setId(Object item, Object value) throws SOSHibernateException {
        if (item == null) {
            return;
        }
        List<Field> l = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).collect(Collectors.toList());
        int size = l.size();
        if (size < 1) {
            l = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(EmbeddedId.class)).collect(Collectors.toList());
            size = l.size();
        }
        if (size < 1) {
            return;
        }

        Object[] id = new Object[size];
        if (size == 1) {
            id[0] = value;
        } else {
            Object[] o = null;
            if (SOSReflection.isArray(value.getClass())) {
                o = (Object[]) value;
            } else {
                // TODO List etc.
                for (int i = 0; i < size; i++) {
                    id[i] = value;
                }
            }
            for (int i = 0; i < size; i++) {
                id[i] = o[i];
            }
        }
        for (int i = 0; i < size; i++) {
            Field field = l.get(i);
            try {
                field.setAccessible(true);
                field.set(item, id[i]);
            } catch (Exception ex) {
                throw new SOSHibernateException(String.format("[setId][can't set field]%s", item.getClass().getSimpleName(), ex.toString()), ex);
            }
        }
    }

    public static String getQueryParametersAsString(Query<?> query) {
        if (query == null) {
            return null;
        }
        try {
            Set<Parameter<?>> set = query.getParameters();
            if (set != null && set.size() > 0) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Parameter<?> parameter : set) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    Object val = query.getParameterValue(parameter.getName());
                    if (val != null && val instanceof Date) {
                        val = SOSDate.getDateTimeAsString((Date) val);
                    }
                    sb.append(parameter.getName() + "=" + val);
                    i++;
                }
                return sb.toString();
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        // exclude object BLOB (byte[]) fields
        List<String> excludeFieldNames = Arrays.stream(o.getClass().getDeclaredFields()).filter(m -> m.getType().isAssignableFrom(byte[].class)).map(
                Field::getName).collect(Collectors.toList());

        // exclude superclass (DBItem) fields
        List<String> excludeDBItemFieldNames = Arrays.stream(o.getClass().getSuperclass().getDeclaredFields()).map(Field::getName).collect(Collectors
                .toList());
        excludeFieldNames.addAll(excludeDBItemFieldNames);

        return SOSString.toString(o, excludeFieldNames);
    }

    public static <T> List<T> getInClausePartition(int part, List<T> list) {
        return list.subList(part, Math.min(part + LIMIT_IN_CLAUSE, list.size()));
    }

    public static <T> List<List<T>> getInClausePartitions(List<T> list) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += LIMIT_IN_CLAUSE) {
            partitions.add(getInClausePartition(i, list));
        }
        return partitions;
    }

    public static String quoteColumn(Dialect dialect, String columnName) {
        if (dialect != null && columnName != null) {
            String[] arr = columnName.split("\\.");
            if (arr.length == 1) {
                columnName = dialect.openQuote() + columnName + dialect.closeQuote();
            } else {
                StringBuilder sb = new StringBuilder();
                String cn = arr[arr.length - 1];
                for (int i = 0; i < arr.length - 1; i++) {
                    sb.append(arr[i] + ".");
                }
                sb.append(dialect.openQuote() + cn + dialect.closeQuote());
                columnName = sb.toString();
            }
        }
        return columnName;
    }

    protected static String getLogIdentifier(String identifier) {
        if (identifier == null) {
            return "";
        }
        if (identifier.startsWith("[")) {
            return identifier;
        }
        return String.format("[%s]", identifier);
    }

    protected static String getMethodName(String logIdentifier, String name) {
        return String.format("%s[%s]", logIdentifier, name);
    }
}
