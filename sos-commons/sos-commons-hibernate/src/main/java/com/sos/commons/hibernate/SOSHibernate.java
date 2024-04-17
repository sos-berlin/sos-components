package com.sos.commons.hibernate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Parameter;

import org.hibernate.dialect.Dialect;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;

public class SOSHibernate {

    public static final String HIBERNATE_PROPERTY_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_CONNECTION_URL = "hibernate.connection.url";
    public static final String HIBERNATE_PROPERTY_CONNECTION_USERNAME = "hibernate.connection.username";
    public static final String HIBERNATE_PROPERTY_CONNECTION_PASSWORD = "hibernate.connection.password";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
    public static final String HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE = "javax.persistence.validation.mode";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    
    //JS7 Environment variables for substitution
    public static final String JS7_DBMS_URL_PARAMETER = "JS7_DBMS_URL_PARAMETER";
    public static final String JS7_DBMS_USER = "JS7_DBMS_USER";
    public static final String JS7_DBMS_PASSWORD = "JS7_DBMS_PASSWORD";

    // SOS configuration properties
    public static final String HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT = "hibernate.sos.mssql_lock_timeout";
    public static final String HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES = "hibernate.sos.show_configuration_properties";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE = "hibernate.sos.credential_store_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE = "hibernate.sos.credential_store_key_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD = "hibernate.sos.credential_store_password";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH = "hibernate.sos.credential_store_entry_path";
    
    // SOS configuration properties for en-/decryption
    public static final String HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEY = "hibernate.sos.decryption_key";
    public static final String HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEYPWD = "hibernate.sos.decryption_keypassword";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE = "hibernate.sos.keystore_path";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_TYPE = "hibernate.sos.keystore_type";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_PWD = "hibernate.sos.keystore_password";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYPWD = "hibernate.sos.keystore_keypassword";
    public static final String HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYALIAS = "hibernate.sos.keystore_keyalias";
    // Identifiers
    public static final String ENCRYPTION_IDENTIFIER = "enc://";

    public static final int LIMIT_IN_CLAUSE = 1000;

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
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            boolean present = of.isPresent();
            if (!present) {
                of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(EmbeddedId.class)).findFirst();
                present = of.isPresent();
            }
            if (present) {
                Field field = of.get();
                field.setAccessible(true);
                try {
                    return field.get(item);
                } catch (Exception ex) {
                    throw new SOSHibernateException(String.format("[getId][can't get field]%s", item.getClass().getSimpleName(), ex.toString()), ex);
                }
            }
        }
        return null;
    }

    public static void setId(Object item, Object value) throws SOSHibernateException {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            boolean present = of.isPresent();
            if (!present) {
                of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(EmbeddedId.class)).findFirst();
                present = of.isPresent();
            }
            if (present) {
                Field field = of.get();
                field.setAccessible(true);
                try {
                    field.set(item, value);
                } catch (Exception ex) {
                    throw new SOSHibernateException(String.format("[setId][can't set field]%s", item.getClass().getSimpleName(), ex.toString()), ex);
                }
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
