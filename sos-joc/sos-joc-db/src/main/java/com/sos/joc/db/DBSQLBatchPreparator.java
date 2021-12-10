package com.sos.joc.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.common.SOSBatchObject;

public class DBSQLBatchPreparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSQLBatchPreparator.class);

    public static BatchPreparator prepareForSQLBatchInsert(Dialect dialect, List<? extends DBItem> items) {
        if (items == null || items.size() == 0) {
            return null;
        }
        Meta meta = getMeta(items.get(0));
        if (meta == null) {
            return null;
        }
        return (new DBSQLBatchPreparator()).new BatchPreparator(meta.tableName, getInsertSQL(meta, dialect).toString(), getRows(meta, items));
    }

    private static StringBuilder getInsertSQL(Meta meta, Dialect dialect) {
        StringBuilder sql = new StringBuilder("insert into ").append(meta.tableName);
        sql.append("(");
        sql.append(meta.columns.entrySet().stream().map(e -> {
            return SOSHibernate.quoteColumn(dialect, e.getKey());
        }).collect(Collectors.joining(",")));
        sql.append(") values (");
        sql.append(meta.columns.entrySet().stream().map(e -> {
            return "?";
        }).collect(Collectors.joining(",")));
        sql.append(")");
        return sql;
    }

    private static Collection<Collection<SOSBatchObject>> getRows(Meta meta, List<? extends DBItem> items) {
        Collection<Collection<SOSBatchObject>> rows = new ArrayList<>();
        for (DBItem item : items) {
            rows.add(getRow(meta, item));
        }
        return rows;
    }

    private static Collection<SOSBatchObject> getRow(Meta meta, DBItem item) {
        Collection<SOSBatchObject> row = new ArrayList<>();

        List<Field> fields = getFields(item);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String columnName = getColumnName(field);
                row.add(new SOSBatchObject(meta.columns.get(columnName), columnName, field.get(item)));
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
        }
        return row;
    }

    private static Meta getMeta(DBItem item) {
        Table table = item.getClass().getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        Map<String, Integer> map = new LinkedHashMap<>();
        List<Field> fields = getFields(item);
        int i = 1;
        for (Field field : fields) {
            map.put(getColumnName(field), i);
            i++;
        }
        return (new DBSQLBatchPreparator()).new Meta(table.name(), map);
    }

    private static List<Field> getFields(DBItem item) {
        return Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Column.class) && !m.isAnnotationPresent(
                GeneratedValue.class)).collect(Collectors.toList());
    }

    private static String getColumnName(Field field) {
        return field.getAnnotation(Column.class).name().replaceAll("\\[", "").replaceAll("\\]", "");
    }

    public class BatchPreparator {

        private final String tableName;
        private final String sql;
        private final Collection<Collection<SOSBatchObject>> rows;

        private BatchPreparator(String tableName, String sql, Collection<Collection<SOSBatchObject>> rows) {
            this.tableName = tableName;
            this.sql = sql;
            this.rows = rows;
        }

        public String getTableName() {
            return tableName;
        }

        public String getSQL() {
            return sql;
        }

        public Collection<Collection<SOSBatchObject>> getRows() {
            return rows;
        }
    }

    private class Meta {

        private final String tableName;
        private final Map<String, Integer> columns;

        private Meta(String tableName, Map<String, Integer> columns) {
            this.tableName = tableName;
            this.columns = columns;
        }

    }

}
