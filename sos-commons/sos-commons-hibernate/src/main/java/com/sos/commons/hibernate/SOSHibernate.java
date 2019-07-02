package com.sos.commons.hibernate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;

public class SOSHibernate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFactory.class);

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

    public static int hashCode(Object item) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        try {
            Map<String, Object> fields = getUniqueConstraintFields(item);
            if (fields == null) {
                hcb.append(getId(item));
            } else {
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    hcb.append(entry.getValue());
                }
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("[hashCode][%s]%s", item.getClass().getSimpleName(), ex.toString()), ex);
            hcb.append(0);
        }
        return hcb.toHashCode();
    }

    public static boolean equals(Object item, Object other) {
        if (other == item) {
            return true;
        }

        Class<?> otherClazz = other.getClass();
        if (!(otherClazz.isInstance(item))) {
            return false;
        }

        try {
            EqualsBuilder eb = new EqualsBuilder();
            Map<String, Object> fields = getUniqueConstraintFields(item);
            if (fields == null) {
                eb.append(getId(item), getId(other));
            } else {
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    Field otherClassField = otherClazz.getDeclaredField((String) entry.getValue());
                    otherClassField.setAccessible(true);
                    eb.append(entry.getValue(), otherClassField.get(other));
                }
            }
            return eb.isEquals();
        } catch (Exception ex) {
            LOGGER.error(String.format("[equals][%s]%s", item.getClass().getSimpleName(), ex.toString()), ex);
            return false;
        }
    }

    public static Map<String, Object> getUniqueConstraintFields(Object o) throws SOSHibernateException {
        Class<?> clazz = o.getClass();
        String clazzName = clazz.getSimpleName();
        Table ta = clazz.getDeclaredAnnotation(Table.class);
        if (ta == null) {
            throw new SOSHibernateException(String.format("[%s]missing @Table annotation", clazzName));
        }
        UniqueConstraint[] ucs = ta.uniqueConstraints();
        if (ucs == null || ucs.length == 0) {
            return null;
        }

        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 0; i < ucs.length; i++) {
            UniqueConstraint uc = ucs[i];
            String[] columnNames = uc.columnNames();
            if (columnNames == null || columnNames.length == 0) {
                throw new SOSHibernateException(String.format(
                        "[%s][@Table][uniqueConstraints @UniqueConstraint]columnNames annotation is null or empty", clazzName));
            }
            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                Optional<Field> of = Arrays.stream(clazz.getDeclaredFields()).filter(m -> m.isAnnotationPresent(Column.class) && m.getAnnotation(
                        Column.class).name().equals(columnName)).findFirst();
                if (of.isPresent()) {
                    Field field = of.get();
                    field.setAccessible(true);
                    try {
                        fields.put(field.getName(), field.get(o));
                    } catch (Throwable e) {
                        throw new SOSHibernateException(String.format("[%s][%s][can't get field]%s", clazzName, columnName, e.toString()), e);
                    }
                } else {
                    throw new SOSHibernateException(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]can't find %s annoted field",
                            clazzName, columnName));
                }
            }
        }
        return fields.size() == 0 ? null : fields;
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
