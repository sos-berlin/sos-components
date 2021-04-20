package com.sos.jitl.jobs.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSQLExecutor;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.db.SQLExecutorJobArguments.ArgResultSetAsParametersValues;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class SQLExecutorJob extends ABlockingInternalJob<SQLExecutorJobArguments> {

    public SQLExecutorJob(JobContext jobContext) {
        super(jobContext, SQLExecutorJobArguments.class);
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, SQLExecutorJobArguments args) throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            if (SOSString.isEmpty(args.getCommand())) {
                throw new Exception("command is empty. please check the job/order \"command\" parameter.");
            }
            factory = new SOSHibernateFactory(args.getHibernateFile());
            factory.setIdentifier(SQLExecutorJob.class.getSimpleName());
            factory.build();
            session = factory.openStatelessSession();

            return Job.success(process(step, session, args));
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

    private Map<String, Object> process(final BlockingInternalJob.Step step, final SOSHibernateSession session, SQLExecutorJobArguments args)
            throws Exception {
        args.setCommand(args.getCommand().replaceAll(Pattern.quote("\\${"), "\\${")); // replace \${ to ${ TODO - is needed?
        args.setCommand(Job.replaceVars(Job.getSubstitutor(args), args.getCommand()));

        SOSHibernateSQLExecutor executor = session.getSQLExecutor();
        List<String> statements = null;
        try {
            Path path = Paths.get(args.getCommand());
            if (Files.exists(path)) {
                Job.debug(step, "[load from file]%s", path);
                statements = executor.getStatements(path);
            }
        } catch (Throwable e) {
        }
        if (statements == null) {
            statements = executor.getStatements(args.getCommand());
        }

        Map<String, Object> map = new HashMap<String, Object>();
        session.beginTransaction();
        for (String statement : statements) {
            Job.info(step, "executing database statement: %s", statement);
            if (SOSHibernateSQLExecutor.isResultListQuery(statement, args.getExecReturnsResultset())) {
                executeResultSet(step, args, executor, statement, map);
            } else {
                executor.executeUpdate(statement);
            }
        }
        session.commit();
        return map;
    }

    private void executeResultSet(final BlockingInternalJob.Step step, final SQLExecutorJobArguments args, final SOSHibernateSQLExecutor executor,
            final String statement, Map<String, Object> map) throws Exception {
        ResultSet rs = null;
        StringBuilder warning = new StringBuilder();
        try {
            boolean checkResultSet = !args.getResultSetAsParameters().equalsIgnoreCase(ArgResultSetAsParametersValues.FALSE.name());
            boolean isParamValue = args.getResultSetAsParameters().equalsIgnoreCase(ArgResultSetAsParametersValues.NAME_VALUE.name());
            rs = executor.getResultSet(statement);

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
                                Job.debug(step, "[order][set param]%s=%s", paramKey, paramValue);
                                map.put(paramKey, paramValue);
                            }
                        } else {
                            if (rowCount == 1) {
                                for (String key : record.keySet()) {
                                    String value = record.get(key);
                                    Job.debug(step, "[order][set param]%s=%s", key, value);
                                    map.put(key, value);
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
        } catch (Exception e) {
            throw e;
        } finally {
            executor.close(rs);
            if (warning.length() > 0) {
                Job.warn(step, warning);
            }
        }
    }
}
