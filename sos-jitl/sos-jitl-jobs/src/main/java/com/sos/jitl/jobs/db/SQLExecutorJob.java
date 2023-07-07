package com.sos.jitl.jobs.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSQLExecutor;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;
import com.sos.jitl.jobs.common.OrderProcessStepOutcome;
import com.sos.jitl.jobs.db.SQLExecutorJobArguments.ResultSetAsVariables;
import com.sos.jitl.jobs.db.common.Export2CSV;
import com.sos.jitl.jobs.db.common.Export2JSON;
import com.sos.jitl.jobs.db.common.Export2XML;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

public class SQLExecutorJob extends ABlockingInternalJob<SQLExecutorJobArguments> {

    public SQLExecutorJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<SQLExecutorJobArguments> step) throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(step.getDeclaredArguments().getHibernateFile().getValue());
            factory.setIdentifier(SQLExecutorJob.class.getSimpleName());
            factory.build();
            session = factory.openStatelessSession();
            step.setPayload(session);

            process(step, session);
            step.getLogger().info("result: " + step.getOutcome().getVariables());
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
    }

    private void process(OrderProcessStep<SQLExecutorJobArguments> step, final SOSHibernateSession session) throws Exception {
        SQLExecutorJobArguments args = step.getDeclaredArguments();
        SOSHibernateSQLExecutor executor = session.getSQLExecutor();
        List<String> statements = null;
        try {
            Path path = Paths.get(args.getCommand());
            if (Files.exists(path)) {
                step.getLogger().debug("[load from file]%s", path);
                statements = executor.getStatements(step.replaceVars(path));
            }
        } catch (Throwable e) {
        }
        if (statements == null) {
            statements = executor.getStatements(args.getCommand());
        }

        session.beginTransaction();
        for (String statement : statements) {
            step.getLogger().info("executing database statement: %s", statement);
            if (SOSHibernateSQLExecutor.isResultListQuery(statement, args.getExecReturnsResultset())) {
                executeResultSet(step, executor, statement, step.getOutcome());
            } else {
                executor.executeUpdate(statement);
            }
        }
        session.commit();
    }

    private void executeResultSet(OrderProcessStep<SQLExecutorJobArguments> step, final SOSHibernateSQLExecutor executor, final String statement,
            OrderProcessStepOutcome outcome) throws Exception {
        ResultSet rs = null;
        StringBuilder warning = new StringBuilder();
        try {
            SQLExecutorJobArguments args = step.getDeclaredArguments();

            rs = executor.getResultSet(statement);

            boolean checkResultSet = args.getResultSetAsVariables() != null;
            boolean isParamValue = false;
            if (checkResultSet) {
                switch (args.getResultSetAsVariables()) {
                case CSV:
                    if (args.getResultFile().getValue() == null) {
                        throw new SOSJobRequiredArgumentMissingException(args.getResultFile().getName());
                    }
                    Export2CSV.export(rs, args.getResultFile().getValue(), step.getLogger());
                    return;
                case XML:
                    if (args.getResultFile().getValue() == null) {
                        throw new SOSJobRequiredArgumentMissingException(args.getResultFile().getName());
                    }
                    Export2XML.export(rs, args.getResultFile().getValue(), step.getLogger());
                    return;
                case JSON:
                    if (args.getResultFile().getValue() == null) {
                        throw new SOSJobRequiredArgumentMissingException(args.getResultFile().getName());
                    }
                    Export2JSON.export(rs, args.getResultFile().getValue(), step.getLogger());
                    return;
                default:
                    isParamValue = args.getResultSetAsVariables().equals(ResultSetAsVariables.NAME_VALUE);
                    break;
                }
            }

            if (checkResultSet || args.getResultSetAsWarning()) {
                StringBuilder warn = new StringBuilder();
                int rowCount = 0;
                Map<String, String> record = null;
                while (!(record = executor.nextAsStringMap(rs)).isEmpty()) {
                    rowCount++;
                    if (checkResultSet) {
                        if (isParamValue) {
                            String paramKey = null;
                            String paramValue = null;

                            int columnCounter = 0;
                            for (String key : record.keySet()) {
                                columnCounter++;
                                if (columnCounter == 1) {
                                    paramKey = record.get(key);
                                } else if (columnCounter == 2) {
                                    paramValue = record.get(key);
                                } else {
                                    break;
                                }
                            }
                            if (paramKey != null && paramValue != null) {
                                if (step.getLogger().isDebugEnabled()) {
                                    step.getLogger().debug("[set outcome]%s=%s", paramKey, paramValue);
                                }
                                outcome.putVariable(paramKey, paramValue);
                            }
                        } else {
                            if (rowCount == 1) {
                                for (String key : record.keySet()) {
                                    String value = record.get(key);
                                    if (step.getLogger().isDebugEnabled()) {
                                        step.getLogger().debug("[set outcome]%s=%s", key, value);
                                    }
                                    outcome.putVariable(key, value);
                                }
                            }
                        }
                    }
                    if (args.getResultSetAsWarning()) {
                        if (rowCount > 1) {
                            warn.append(", ");
                        }
                        warn.append(record);
                    }
                }// while

                if (warn.length() > 0) {
                    if (warning.length() > 0) {
                        warning.append("; ");
                    }
                    warning.append(warn);
                }
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            executor.close(rs);
            if (warning.length() > 0) {
                step.getLogger().warn(warning.toString());
            }
        }
    }
}
