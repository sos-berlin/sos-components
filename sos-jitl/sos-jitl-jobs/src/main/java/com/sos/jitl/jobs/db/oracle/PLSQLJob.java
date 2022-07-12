package com.sos.jitl.jobs.db.oracle;

import java.nio.file.Files;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.cfg.Configuration;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments.SOSCredentialStoreResolver;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.db.common.Export2CSV;
import com.sos.jitl.jobs.db.common.Export2JSON;
import com.sos.jitl.jobs.db.common.Export2XML;

import js7.data_for_java.order.JOutcome;

public class PLSQLJob extends ABlockingInternalJob<PLSQLJobArguments> {

    private static final String STD_OUT_OUTPUT = "std_out_output";
    private static final String DBMS_OUTPUT = "dbms_output";

    public PLSQLJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<PLSQLJobArguments> step) throws Exception {
        step.getArguments().checkRequired();

        Connection conn = null;
        try {
            conn = getConnection(step);

            return step.success(process(step, conn));
        } catch (Throwable e) {
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    private Connection getConnection(JobStep<PLSQLJobArguments> step) throws Exception {
        PLSQLJobArguments args = step.getArguments();
        SOSCredentialStoreArguments csArgs = step.getAppArguments(SOSCredentialStoreArguments.class);
        if (args.useHibernateFile()) {
            if (!Files.exists(args.getHibernateFile())) {
                throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", args.getHibernateFile()));
            }
            Configuration configuration = new Configuration();
            configuration.configure(args.getHibernateFile().toFile());

            String s = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
            if (s != null) {
                args.setDbUrl(s);
            }
            s = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
            if (s != null) {
                args.setDbUser(s);
            }
            s = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
            if (s != null) {
                args.setDbPassword(s);
            }
        }
        if (csArgs.getFile().getValue() != null) {
            SOSCredentialStoreResolver r = csArgs.newResolver();

            args.setDbUrl(r.resolve(args.getDbUrl().getValue()));
            args.setDbUser(r.resolve(args.getDbUser().getValue()));
            args.setDbPassword(r.resolve(args.getDbPassword().getValue()));
        }
        step.getLogger().debug("dbUrl=%s, dbUser=%s, dbPassword=%s", args.getDbUrl().getDisplayValue(), args.getDbUser().getDisplayValue(), args
                .getDbPassword().getDisplayValue());

        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        Connection connection = null;
        if (args.getDbUser().getValue() != null && args.getDbPassword().getValue() != null) {
            step.getLogger().debug("Connecting with user and password.");
            connection = DriverManager.getConnection(args.getDbUrl().getValue(), args.getDbUser().getValue(), args.getDbPassword().getValue());
        } else {
            step.getLogger().debug("Empty user and password. Trying wallet");
            connection = DriverManager.getConnection(args.getDbUrl().getValue());
        }
        return connection;
    }

    private Map<String, Object> process(JobStep<PLSQLJobArguments> step, final Connection connection) throws Exception {

        PLSQLJobArguments args = step.getArguments();
        String plsql = "";
        if ((args.getCommand() != null) && !args.getCommand().isEmpty()) {
            plsql = args.getCommand();
        }
        if (args.getCommandScriptFile() != null) {
            plsql += args.getCommandScriptFileContent();
        }
        plsql = unescapeXML(plsql).replace("\r\n", "\n");
        step.getLogger().info(String.format("substituted Statement: %s", plsql));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(DBMS_OUTPUT, "");
        result.put(STD_OUT_OUTPUT, "");

        DbmsOutput out = null;
        CallableStatement cs = null;
        try {
            out = new DbmsOutput(connection);
            out.enable(1000000);

            cs = connection.prepareCall(plsql);
            cs.execute();

            String output = out.getOutput();
            step.getLogger().info(output);

            if (output != null) {
                result.put(DBMS_OUTPUT, output);
                result.put(STD_OUT_OUTPUT, output);

                int regExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
                Pattern regExprPattern = Pattern.compile(args.getVariableParserRegExpr(), regExpFlags);

                boolean found = false;
                String[] arr = output.split("\n");
                for (String string : arr) {
                    Matcher matcher = regExprPattern.matcher(string);
                    if (matcher.matches() && matcher.group().length() >= 2) {
                        result.put(matcher.group(1), matcher.group(2).trim());
                        found = true;
                    }
                }
                out.close();
                out = null;
                if (!found) {
                    step.getLogger().debug(String.format("no JS-variable definitions found using reg-exp '%1$s'.", args.getVariableParserRegExpr()));
                }
            }

            if (args.getResultSetAs() != null) {
                // resultFile already checked by checkRequired
                ResultSet rs = cs.getResultSet();
                if (rs == null) {
                    step.getLogger().info("[export][%s][skip]command did not generate a result set.", args.getResultFile().toString());
                } else {
                    switch (args.getResultSetAs()) {
                    case CSV:
                        Export2CSV.export(rs, args.getResultFile(), step.getLogger());
                        break;
                    case XML:
                        Export2XML.export(rs, args.getResultFile(), step.getLogger());
                        break;
                    case JSON:
                        Export2JSON.export(rs, args.getResultFile(), step.getLogger());
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            String msg = String.format("SQL Exception raised. Msg='%1$s', Status='%2$s'", e.getMessage(), e.getSQLState());
            step.getLogger().debug(msg);
            throw new Exception(msg, e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (cs != null) {
                cs.close();
                cs = null;
            }
        }
        return result;
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
}