package com.sos.joc.history.helper;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSSerializer;
import com.sos.joc.Globals;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.history.HistoryService;

public class LogExtAsyncHandler implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LogExtAsyncHandler.class);

    private final String jocVariableName;

    private CopyOnWriteArraySet<LogExtAsync> logs = new CopyOnWriteArraySet<>();
    private AtomicBoolean closed = new AtomicBoolean();

    public LogExtAsyncHandler(String jocVariableName) {
        HistoryService.setLogger();
        this.jocVariableName = jocVariableName;
    }

    public void start(ThreadGroup threadGroup) {
        closed.set(false);

        deserialize();
    }

    public void close(StartupMode mode) {
        closed.set(true);

        // if (threadPool != null) {
        // MonitorService.setLogger();
        // JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        // threadPool = null;
        serialize();
        // }
    }

    public void add(Path log, String workflowName, String orderId, Long taskId) {
        logs.add(new LogExtAsync(log, workflowName, orderId, taskId));
    }

    public void handleLongs() {
        if (logs.size() == 0) {
            return;
        }
        List<LogExtAsync> copy = new ArrayList<>(logs);
        List<LogExtAsync> toRemove = new ArrayList<>();
        for (LogExtAsync l : copy) {

            toRemove.add(l);
        }
        logs.removeAll(toRemove);
    }

    private void serialize() {
        int s = logs.size();
        if (s > 0) {
            try {
                saveJocVariable(new SOSSerializer<CopyOnWriteArraySet<LogExtAsync>>().serializeCompressed2bytes(logs));
                LOGGER.info(String.format("[%s][serialized]logs=%s", jocVariableName, s));
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][serialize]%s", jocVariableName, e.toString()), e);
            }
            logs.clear();
        } else {
            deleteJocVariable();
        }
    }

    private void deserialize() {
        DBItemJocVariable var = null;
        try {
            var = getJocVariable();
            if (var == null) {
                return;
            }
            deserialize(var);
            deleteJocVariable();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][deserialize]%s", jocVariableName, e.toString()), e);
        }
    }

    private void deserialize(DBItemJocVariable var) throws Exception {
        int s = 0;

        CopyOnWriteArraySet<LogExtAsync> sr = new SOSSerializer<CopyOnWriteArraySet<LogExtAsync>>().deserializeCompressed(var.getBinaryValue());
        if (sr != null && sr.size() > 0) {
            s = sr.size();
            logs.addAll(sr);
        }
        LOGGER.info(String.format("[%s][deserialized]logs=%s", jocVariableName, s));
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(jocVariableName);
            Query<DBItemJocVariable> query = session.createQuery(String.format("from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES));
            query.setParameter("name", jocVariableName);
            return session.getSingleResult(query);
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void saveJocVariable(byte[] val) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(jocVariableName);
            session.beginTransaction();

            DBItemJocVariable item = getJocVariable();
            if (item == null) {
                item = new DBItemJocVariable();
                item.setName(jocVariableName);
                item.setBinaryValue(val);
                session.save(item);
            } else {
                item.setBinaryValue(val);
                session.update(item);
            }

            session.commit();
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void deleteJocVariable() {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(jocVariableName);
            session.beginTransaction();

            Query<DBItemJocVariable> query = session.createQuery(String.format("delete from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES));
            query.setParameter("name", jocVariableName);
            session.executeUpdate(query);

            session.commit();
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public class LogExtAsync {

        private final Path log;
        private final String workflowName;
        private final String orderId;
        private final Long taskId;

        private LogExtAsync(Path log, String workflowName, String orderId, Long taskId) {
            this.log = log;
            this.workflowName = workflowName;
            this.orderId = orderId;
            this.taskId = taskId;
        }

        public Path getLog() {
            return log;
        }

        public String getWorkflowName() {
            return workflowName;
        }

        public String getOrderId() {
            return orderId;
        }

        public Long getTaskId() {
            return taskId;
        }

    }
}
