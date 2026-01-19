package com.sos.commons.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;

/** TODO: This class is incomplete and should not be used yet. <br/>
 * It is under development and its API may change at any time. */
public class SOSHibernateSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSynchronizer.class);

    private static final boolean SYNCHRONIZER_AUTO_COMMIT = false;

    private BatchPreparator preparator;
    private boolean doDelete = false;// tmp

    private boolean useInsertBatch = false;
    private int insertBatchSize = 100;
    private Map<Integer, Object> parameters = new HashMap<>();

    public void setParameters(Map<Integer, Object> parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public void process(SOSHibernateSession sourceSession, String sourceTable, String sourceSelectQuery, String primaryKeyColumn,
            SOSHibernateSession targetSession, String targetTable) throws Exception {

        preparator = new BatchPreparator(sourceSession.getFactory().getDialect(), sourceTable, primaryKeyColumn, targetSession.getFactory().getDbms(),
                targetSession.getFactory().getDialect());

        Connection sourceConnection = sourceSession.getConnection();
        Connection targetConnection = targetSession.getConnection();
        boolean sourceAutoCommit = setAutoCommit(sourceConnection);
        boolean targetAutoCommit = setAutoCommit(targetConnection);
        try {
            // 1) select Max Target ID
            Number maxTargetId = 0;// getMaxId(targetConnection, targetTable, primaryKeyColumn);

            boolean run = true;
            while (run) {
                int r = process(sourceConnection, sourceTable, sourceSelectQuery, primaryKeyColumn, targetConnection, targetTable, maxTargetId);
                if (r < 1) {
                    run = false;
                }
            }

        } finally {
            restoreAutoCommit("target", targetConnection, targetAutoCommit);
            restoreAutoCommit("source", sourceConnection, sourceAutoCommit);
        }
    }

    private int process(Connection sourceConnection, String sourceTable, String sourceSelectQuery, String primaryKeyColumn,
            Connection targetConnection, String targetTable, Number maxTargetId) throws Exception {

        // 2) Read from the Source Table
        List<Object> primaryKeys = new ArrayList<>();
        int countInserted = 0;
        int size = 0;
        try (PreparedStatement select = sourceConnection.prepareStatement(sourceSelectQuery)) {
            if (parameters != null) {
                for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
                    select.setObject(entry.getKey().intValue(), entry.getValue());
                }
            }

            try (ResultSet rs = select.executeQuery()) {
                // Set parameters if needed
                if (preparator.insertQuery == null) {
                    preparator.init(rs, targetTable);
                }

                // 3) INSERT into Target Table
                try (PreparedStatement insert = targetConnection.prepareStatement(preparator.insertQuery)) {
                    int currentBatchSize = 0;
                    while (rs.next()) {
                        Object primaryKey = rs.getObject(primaryKeyColumn);
                        primaryKeys.add(primaryKey);

                        if (maxTargetId.longValue() < ((Number) primaryKey).longValue()) {
                            for (int i = 1; i <= preparator.columns.size(); i++) {
                                insert.setObject(i, rs.getObject(i), rs.getMetaData().getColumnType(i));
                            }
                            if (useInsertBatch) {
                                insert.addBatch();
                                currentBatchSize++;
                            } else {
                                if (insertSingleRow(targetConnection, insert)) {
                                    countInserted++;
                                }
                            }
                        }
                        if (currentBatchSize % insertBatchSize == 0) {
                            if (insertBatch(targetConnection, insert)) {
                                countInserted += currentBatchSize;
                            }
                            currentBatchSize = 0;
                        }
                    }
                    if (currentBatchSize > 0) {
                        if (insertBatch(targetConnection, insert)) {
                            countInserted += currentBatchSize;
                        }
                    }
                }
                if (!useInsertBatch) {
                    targetConnection.commit();
                }
                size = primaryKeys.size();

                LOGGER.info("[total=" + size + "][target][inserted=" + countInserted + "][not inserted=" + (size - countInserted) + "]");

                if (!doDelete) {
                    return 0;
                }

                if (size > 0) {
                    // 4) DELETE from the Source Table
                    try (PreparedStatement delete = sourceConnection.prepareStatement(preparator.deleteQuery)) {
                        int currentBatchSize = 0;
                        for (int i = 0; i < primaryKeys.size(); i++) {
                            delete.setObject(1, primaryKeys.get(i));
                            delete.addBatch();
                            currentBatchSize++;

                            if (currentBatchSize % insertBatchSize == 0) {
                                delete.executeBatch();
                                delete.clearBatch();
                                sourceConnection.commit();

                                currentBatchSize = 0;
                            }
                        }
                        if (currentBatchSize > 0) {
                            delete.executeBatch();
                            delete.clearBatch();
                            sourceConnection.commit();
                        }
                    } catch (Exception e) {
                        throw new SQLException("[source]" + e.toString(), e);
                    }
                }
            }
            return size;
        } catch (SQLException ex) {
            rollback("target", targetConnection);
            rollback("source", sourceConnection);
            throw ex;
        }
    }

    private boolean insertBatch(Connection targetConnection, PreparedStatement insert) throws Exception {
        try {
            insert.executeBatch();
            insert.clearBatch();
            targetConnection.commit();
            return true;
        } catch (Exception e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw new SQLException("[target]" + e.toString(), e);
            }
            rollback("target", targetConnection);
            LOGGER.info("[target][insert]" + e.toString());
            return false;
        }
    }

    private boolean insertSingleRow(Connection targetConnection, PreparedStatement insert) throws Exception {
        try {
            insert.executeUpdate();
            return true;
        } catch (Exception e) {
            Exception cve = SOSHibernate.findConstraintViolationException(e);
            if (cve == null) {
                throw new SQLException("[target]" + e.toString(), e);
            }
            rollback("target", targetConnection);
            LOGGER.info("[target][insert]" + e.toString());
            return false;
        }
    }

    private boolean setAutoCommit(Connection connection) throws SQLException {
        boolean orig = connection.getAutoCommit();
        if (orig != SYNCHRONIZER_AUTO_COMMIT) {
            connection.setAutoCommit(SYNCHRONIZER_AUTO_COMMIT);
        }
        return orig;
    }

    private void rollback(String source, Connection connection) {
        try {
            connection.rollback();
        } catch (Exception e) {
            LOGGER.error("[" + source + "]" + e.toString(), e);
        }
    }

    private void restoreAutoCommit(String source, Connection connection, boolean orig) {
        if (orig != SYNCHRONIZER_AUTO_COMMIT) {
            try {
                connection.setAutoCommit(orig);
            } catch (Exception e) {
                LOGGER.error("[" + source + "]" + e.toString(), e);
            }
        }
    }

    @SuppressWarnings("unused")
    private Number getMaxId(Connection connection, String table, String primaryKeyColumn) throws SQLException {
        Number id = null;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(" + primaryKeyColumn + ") AS max_id FROM "
                + table)) {
            if (rs.next()) {
                id = (Number) rs.getObject("max_id");
            }
        }
        return id == null ? Long.valueOf(0) : id;
    }

    private class BatchPreparator {

        private final Dialect deleteDialect;
        private final String deleteTableName;
        private final String primaryKeyColumn;
        private final Dbms insertDbms;
        private final Dialect insertDialect;

        private Map<String, Integer> columns;
        private String insertQuery;
        private String deleteQuery;

        private BatchPreparator(Dialect deleteDialect, String deleteTableName, String primaryKeyColumn, Dbms insertDbms, Dialect insertDialect) {
            this.deleteDialect = deleteDialect;
            this.deleteTableName = deleteTableName;
            this.primaryKeyColumn = primaryKeyColumn;
            this.insertDbms = insertDbms;
            this.insertDialect = insertDialect;
        }

        private void init(ResultSet rs, String insertTableName) throws SQLException {
            setColumns(rs);
            try {
                setInsertQuery(insertTableName);
                setDeleteQuery();
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }

        private void setColumns(ResultSet rs) throws SQLException {
            if (columns == null) {
                columns = new LinkedHashMap<>();
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    columns.put(meta.getColumnName(i).toLowerCase(), meta.getColumnType(i));
                }
            }
        }

        private void setInsertQuery(String tableName) throws Exception {
            if (insertQuery == null) {
                insertQuery = createInsertQuery(tableName);
            }
        }

        private String createInsertQuery(String tableName) {
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName);
            sql.append("(");
            sql.append(columns.entrySet().stream().map(e -> {
                return SOSHibernate.quoteColumn(insertDialect, e.getKey());
            }).collect(Collectors.joining(",")));
            sql.append(") VALUES (");
            sql.append(columns.entrySet().stream().map(e -> {
                return "?";
            }).collect(Collectors.joining(",")));
            sql.append(")");
            return sql.toString();
        }

        @SuppressWarnings("unused")
        private String createInsertIgnoreDuplicateQuery(String tableName) {
            String prefix = "";
            String suffix = "";
            switch (insertDbms) {
            case MYSQL:
                // Variant 1)
                // INSERT IGNORE INTO " + targetTable + " (" + insertColumns + ") VALUES (" + insertValues + ")";
                // Variant 2)
                // INSERT INTO " + targetTable + " (" + insertColumns + ") VALUES (" + insertValues + ") ON DUPLICATE KEY UPDATE ID = ID";
                // - in this case INSERT IGNORE should be effective as - more overhead by ON DUPLICATE KEY UPDATE ID ...
                prefix = "IGNORE ";
                break;
            case MSSQL: // ab 2016
            case PGSQL:
                suffix = " ON CONFLICT (" + SOSHibernate.quoteColumn(insertDialect, primaryKeyColumn) + ") DO NOTHING";
                break;
            case ORACLE:
                // ab 12c . kein standard, nicht überall verfügbar
                suffix = " ON DUPLICATE KEY IGNORE";

                suffix = " WHERE NOT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + SOSHibernate.quoteColumn(insertDialect, primaryKeyColumn)
                        + " = ?)";
                break;
            default:
                break;
            }

            StringBuilder sql = new StringBuilder("INSERT ").append(prefix).append("INTO ").append(tableName);
            sql.append("(");
            sql.append(columns.entrySet().stream().map(e -> {
                return SOSHibernate.quoteColumn(insertDialect, e.getKey());
            }).collect(Collectors.joining(",")));
            sql.append(") VALUES (");
            sql.append(columns.entrySet().stream().map(e -> {
                return "?";
            }).collect(Collectors.joining(",")));
            sql.append(")").append(suffix);
            return sql.toString();
        }

        private void setDeleteQuery() throws Exception {
            if (deleteQuery == null) {
                deleteQuery = "DELETE FROM " + deleteTableName + " WHERE " + SOSHibernate.quoteColumn(deleteDialect, primaryKeyColumn) + "=?";
            }
        }
    }

}
