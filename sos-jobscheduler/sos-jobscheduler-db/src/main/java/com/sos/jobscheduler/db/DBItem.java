package com.sos.jobscheduler.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public abstract class DBItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBItem.class);

    private List<String> uniqueConstraintFieldNames = null;

    /* return empty list (not null) when nothing found */
    public List<String> getUniqueConstraintFieldNames() throws SOSHibernateException {
        if (uniqueConstraintFieldNames == null) {
            uniqueConstraintFieldNames = new ArrayList<>();
            Class<?> clazz = this.getClass();
            String clazzName = clazz.getSimpleName();
            Table ta = clazz.getDeclaredAnnotation(Table.class);
            if (ta == null) {
                throw new SOSHibernateException(String.format("[%s]missing @Table annotation", clazzName));
            }
            UniqueConstraint[] ucs = ta.uniqueConstraints();
            if (ucs == null || ucs.length == 0) {
                return uniqueConstraintFieldNames;
            }

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
                        uniqueConstraintFieldNames.add(of.get().getName());
                    } else {
                        throw new SOSHibernateException(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]can't find %s annoted field",
                                clazzName, columnName));
                    }
                }
            }
        }
        return uniqueConstraintFieldNames;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        Class<?> otherClazz = other.getClass();
        if (!(otherClazz.isInstance(this))) {
            return false;
        }

        try {
            EqualsBuilder eb = new EqualsBuilder();
            List<String> fields = getUniqueConstraintFieldNames();
            if (fields.isEmpty()) {
                eb.append(SOSHibernate.getId(this), SOSHibernate.getId(other));
            } else {
                for (String entry : fields) {
                    Field thisClassField = this.getClass().getDeclaredField(entry);
                    Field otherClassField = otherClazz.getDeclaredField(entry);
                    thisClassField.setAccessible(true);
                    otherClassField.setAccessible(true);
                    eb.append(thisClassField.get(this), otherClassField.get(other));
                }
            }
            return eb.isEquals();
        } catch (Throwable ex) {
            LOGGER.error(String.format("[equals][%s]%s", this.getClass().getSimpleName(), ex.toString()), ex);
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        try {
            List<String> fields = getUniqueConstraintFieldNames();
            if (fields.isEmpty()) {
                hcb.append(SOSHibernate.getId(this));
            } else {
                for (String entry : fields) {
                    Field field = this.getClass().getDeclaredField(entry);
                    field.setAccessible(true);
                    hcb.append(field.get(this));
                }
            }
        } catch (Throwable ex) {
            LOGGER.error(String.format("[hashCode][%s]%s", this.getClass().getSimpleName(), ex.toString()), ex);
            hcb.append(0);
        }
        return hcb.toHashCode();
    }
}
