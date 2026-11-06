package com.sos.commons.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Transient;

/** Utility for removing NUL bytes ({@code \u0000}) from String fields of entities before they are written to PostgreSQL.
 *
 * <p>
 * PostgreSQL text columns ({@code text}, {@code varchar}, {@code char}) cannot store NUL bytes because the server internally uses NUL-terminated C strings.
 * Writing a String containing {@code \u0000} through JDBC fails with: {@code ERROR: invalid byte sequence for encoding "UTF8": 0x00}.<br />
 * The other supported databases (H2, MySQL/MariaDB, MSSQL, Oracle) store text with a length prefix and can hold {@code \u0000}.
 * </p>
 *
 * <p>
 * This sanitizer is intended for use with Hibernate {@code Session} and {@code StatelessSession}.<br />
 * It is the single, deliberate solution for NUL-byte removal in this code base.<br />
 * - No {@code AttributeConverter} is registered, because an {@code AttributeConverter} only takes effect for {@code Session} and is bypassed by
 * {@code StatelessSession}.
 * </p>
 *
 * <p>
 * String fields are discovered once per entity class via reflection (including inherited non-final fields) and cached.<br />
 * The cache holds one entry per entity class and does not grow with the number of rows processed.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * 
 * <pre>{@code
 * SOSHibernateSanitizer.sanitizeStringValues(entity);
 * sosStatelessSession.save(entity);
 * }</pre>
 */
public final class SOSHibernateSanitizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSanitizer.class);

    private SOSHibernateSanitizer() {
    }

    /** Cache of accessible, non-final String fields per entity class.
     *
     * ClassValue is used instead of ConcurrentHashMap to avoid classloader memory leaks when the application is redeployed or its classes are reloaded. */
    private static final ClassValue<Field[]> STRING_FIELD_CACHE = new ClassValue<>() {

        @Override
        protected Field[] computeValue(Class<?> clazz) {

            List<Field> fields = new ArrayList<>();
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (field.getType() != String.class || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(
                            modifiers) || field.isSynthetic() || field.isAnnotationPresent(Transient.class)) {
                        continue;
                    }

                    try {
                        field.setAccessible(true);
                        fields.add(field);
                    } catch (RuntimeException e) {
                        LOGGER.warn("[sanitizeStringValues] Cannot access field: " + field.getDeclaringClass().getName() + "." + field.getName(), e);
                    }
                }
            }

            return fields.toArray(new Field[0]);
        }
    };

    /** Removes NUL characters from all accessible String fields.
     *
     * Hibernate proxies are resolved to their persistent entity class.
     *
     * @param entity entity to sanitize */
    public static void sanitizeStringValues(Object entity) {
        if (entity == null) {
            return;
        }

        Class<?> entityClass = getEntityClass(entity);
        if (entityClass == null) {
            return;
        }

        Field[] fields = STRING_FIELD_CACHE.get(entityClass);
        for (Field field : fields) {
            try {
                String value = (String) field.get(entity);
                if (value == null || value.indexOf('\u0000') < 0) {
                    continue;
                }

                field.set(entity, value.replace("\u0000", ""));
            } catch (IllegalAccessException e) {
                LOGGER.warn("[sanitizeStringValues] Failed for " + entityClass.getName() + "." + field.getName(), e);
            }
        }
    }

    /** Resolves Hibernate proxy classes to their persistent entity class. */
    private static Class<?> getEntityClass(Object entity) {
        if (entity instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }

        return entity.getClass();
    }
}
