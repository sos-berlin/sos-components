package com.sos.jitl.jobs.db.common;

import java.sql.Connection;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.js7.job.Job;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStep;

public abstract class CancelableDatabaseJob<A extends JobArguments> extends Job<A> {

    private static final String CANCELABLE_RESOURCE_NAME_HIBERNATE = "hibernate";
    private static final String CANCELABLE_RESOURCE_NAME_SQL_CONNECTION = "sql_connection";

    public CancelableDatabaseJob(JobContext jobContext) {
        super(jobContext);
    }

    public void addCancelableResource(OrderProcessStep<A> step, SOSHibernateSession session) {
        step.addCancelableResource(CANCELABLE_RESOURCE_NAME_HIBERNATE, session);
    }

    @Override
    public void onOrderProcessCancel(OrderProcessStep<A> step) throws Exception {
        cancelHibernate(step, step.getJobName());
    }

    private void cancelHibernate(OrderProcessStep<A> step, String jobName) {
        try {
            Object o = step.getCancelableResources().get(CANCELABLE_RESOURCE_NAME_HIBERNATE);
            if (o == null) {
                return;
            }

            SOSHibernateSession s = (SOSHibernateSession) o;
            boolean doRollback = false;
            if (s.getCurrentStatement() != null) {
                step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]cancel statement ...");
                try {
                    s.getCurrentStatement().cancel();
                } catch (Throwable ex) {
                    step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][cancel statement]" + ex.toString(), ex);
                }
                doRollback = true;
            }
            Connection conn = s.getConnection();
            // Rollback if the current statement is canceled.
            // Otherwise, rollback execution waits until the current statement completes.
            if (doRollback) {
                try {
                    step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]connection rollback ...");
                    conn.rollback();
                } catch (Throwable ex) {
                    step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][connection rollback]" + ex.toString(), ex);
                }
            } else {
                step.getLogger().info("[" + OPERATION_CANCEL_KILL
                        + "][hibernate][connection rollback][skip]because the current statement is no more active");
            }
            try {
                conn.abort(Runnable::run);
            } catch (Throwable ex) {
                step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][connection abort]" + ex.toString(), ex);
            }
            // close session and factory
            step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]close...");
            s.getFactory().close(s);
            step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]completed");
        } catch (Throwable e) {
            step.getLogger().error(String.format("[%s][job name=%s][cancelHibernate]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    // TODO currently not used(candidate PLSQLJob) because abort is asynchronous and conn.close() is synchronous (waiting for execution is completed)
    // PLSQLJob is cancelled when the Thread is interrupted.
    @SuppressWarnings("unused")
    private void cancelSQLConnection(OrderProcessStep<A> step, String jobName) {
        try {
            Object o = step.getCancelableResources().get(CANCELABLE_RESOURCE_NAME_SQL_CONNECTION);
            if (o != null) {
                step.getLogger().info("[" + OPERATION_CANCEL_KILL + "]abort sql connection ...");
                // ((Connection) o).close();
                ((Connection) o).abort(Runnable::run);
            }
        } catch (Throwable e) {
            step.getLogger().error(String.format("[%s][job name=%s][cancelSQLConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }
}
