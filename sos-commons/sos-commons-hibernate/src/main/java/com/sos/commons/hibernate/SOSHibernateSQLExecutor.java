package com.sos.commons.hibernate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.common.SOSBatchObject;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateSQLCommandExtractorException;
import com.sos.commons.hibernate.exception.SOSHibernateSQLExecutorException;
import com.sos.commons.util.SOSString;

public class SOSHibernateSQLExecutor implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSQLExecutor.class);
    private static final long serialVersionUID = 1L;

    private final SOSHibernateSession session;
    private String logIdentifier;
    private boolean execReturnsResultSet;

    protected SOSHibernateSQLExecutor(SOSHibernateSession sess) {
        session = sess;
        logIdentifier = SOSHibernate.getLogIdentifier(session == null ? null : session.getIdentifier());
    }

    /** see getResultSet */
    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable e) {
            }
            try {
                rs.getStatement().close();
            } catch (Throwable e) {
            }
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public boolean execute(String... sqls) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "execute") : "";
        boolean result = false;
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            for (String sql : sqls) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s]", method, sql));
                }
                try {
                    result = stmt.execute(sql);
                } catch (SQLException e) {
                    throw new SOSHibernateSQLExecutorException(e, sql);
                }
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int[] executeBatch(List<String> sqls) throws SOSHibernateException {
        int[] result = null;
        if (sqls != null && sqls.size() > 0) {
            result = executeBatch(sqls.toArray(new String[sqls.size()]));
        }
        return result;
    }

    public int[] executeBatch(String... sqls) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "executeBatch") : "";
        int[] result = null;
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            for (String sql : sqls) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[addBatch][%s]", method, sql));
                }
                try {
                    stmt.addBatch(sql);
                } catch (SQLException e) {
                    throw new SOSHibernateSQLExecutorException(e, sql);
                }
            }
            result = stmt.executeBatch();
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    public int[] executeBatch(String tableName, String sql, Collection<Collection<SOSBatchObject>> rows) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "executeBatch") : "";
        int[] result = null;
        PreparedStatement stmt = null;
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%s", method, sql));
            }
            Connection conn = getConnection();
            Map<String, Integer> columnsMeta = getColumnsMeta(conn, tableName);
            stmt = conn.prepareStatement(sql);
            session.setCurrentStatement(stmt);
            int i = 0;
            for (Collection<SOSBatchObject> row : rows) {
                i++;
                for (SOSBatchObject bo : row) {
                    try {
                        stmt.setObject(bo.getIndex(), bo.getValue(), columnsMeta.get(bo.getColumnName().toLowerCase()));
                    } catch (SQLException e) {
                        throw new SOSHibernateSQLExecutorException(e, sql);
                    }
                }
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][addBatch]%s", method, i, row.stream().map(e -> {
                        return SOSString.toString(e);
                    }).collect(Collectors.joining(","))));
                }
                stmt.addBatch();
            }
            result = stmt.executeBatch();
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @param connection
     * @param tableName
     * @return Map: key - column name lower case, value - SQL type
     * @throws SOSHibernateException */
    public Map<String, Integer> getColumnsMeta(Connection connection, String tableName) throws SOSHibernateException {
        Map<String, Integer> result = new HashMap<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE 1=0");

            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 1; i <= count; i++) {
                result.put(meta.getColumnName(i).toLowerCase(), meta.getColumnType(i));
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public void executeQuery(String sql) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]", SOSHibernate.getMethodName(logIdentifier, "executeQuery"), sql));
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException,
     *             SOSHibernateSQLCommandExtractorException */
    public void executeStatements(Path file) throws SOSHibernateException {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(file);
        } catch (Throwable e) {
            throw new SOSHibernateSQLExecutorException(String.format("cannot read file %s", file), e);
        }
        executeStatements(new String(bytes));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException,
     *             SOSHibernateSQLCommandExtractorException */
    public void executeStatements(String content) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "executeStatements") : "";
        Statement stmt = null;
        String command = null;
        try {
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            List<String> commands = getStatements(content);
            for (int i = 0; i < commands.size(); i++) {
                command = commands.get(i);
                if (isResultListQuery(command, execReturnsResultSet)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[executeQuery][%s]", method, command));
                    }
                    ResultSet rs = null;
                    try {
                        rs = stmt.executeQuery(command);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[executeUpdate][%s]", method, command));
                    }
                    stmt.executeUpdate(command);
                }
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, command);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int executeUpdate(String... sqls) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "executeUpdate") : "";
        int result = 0;
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            for (String sql : sqls) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s]", method, sql));
                }
                try {
                    result += stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new SOSHibernateSQLExecutorException(e, sql);
                }
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int executeUpdateCallableStatement(String sql) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]", SOSHibernate.getMethodName(logIdentifier, "executeUpdateCallableStatement"), sql));
        }
        int result = -1;
        CallableStatement stmt = null;
        try {
            stmt = getConnection().prepareCall(sql);
            session.setCurrentStatement(stmt);
            result = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public byte[] getBlob(String sql) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]", SOSHibernate.getMethodName(logIdentifier, "getBlob"), sql));
        }
        Statement stmt = null;
        ResultSet rs = null;
        byte[] result = {};
        try {
            if (SOSString.isEmpty(sql)) {
                throw new SOSHibernateSQLExecutorException("missing sql");
            }
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getBytes(1);
            }
            if (result == null) {
                return result;
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public long getBlob(String sql, Path path) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]path=%s", SOSHibernate.getMethodName(logIdentifier, "getBlob"), sql, path));
        }
        Statement stmt = null;
        ResultSet rs = null;
        InputStream in = null;
        FileOutputStream out = null;
        long result = 0;
        try {
            if (SOSString.isEmpty(sql)) {
                throw new SOSHibernateSQLExecutorException("missing sql");
            }
            if (path == null) {
                throw new SOSHibernateSQLExecutorException("path is null");
            }
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            rs = stmt.executeQuery(sql);
            int len = 0;
            if (rs.next()) {
                in = rs.getBinaryStream(1);
                if (in == null) {
                    return result;
                }
                byte[] buff = new byte[1024];
                if ((len = in.read(buff)) > 0) {
                    out = new FileOutputStream(path.toFile());
                    out.write(buff, 0, len);
                    result += len;
                } else {
                    return result;
                }
                while (0 < (len = in.read(buff))) {
                    out.write(buff, 0, len);
                    result += len;
                }
            }
        } catch (IOException e) {
            throw new SOSHibernateSQLExecutorException(String.format("can't write to file %s", path), e);
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public String getClob(String sql) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]", SOSHibernate.getMethodName(logIdentifier, "getClob"), sql));
        }
        Statement stmt = null;
        ResultSet rs = null;
        Reader in = null;
        StringBuilder result = new StringBuilder();
        try {
            if (SOSString.isEmpty(sql)) {
                throw new SOSHibernateSQLExecutorException("missing sql");
            }
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            try {
                rs = stmt.executeQuery(sql);
            } catch (SQLException e) {
                throw new SOSHibernateSQLExecutorException(e, sql);
            }

            int bytesRead;
            if (rs.next()) {
                in = rs.getCharacterStream(1);
                if (in == null) {
                    return "";
                }
                if ((bytesRead = in.read()) != -1) {
                    result.append((char) bytesRead);
                } else {
                    return "";
                }
                while ((bytesRead = in.read()) != -1) {
                    result.append((char) bytesRead);
                }
            }
        } catch (IOException e) {
            throw new SOSHibernateSQLExecutorException("exception during read bytes from clob", e);
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
            session.resetCurrentStatement();
        }
        return result.toString();
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public long getClob(String sql, Path path) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]path=%s", SOSHibernate.getMethodName(logIdentifier, "getClob"), sql, path));
        }
        Statement stmt = null;
        ResultSet rs = null;
        Reader in = null;
        FileOutputStream out = null;
        long result = 0;
        try {
            if (SOSString.isEmpty(sql)) {
                throw new SOSHibernateSQLExecutorException("missing sql");
            }
            if (path == null) {
                throw new SOSHibernateSQLExecutorException("path is null");
            }
            stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            try {
                rs = stmt.executeQuery(sql);
            } catch (SQLException e) {
                throw new SOSHibernateSQLExecutorException(e, sql);
            }
            int bytesRead = 0;
            if (rs.next()) {
                in = rs.getCharacterStream(1);
                if (in == null) {
                    return result;
                }
                if ((bytesRead = in.read()) != -1) {
                    out = new FileOutputStream(path.toFile());
                    out.write(bytesRead);
                    result++;
                } else {
                    return result;
                }
                while ((bytesRead = in.read()) != -1) {
                    out.write(bytesRead);
                    result++;
                }
            }
        } catch (IOException e) {
            throw new SOSHibernateSQLExecutorException(String.format("can't write to file %s", path), e);
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            try {
                out.flush();
            } catch (Exception e) {
                //
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
            session.resetCurrentStatement();
        }
        return result;
    }

    /** ResultSet rs = session.getSQLExecutor().getResultSet("select * from ...");
     * 
     * Map<String, String> record = null;
     * 
     * while (!(record = session.getSQLExecutor().next(rs)).isEmpty()) {
     * 
     * LOGGER.info("record = " + result);
     * 
     * }
     * 
     * session.getSQLExecutor().close(rs);
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public ResultSet getResultSet(String sql) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]", SOSHibernate.getMethodName(logIdentifier, "getResultSet"), sql));
        }
        try {
            Statement stmt = getConnection().createStatement();
            session.setCurrentStatement(stmt);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql);
        } finally {
            session.resetCurrentStatement();
        }
    }

    public List<String> getStatements(Path file) throws SOSHibernateSQLCommandExtractorException, SOSHibernateSQLExecutorException {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(file);
        } catch (Throwable e) {
            throw new SOSHibernateSQLExecutorException(String.format("cannot read file %s", file), e);
        }
        return getStatements(new String(bytes));
    }

    public List<String> getStatements(String content) throws SOSHibernateSQLCommandExtractorException {
        SOSSQLCommandExtractor extractor = new SOSSQLCommandExtractor(session.getFactory().getDbms());
        return extractor.extractCommands(content);
    }

    /** see getResultSet */
    public Map<String, Object> next(ResultSet rs) throws SOSHibernateSQLExecutorException {
        Map<String, Object> record = new LinkedHashMap<String, Object>();
        try {
            if (rs != null && rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    record.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        }
        return record;
    }

    /** see getResultSet */
    public Map<String, String> nextAsStringMap(ResultSet rs) throws SOSHibernateSQLExecutorException {
        Map<String, String> record = new LinkedHashMap<String, String>();
        try {
            if (rs != null && rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    String name = meta.getColumnName(i);
                    String value = rs.getString(name);
                    if (SOSString.isEmpty(value)) {
                        value = "";
                    }
                    record.put(name.toLowerCase(), value.trim());
                }
            }
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e);
        }
        return record;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public void setDefaults() throws SOSHibernateException {
        Dbms dbms = session.getFactory().getDbms();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s dbms=%s", SOSHibernate.getMethodName(logIdentifier, "setDefaults"), dbms));
        }
        String dateFormat;
        String language;
        switch (dbms) {
        case MSSQL:
            // default set LOCK_TIMEOUT xxx was set by the SOSHibernateFactory
            dateFormat = "set DATEFORMAT ymd";
            language = "set LANGUAGE British";
            execute(dateFormat, language);
            break;
        case MYSQL:
            execute("SET SESSION SQL_MODE='ANSI_QUOTES'");
            break;
        case ORACLE:
            String nlsNumericCharacters = "ALTER SESSION SET NLS_NUMERIC_CHARACTERS='.,'";
            String nlsDateFormat = "ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD HH24:MI:SS'";
            String nlsSort = "ALTER SESSION SET NLS_SORT='BINARY'";
            executeBatch(nlsNumericCharacters, nlsDateFormat, nlsSort);
            executeUpdateCallableStatement("begin dbms_output.enable(10000); end;");
            break;
        case PGSQL:
            String lcNumeric = "SELECT set_config('lc_numeric', '', true)";
            String dateStyle = "SELECT set_config('datestyle', 'ISO, YMD', true)";
            String defaultTransactionIsolation = "SELECT set_config('default_transaction_isolation', 'repeatable read', true)";
            execute(lcNumeric, dateStyle, defaultTransactionIsolation);
            break;

        case H2:
        case UNKNOWN:
            break;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int updateBlob(byte[] data, String tableName, String columnName, String condition) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s tableName=%s, columnName=%s, condition=%s", SOSHibernate.getMethodName(logIdentifier, "updateBlob"),
                    tableName, columnName, condition));
        }
        if (data == null || data.length <= 0) {
            throw new SOSHibernateSQLExecutorException("missing data");
        }
        int result = data.length;
        updateBlob(new ByteArrayInputStream(data), result, tableName, columnName, condition);
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public void updateBlob(InputStream inputStream, int dataLength, String tableName, String columnName, String condition)
            throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s tableName=%s, columnName=%s, condition=%s", SOSHibernate.getMethodName(logIdentifier, "updateBlob"),
                    tableName, columnName, condition));
        }
        PreparedStatement pstmt = null;
        StringBuilder sql = null;
        try {
            if (SOSString.isEmpty(tableName)) {
                throw new SOSHibernateSQLExecutorException("missing tableName");
            }
            if (SOSString.isEmpty(columnName)) {
                throw new SOSHibernateSQLExecutorException("missing columnName");
            }
            if (inputStream == null) {
                throw new SOSHibernateSQLExecutorException("input stream is null");
            }
            sql = new StringBuilder();
            sql.append("UPDATE ");
            sql.append(tableName);
            sql.append(" SET ").append(columnName).append(" = ? ");
            if (condition != null) {
                String where = condition.trim();
                if (!SOSString.isEmpty(where)) {
                    if (where.toUpperCase().startsWith("WHERE")) {
                        sql.append(" ").append(where);
                    } else {
                        sql.append(" WHERE ").append(where);
                    }
                }
            }
            pstmt = getConnection().prepareStatement(sql.toString());
            session.setCurrentStatement(pstmt);
            pstmt.setBinaryStream(1, inputStream, dataLength);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql == null ? null : sql.toString());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    //
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    //
                }
            }
            session.resetCurrentStatement();
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int updateBlob(Path path, String tableName, String columnName, String condition) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s path=%s, tableName=%s, columnName=%s, condition=%s", SOSHibernate.getMethodName(logIdentifier,
                    "updateBlob"), path, tableName, columnName, condition));
        }
        if (path == null) {
            throw new SOSHibernateSQLExecutorException("path is null");
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new SOSHibernateSQLExecutorException(String.format("file %s doesn't exist", path));
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (Throwable e) {
            throw new SOSHibernateSQLExecutorException(String.format("cannot read file %s", file), e);
        }
        updateBlob(fis, result, tableName, columnName, condition);
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int updateClob(Path path, String tableName, String columnName, String condition) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s path=%s, tableName=%s, columnName=%s, condition=%s", SOSHibernate.getMethodName(logIdentifier,
                    "updateClob"), path, tableName, columnName, condition));
        }
        if (path == null) {
            throw new SOSHibernateSQLExecutorException("path is null");
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new SOSHibernateSQLExecutorException(String.format("file %s doesn't exist", path));
        }
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (Throwable e) {
            throw new SOSHibernateSQLExecutorException(String.format("cannot read file %s", file), e);
        }
        updateClob(fr, result, tableName, columnName, condition);
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public void updateClob(Reader reader, int dataLength, String tableName, String columnName, String condition) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s tableName=%s, columnName=%s, condition=%s", SOSHibernate.getMethodName(logIdentifier, "updateClob"),
                    tableName, columnName, condition));
        }
        PreparedStatement pstmt = null;
        StringBuilder sql = null;
        try {
            if (SOSString.isEmpty(tableName)) {
                throw new SOSHibernateSQLExecutorException("missing tableName");
            }
            if (SOSString.isEmpty(columnName)) {
                throw new SOSHibernateSQLExecutorException("missing columnName");
            }
            if (reader == null) {
                throw new SOSHibernateSQLExecutorException("reader is null");
            }
            sql = new StringBuilder();
            sql.append("UPDATE ");
            sql.append(tableName);
            sql.append(" SET ").append(columnName).append(" = ? ");
            if (condition != null) {
                String where = condition.trim();
                if (!SOSString.isEmpty(where)) {
                    if (where.toUpperCase().startsWith("WHERE")) {
                        sql.append(" ").append(where);
                    } else {
                        sql.append(" WHERE ").append(where);
                    }
                }
            }
            pstmt = getConnection().prepareStatement(sql.toString());
            session.setCurrentStatement(pstmt);
            pstmt.setCharacterStream(1, reader, dataLength);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SOSHibernateSQLExecutorException(e, sql == null ? null : sql.toString());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    //
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable e) {
                }
            }
            session.resetCurrentStatement();
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateConnectionException, SOSHibernateSQLExecutorException */
    public int updateClob(String data, String tableName, String columnName, String condition) throws SOSHibernateException {
        if (SOSString.isEmpty(data)) {
            throw new SOSHibernateSQLExecutorException("missing data");
        }
        int result = data.length();
        updateClob(new java.io.StringReader(data), result, tableName, columnName, condition);
        return result;
    }

    public boolean isExecReturnsResultSet() {
        return execReturnsResultSet;
    }

    public void setExecReturnsResultSet(final boolean val) {
        execReturnsResultSet = val;
    }

    private Connection getConnection() throws SOSHibernateException {
        if (session == null) {
            throw new SOSHibernateSQLExecutorException("session is null");
        }
        Connection conn = session.getConnection();
        if (conn == null) {
            throw new SOSHibernateSQLExecutorException("SQL connection is null");
        }
        return conn;
    }

    public static boolean isResultListQuery(final String sql, final boolean execReturnsResultSet) {
        String stmt = sql.toLowerCase();
        return stmt.startsWith("select") || (stmt.startsWith("exec") && execReturnsResultSet);
    }

    public static String[] getColumnLabels(ResultSet rs) throws SQLException {
        if (rs == null) {
            return null;
        }
        ResultSetMetaData m = rs.getMetaData();
        if (m == null) {
            return null;
        }

        final int count = m.getColumnCount();
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = m.getColumnLabel(i + 1);
        }
        return result;
    }

    public static String sqlValueToString(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof java.sql.Clob) {
            return toString((java.sql.Clob) o);
        }
        return o.toString();
    }

    public static String toString(java.sql.Clob clob) {
        final StringBuilder sb = new StringBuilder();
        try {
            final Reader reader = clob.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);
            int b;
            while (-1 != (b = br.read())) {
                sb.append((char) b);
            }
            br.close();
        } catch (Throwable e) {
            return e.toString();
        }
        return sb.toString();
    }
}
