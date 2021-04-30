package com.sos.jitl.jobs.db.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;

public class SOSPLSQLJob extends ABlockingInternalJob<SOSPLSQLJobArguments> {

    private static final String STD_OUT_OUTPUT = "std_out_output";
    private static final String DBMS_OUTPUT = "dbms_output";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPLSQLJob.class);

    public SOSPLSQLJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<SOSPLSQLJobArguments> step) throws Exception {

        try {

            Map<String, Value> variables = new HashMap<String, Value>();
            if (step != null) {
                variables.putAll(getJobContext().jobArguments());
                variables.putAll(step.getInternalStep().arguments());
                variables.putAll(step.getInternalStep().order().arguments());
            }

            for (Entry<String, Object> entry : Job.convert(variables).entrySet()) {
                String value = "";
                if (entry.getKey().equalsIgnoreCase("db_password")) {
                    value = "********";
                } else {
                    value = entry.getValue().toString();
                }
                String log = String.format("%1$s = %2$s", entry.getKey(), value);
                LOGGER.debug(log);
                if (step != null) {
                    step.getLogger().debug(log);
                }
            }

            Connection connection = getConnection(step.getLogger(), step.getArguments());
            return step.success(process(step.getLogger(), connection, step.getArguments()));
        } catch (Throwable e) {
            throw e;
        }
    }

    private void log(JobLogger logger, String log) {
        LOGGER.info(log);
        if (logger != null) {
            logger.info(log);
        }
    }

    private void debug(JobLogger logger, String log) {
        LOGGER.debug(log);
        if (logger != null) {
            logger.debug(log);
        }
    }

    private Connection getConnection(JobLogger logger, SOSPLSQLJobArguments args) throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        Connection connection = null;

        args.checkRequired();

        try {

            if (args.useHibernateFile()) {

            } else {
                if (args.getCredentialStoreFile() != null) {
                    SOSKeePassResolver r = new SOSKeePassResolver(args.getCredentialStoreFile(), args.getCredentialStoreKeyFile(), args
                            .getCredentialStorePassword());

                    r.setEntryPath(args.getCredentialStoreEntryPath());

                    args.setDbUrl(r.resolve(args.getDbUrl()));

                    args.setDbUser(r.resolve(args.getDbUser()));
                    args.setDbPassword(r.resolve(args.getDbPassword()));
                    debug(logger, args.getCredentialStoreFile());
                    debug(logger, args.getCredentialStoreKeyFile());
                    debug(logger, args.getCredentialStoreEntryPath());
                }

                debug(logger, "dbUrl: " + args.getDbUrl());
                debug(logger, "dbUser: " + args.getDbUser());
                debug(logger, "dbPassword: " + "********");

                DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
                connection = DriverManager.getConnection(args.getDbUrl(), args.getDbUser(), args.getDbPassword());
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
        return connection;

    }

    private String unescapeXML(final String stringValue) {
        String newValue = stringValue;
        if (newValue.indexOf("&") != -1) {
            newValue = newValue.replaceAll("&quot;", "\"");
            newValue = newValue.replaceAll("&lt;", "<");
            newValue = newValue.replaceAll("&gt;", ">");
            newValue = newValue.replaceAll("&amp;", "&");
            newValue = newValue.replaceAll("&apos;", "'");
            newValue = newValue.replaceAll("&#13;", "\r");
            newValue = newValue.replaceAll("&#x0d;", "\r");
            newValue = newValue.replaceAll("&#xd;", "\r");
            newValue = newValue.replaceAll("&#09;", "\t");
            newValue = newValue.replaceAll("&#9;", "\t");
            newValue = newValue.replaceAll("&#10;", "\n");
            newValue = newValue.replaceAll("&#x0a;", "\n");
            newValue = newValue.replaceAll("&#xa;", "\n");
        }
        return newValue;
    }

    private Map<String, Object> process(JobLogger logger, final Connection connection, SOSPLSQLJobArguments args) throws Exception {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(DBMS_OUTPUT, "");
        resultMap.put(STD_OUT_OUTPUT, "");

        CallableStatement callableStatement = null;
        DbmsOutput dbmsOutput = null;

        String plsql = "";
        if ((args.getCommand() != null) && !args.getCommand().isEmpty()) {
            plsql = args.getCommand();
        }
        if (args.getCommandScriptFile() != null) {
            plsql += args.getCommandScriptFileContent();
        }

        plsql = unescapeXML(plsql).replace("\r\n", "\n");
        plsql = Job.replaceVars(Job.getSubstitutor(args), plsql);

        log(logger, String.format("substituted Statement: %s will be executed.", plsql));

        dbmsOutput = new DbmsOutput(connection);
        dbmsOutput.enable(1000000);
        callableStatement = connection.prepareCall(plsql);
        try {
            callableStatement.execute();

            if (dbmsOutput != null) {
                String output = dbmsOutput.getOutput();

                log(logger, output);

                if (output != null) {
                    resultMap.put(DBMS_OUTPUT, output);
                    resultMap.put(STD_OUT_OUTPUT, output);

                    int regExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
                    String[] strA = output.split("\n");
                    boolean aVariableFound = false;
                    String regExp = args.getVariableParserRegExpr();
                    Pattern regExprPattern = Pattern.compile(regExp, regExpFlags);
                    for (String string : strA) {
                        Matcher matcher = regExprPattern.matcher(string);
                        if (matcher.matches() && matcher.group().length() >= 2) {
                            resultMap.put(matcher.group(1), matcher.group(2).trim());
                            aVariableFound = true;
                        }
                    }
                    dbmsOutput.close();
                    if (!aVariableFound) {
                        debug(logger, String.format("no JS-variable definitions found using reg-exp '%1$s'.", regExp));
                    }
                    ResultSetMetaData resultSetMetaData = callableStatement.getMetaData();
                    if (resultSetMetaData != null) {
                        int nCols;
                        nCols = resultSetMetaData.getColumnCount();
                        for (int i = 1; i <= nCols; i++) {
                            debug(logger, resultSetMetaData.getColumnName(i));
                            int colSize = resultSetMetaData.getColumnDisplaySize(i);
                            for (int k = 0; k < colSize - resultSetMetaData.getColumnName(i).length(); k++) {
                                debug(logger, " ");
                            }
                        }
                        debug(logger, "");
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage = String.format("SQL Exception raised. Msg='%1$s', Status='%2$s'", e.getMessage(), e.getSQLState());
            LOGGER.error(errorMessage, e);
            throw new Exception(errorMessage, e);
        } finally {

            if (callableStatement != null) {
                callableStatement.close();
                callableStatement = null;
            }
            if (connection != null) {
                connection.close();
            }

        }
        return resultMap;
    }

    public static void main(String[] args) {
        SOSPLSQLJob sosPLSQLJob = new SOSPLSQLJob(null);
        SOSPLSQLJobArguments arguments = new SOSPLSQLJobArguments();
        arguments.setCommandScripFile("c:/temp/2.sql");
        arguments.setDbPassword("scheduler");
        arguments.setDbUser("scheduler");
        arguments.setVariableParserRegExpr("");
        arguments.setDbUrl("jdbc:oracle:thin:@//LAPTOP-7RSACSCV:1521/xe");
        // arguments.setHibernateFile(Paths.get("D:/documents/sos-berlin.com/scheduler_joc_cockpit/oracle/hibernate.cfg.xml"));

        Connection connection;
        try {
            connection = sosPLSQLJob.getConnection(null, arguments);
            sosPLSQLJob.process(null, connection, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}