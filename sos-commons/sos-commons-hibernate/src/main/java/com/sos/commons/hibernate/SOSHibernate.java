package com.sos.commons.hibernate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;
import javax.persistence.Parameter;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.commons.util.SOSString;

public class SOSHibernate {

    public static final String HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_CONNECTION_PASSWORD = "hibernate.connection.password";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
    public static final String HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE = "javax.persistence.validation.mode";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    // SOS configuration properties
    public static final String HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT = "hibernate.sos.mssql_lock_timeout";
    public static final String HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES = "hibernate.sos.show_configuration_properties";
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

    public static Object getId(Object item) throws SOSHibernateException {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            if (of.isPresent()) {
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
            if (of.isPresent()) {
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
                    sb.append(parameter.getName() + "=" + query.getParameterValue(parameter.getName()));
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
