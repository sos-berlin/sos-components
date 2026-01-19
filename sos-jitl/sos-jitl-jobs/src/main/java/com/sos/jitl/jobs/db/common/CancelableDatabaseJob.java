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
    public void onProcessOrderCanceled(OrderProcessStep<A> step) throws Exception {
        cancelHibernate(step, step.getCancelableResources().get(CANCELABLE_RESOURCE_NAME_HIBERNATE));
    }

    /**
     * <p>
     * Properly cancels a Hibernate query if {@link SOSHibernateSession#getSQLExecutor()} was used,<br/>
     * because it uses a JDBC Statement that can be cancelled.
     * <p>
     * In contrast, the standard Hibernate API does not provide a way to cancel queries<br/>
     * executed via methods like {@link SOSHibernateSession#executeUpdate(String)} or {@link SOSHibernateSession#getResultList(String)} .<br/>
     *
     * @param step the JS7 step
     * @param cancelableResource the resource that can be cancelled (session) */
    public static void cancelHibernate(OrderProcessStep<?> step, Object cancelableResource) {
        try {
            if (cancelableResource == null) {
                return;
            }

            SOSHibernateSession session = (SOSHibernateSession) cancelableResource;
            boolean currentStatementCanceled = false;
            // 1) Cancel JDBC statement (if SOSHibernateSession#getSQLExecutor() was used)
            if (session.getCurrentStatement() != null) {
                step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]cancel statement ...");
                try {
                    session.getCurrentStatement().cancel();
                } catch (Exception ex) {
                    step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][cancel statement]" + ex.toString(), ex);
                }
                currentStatementCanceled = true;
            }

            Connection conn = session.getConnection();
            // 2) Rollback if the current JDBC statement is canceled.
            // - Otherwise, rollback execution waits until the current statement completes.
            if (currentStatementCanceled) {
                try {
                    step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]connection rollback ...");
                    conn.rollback();
                } catch (Exception ex) {
                    step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][connection rollback]" + ex.toString(), ex);
                }
            } else {
                step.getLogger().info("[" + OPERATION_CANCEL_KILL
                        + "][hibernate][connection rollback][skip]because the current jdbc statement cannot be evaluated");
            }

            // 3) Abort connection
            try {
                conn.abort(Runnable::run);
            } catch (Exception ex) {
                step.getLogger().warn("[" + OPERATION_CANCEL_KILL + "][hibernate][connection abort]" + ex.toString(), ex);
            }

            // 4) Close session/factory
            step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]close...");
            session.getFactory().close(session);
            step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][hibernate]completed");
        } catch (Exception e) {
            String jobName = "unknown";
            try {
                jobName = step.getJobName();
            } catch (Exception ex) {

            }
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
        } catch (Exception e) {
            step.getLogger().error(String.format("[%s][job name=%s][cancelSQLConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }
}
