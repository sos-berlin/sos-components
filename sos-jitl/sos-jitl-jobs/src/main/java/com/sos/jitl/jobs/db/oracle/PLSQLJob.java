package com.sos.jitl.jobs.db.oracle;

import java.nio.file.Files;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.db.common.CancelableDatabaseJob;
import com.sos.jitl.jobs.db.common.Export2CSV;
import com.sos.jitl.jobs.db.common.Export2JSON;
import com.sos.jitl.jobs.db.common.Export2XML;
import com.sos.js7.job.OrderProcessStep;

public class PLSQLJob extends CancelableDatabaseJob<PLSQLJobArguments> {

    private static final String STD_OUT_OUTPUT = "std_out_output";
    private static final String DBMS_OUTPUT = "dbms_output";

    public PLSQLJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<PLSQLJobArguments> step) throws Exception {
        step.getDeclaredArguments().checkRequired();

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        try {
            factory = getHibernateFactory(step);
            session = factory.openStatelessSession(PLSQLJob.class.getSimpleName());
            addCancelableResource(step, session);
            process(step, session);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (factory != null) {
                try {
                    factory.close(session);
                } catch (Throwable e) {
                }
            }
        }
    }

    private SOSHibernateFactory getHibernateFactory(OrderProcessStep<PLSQLJobArguments> step) throws Exception {
        PLSQLJobArguments args = step.getDeclaredArguments();
        SOSHibernateFactory f = null;
        if (args.useHibernateFile()) {
            if (!Files.exists(args.getHibernateFile())) {
                throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", args.getHibernateFile()));
            }
            f = new SOSHibernateFactory(args.getHibernateFile());
        } else {
            Properties p = new Properties();
            // required
            p.put(SOSHibernate.HIBERNATE_PROPERTY_DIALECT, args.getDbDialect().getValue());
            p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_DRIVERCLASS, args.getDbDriverClass().getValue());
            p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, args.getDbUrl().getValue());
            // optional
            if (!SOSString.isEmpty(args.getDbUser().getValue())) {
                p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, args.getDbUser().getValue());
            } else {
                p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, "");
            }
            if (!SOSString.isEmpty(args.getDbPassword().getValue())) {
                p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, args.getDbPassword().getValue());
            } else {
                p.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, "");
            }
            // set hikariCP as the default connection pool
            p.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            // TODO: set poolsize to 1 for this job
            p.put("hibernate.hikari.maximumPoolSize", "1");
            f = new SOSHibernateFactory();
            f.getConfigurationProperties().putAll(p);
        }

        CredentialStoreArguments csArgs = step.getIncludedArguments(CredentialStoreArguments.class);
        if (csArgs != null) {
            Properties p = new Properties();
            if (!SOSString.isEmpty(csArgs.getFile().getValue())) {
                p.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE, csArgs.getFile().getValue());
            }
            if (!SOSString.isEmpty(csArgs.getKeyFile().getValue())) {
                p.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE, csArgs.getKeyFile().getValue());
            }
            if (!SOSString.isEmpty(csArgs.getPassword().getValue())) {
                p.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD, csArgs.getPassword().getValue());
            }
            if (!SOSString.isEmpty(csArgs.getEntryPath().getValue())) {
                p.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH, csArgs.getEntryPath().getValue());
            }
            if (p.size() > 0) {
                f.getConfigurationProperties().putAll(p);
            }
        }

        f.build();
        return f;
    }

    private void process(OrderProcessStep<PLSQLJobArguments> step, final SOSHibernateSession session) throws Exception {

        PLSQLJobArguments args = step.getDeclaredArguments();
        String plsql = "";
        if ((args.getCommand() != null) && !args.getCommand().isEmpty()) {
            plsql = args.getCommand();
        }
        if (args.getCommandScriptFile() != null) {
            plsql += args.getCommandScriptFileContent();
        }
        plsql = unescapeXML(plsql).replace("\r\n", "\n");
        step.getLogger().info(String.format("substituted Statement: %s", plsql));

        step.getOutcome().putVariable(DBMS_OUTPUT, "");
        step.getOutcome().putVariable(STD_OUT_OUTPUT, "");

        DbmsOutput out = null;
        CallableStatement cs = null;
        try {
            Connection connection = session.getConnection();
            out = new DbmsOutput(connection);
            out.enable(1000000);

            cs = connection.prepareCall(plsql);
            session.setCurrentStatement(cs);
            cs.execute();

            String output = out.getOutput();
            step.getLogger().info(output);

            if (output != null) {
                step.getOutcome().putVariable(DBMS_OUTPUT, output);
                step.getOutcome().putVariable(STD_OUT_OUTPUT, output);

                int regExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
                Pattern regExprPattern = Pattern.compile(args.getVariableParserRegExpr(), regExpFlags);

                boolean found = false;
                String[] arr = output.split("\n");
                for (String string : arr) {
                    Matcher matcher = regExprPattern.matcher(string);
                    if (matcher.matches() && matcher.group().length() >= 2) {
                        step.getOutcome().putVariable(matcher.group(1), matcher.group(2).trim());
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
            session.resetCurrentStatement();
        }
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