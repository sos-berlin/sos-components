package com.sos.jobscheduler.history.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class DBItemTest {

    private List<Field> uniqueConstraintFields = new ArrayList<>();

    public List<Field> getUniqueConstraintFields() {
        return uniqueConstraintFields;
    }

    public DBItemTest() {
        try {
            Class<?> clazz = this.getClass();
            Table ta = clazz.getDeclaredAnnotation(Table.class);
            if (ta == null) {
                throw new Exception(String.format("[%s]missing @Table annotation", clazz.getSimpleName()));
            }
            UniqueConstraint[] ucs = ta.uniqueConstraints();
            if (ucs == null || ucs.length == 0) {
                throw new Exception(String.format("[%s][@Table]uniqueConstraints annotation is null or empty", clazz.getSimpleName()));
            }

            for (int i = 0; i < ucs.length; i++) {
                UniqueConstraint uc = ucs[i];
                String[] columnNames = uc.columnNames();
                if (columnNames == null || columnNames.length == 0) {
                    throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]columnNames annotation is null or empty",
                            clazz.getSimpleName()));
                }
                for (int j = 0; j < columnNames.length; j++) {
                    String columnName = columnNames[j];
                    Optional<Field> of = Arrays.stream(clazz.getDeclaredFields()).filter(m -> m.isAnnotationPresent(Column.class) && m.getAnnotation(
                            Column.class).name().equals(columnName)).findFirst();
                    if (of.isPresent()) {
                        uniqueConstraintFields.add(of.get());
                    } else {
                        throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]can't find %s annoted field", clazz
                                .getSimpleName(), columnName));
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println(ex.toString());
        }

    }

    public boolean equals(DBItemTest o) {
        /** if (o == null || !(o instanceof this.getClass().getClass())) { return false; } DBItemAgentTest item = (DBItemAgentTest) o; if
         * (!getId().equals(item.getId())) { return false; } */
        if (o == null) {
            return false;
        }

        for (Field field : uniqueConstraintFields) {
            field.setAccessible(true);
            try {
                System.out.println(field.get(this));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println(uniqueConstraintFields);
        return true;
    }

    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        for (Field field : uniqueConstraintFields) {
            field.setAccessible(true);
            try {
                hcb.append(field.get(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hcb.toHashCode();
    }
}
