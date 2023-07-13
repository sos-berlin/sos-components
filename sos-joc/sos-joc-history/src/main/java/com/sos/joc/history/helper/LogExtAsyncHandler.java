package com.sos.joc.history.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.logs.LogOrderContent;
import com.sos.joc.classes.logs.LogTaskContent;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc.LogExtType;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.history.HistoryService;
import com.sos.joc.history.helper.LogExt.Type;

public class LogExtAsyncHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogExtAsyncHandler.class);

    private final String THREAD_POOL_SUFFIX_MAIN = "m";
    private final String THREAD_POOL_SUFFIX_HANDLE = "h";

    private final JocHistoryConfiguration conf;
    private final String jocVariableName;
    private final SOSAuthFolderPermissions folderPermissions;

    private CopyOnWriteArraySet<LogExt> logs = new CopyOnWriteArraySet<>();
    private CopyOnWriteArraySet<LogExt> notProcessed = new CopyOnWriteArraySet<>();
    private AtomicBoolean closed = new AtomicBoolean();
    private ExecutorService threadPool;

    private ThreadGroup threadGroup;

    public LogExtAsyncHandler(JocHistoryConfiguration conf, String jocVariableName) {
        HistoryService.setLogger();
        this.conf = conf;
        this.jocVariableName = jocVariableName;
        this.folderPermissions = new SOSAuthFolderPermissions();
    }

    public void start(ThreadGroup threadGroup) {
        closed.set(false);

        this.threadGroup = threadGroup;
        deserialize();
    }

    public void close(StartupMode mode) {
        closed.set(true);

        if (threadPool != null) {
            HistoryService.setLogger();
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
        serialize();
    }

    public boolean isLogExt(LogExtType t, boolean failed) {
        if (t != null && isLogExtDirAvailable()) {
            switch (t) {
            case all:
                return true;
            case failed:
                return failed;
            case successful:
                return !failed;
            default:
                break;
            }
        }
        return false;
    }

    private boolean isLogExtDirAvailable() {
        boolean r = conf.isLogExtDirAvailable();
        if (!r) {
            conf.checkLogExtDirAvailable();
            r = conf.isLogExtDirAvailable();
            if (r) {
                LOGGER.info(String.format("[%s][isLogExtDirAvailable][%s]exists=%s,isWritable=%s", jocVariableName, conf.getLogExtDir(), conf
                        .isLogExtDirExists(), conf.isLogExtDirWritable()));
            }
        }
        return r;
    }

    public void add(com.sos.joc.history.helper.LogExt.Type type, Path file, Long logId, String workflowName, String orderId, Long orderHistoryId,
            Long taskHistoryId, String jobLabel) {
        logs.add(new LogExt(type, file, logId, workflowName, orderId, orderHistoryId, taskHistoryId, jobLabel));
    }

    public void process() {
        if (isLogExtDirAvailable()) {
            List<LogExt> copy = new ArrayList<>(logs);
            logs.clear();

            if (notProcessed.size() > 0) {
                copy.addAll(notProcessed);
                notProcessed.clear();
            }
            if (copy.size() == 0) {
                return;
            }
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    HistoryService.setLogger();
                    processLogs(copy);
                }

            };
            if (threadPool == null) {
                threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(threadGroup, jocVariableName + "_"
                        + THREAD_POOL_SUFFIX_MAIN));
            }
            threadPool.submit(task);
        } else {
            logs.clear();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][process][skip][%s]exists=%s,isWritable=%s", jocVariableName, conf.getLogExtDir(), conf
                        .isLogExtDirExists(), conf.isLogExtDirWritable()));
            }
        }
    }

    private void processLogs(List<LogExt> copy) {
        List<Supplier<LogExt>> tasks = new ArrayList<>();
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        x: for (LogExt l : copy) {
            if (closed.get()) {
                break x;
            }
            Supplier<LogExt> task = new Supplier<LogExt>() {

                @Override
                public LogExt get() {
                    HistoryService.setLogger();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[processLogs][start]%s", SOSString.toString(l)));
                    }
                    try {
                        if (l.isTask()) {
                            processTaskLog(l);
                        } else {
                            processOrderLog(l);
                        }
                        l.setProcessed();
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s][processLogs][%s]%s", jocVariableName, SOSString.toString(l), e.toString()), e);
                    }
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[processLogs][end]%s", SOSString.toString(l)));
                    }
                    return l;
                }
            };
            tasks.add(task);
        }

        int tz = tasks.size();
        if (tz > 0) {
            ExecutorService es = Executors.newFixedThreadPool(tz, new JocClusterThreadFactory(threadGroup, jocVariableName + "_"
                    + THREAD_POOL_SUFFIX_HANDLE));
            List<CompletableFuture<LogExt>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                    .toList());
            CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
            JocCluster.shutdownThreadPool(StartupMode.automatic, es, 3, false);

            copy.stream().forEach(l -> {
                if (l.isProcessed()) {
                    if (!l.isTask() && !l.isDeserialized()) {
                        deleteLogOrderDirectory(l.getFile(), l.getOrderId());
                    }
                } else {
                    l.setDeserialized();
                    notProcessed.add(l);
                }
            });
        }
    }

    private void processTaskLog(LogExt l) {
        Path t = conf.getLogExtDir().resolve(getTaskLogName(l));
        if (l.isDeserialized()) {
            LogTaskContent lc = new LogTaskContent(l.getTaskHistoryId(), folderPermissions);
            try {
                lc.toFile(t);
            } catch (Throwable e) {
                LOGGER.warn(String.format("[processTaskLog][%s]%s", t, e.toString()), e);
            }
        } else {
            try {
                SOSPath.copyFile(l.getFile(), t);
            } catch (Throwable e) {
                LOGGER.warn(String.format("[processTaskLog][%s][%s]%s", l.getFile(), t, e.toString()), e);
            } finally {
                deleteLogFile(l.getFile());
            }
        }
    }

    private void processOrderLog(LogExt l) {
        switch (l.getType()) {
        case order_all:
            handleOrderHistory(l);
            handleOrderLog(l);
            break;
        case order_history:
            handleOrderHistory(l);
            break;
        case order:
            handleOrderLog(l);
            break;
        default:
            break;
        }
    }

    private void handleOrderHistory(LogExt l) {
        Path t = conf.getLogExtDir().resolve(getOrderLogName(l, LogExt.Type.order_history));
        try {
            if (l.isDeserialized()) {
                DBItemHistoryLog item = getOrderLogHistory(l.getLogId());
                if (item != null && !item.fileContentIsNull()) {
                    try (OutputStream out = Files.newOutputStream(l.getFile(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        InputStream is = new ByteArrayInputStream(item.getFileContent());
                        IOUtils.copy(is, out);
                        try {
                            is.close();
                        } catch (Throwable e) {
                        }
                    }
                }
            } else {
                SOSPath.copyFile(l.getFile(), t);
            }
        } catch (IOException e) {
            LOGGER.warn(String.format("[handleOrderHistory][%s]%s", t, e.toString()), e);
        }
    }

    private void handleOrderLog(LogExt l) {
        Path t = conf.getLogExtDir().resolve(getOrderLogName(l, LogExt.Type.order));
        try {
            LogOrderContent lc = new LogOrderContent(l.getOrderHistoryId(), folderPermissions);
            lc.toFile(t);
        } catch (Throwable e) {
            LOGGER.warn(String.format("[handleOrderLog][%s]%s", t, e.toString()), e);
        }
    }

    private String getOrderLogName(LogExt l, Type type) {
        Type lt = type == null ? l.getType() : type;

        StringBuilder sb = new StringBuilder();
        sb.append("order");
        sb.append(".").append(l.getWorkflowName());
        sb.append(".").append(l.getOrderId());
        sb.append(".");
        sb.append(LogExt.isOrderHistory(lt) ? "json" : "log");
        return HistoryUtil.normalizeFileName(sb.toString());
    }

    private String getTaskLogName(LogExt l) {
        StringBuilder sb = new StringBuilder();
        sb.append("task");
        sb.append(".").append(l.getWorkflowName());
        sb.append(".").append(l.getOrderId());
        sb.append(".").append(l.getJobLabel());
        sb.append(".").append(l.getTaskHistoryId());
        sb.append(".log");
        return HistoryUtil.normalizeFileName(sb.toString());
    }

    private void deleteLogFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            LOGGER.warn(String.format("[%s][error on delete log file][%s]%s", jocVariableName, file, e.toString()), e);
        }
    }

    private void deleteLogOrderDirectory(Path file, String orderId) {
        try {
            SOSPath.deleteIfExists(file.getParent());
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s][error on delete order directory][%s]%s", jocVariableName, orderId, file.getParent(), e.toString()),
                    e);
        }
    }

    private void serialize() {
        if (!isLogExtDirAvailable()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][serialize][skip][%s]exists=%s,isWritable=%s", jocVariableName, conf.getLogExtDir(), conf
                        .isLogExtDirExists(), conf.isLogExtDirWritable()));
            }
            return;
        }

        int s = notProcessed.size();
        if (s > 0) {
            try {
                saveJocVariable(new SOSSerializer<CopyOnWriteArraySet<LogExt>>().serializeCompressed2bytes(notProcessed));
                LOGGER.info(String.format("[%s][serialized]logs=%s", jocVariableName, s));
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][serialize]%s", jocVariableName, e.toString()), e);
            }
            notProcessed.clear();
        } else {
            try {
                deleteJocVariable(getJocVariable());
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][deleteJocVariable]%s", jocVariableName, e.toString()), e);
            }
        }
    }

    private void deserialize() {
        if (!isLogExtDirAvailable()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][deserialize][skip][%s]exists=%s,isWritable=%s", jocVariableName, conf.getLogExtDir(), conf
                        .isLogExtDirExists(), conf.isLogExtDirWritable()));
            }
            return;
        }

        DBItemJocVariable item = null;
        try {
            item = getJocVariable();
            if (item == null) {
                return;
            }
            deserialize(item);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][deserialize]%s", jocVariableName, e.toString()), e);
        } finally {
            deleteJocVariable(item);
        }
    }

    private void deserialize(DBItemJocVariable item) throws Exception {
        CopyOnWriteArraySet<LogExt> sr = new SOSSerializer<CopyOnWriteArraySet<LogExt>>().deserializeCompressed(item.getBinaryValue());
        if (sr != null && sr.size() > 0) {
            notProcessed.addAll(sr.stream().map(e -> {
                e.setDeserialized();
                return e;
            }).collect(Collectors.toList()));

            LOGGER.info(String.format("[%s][deserialized]logs=%s", jocVariableName, sr.size()));
        }
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

    private void deleteJocVariable(DBItemJocVariable item) {
        if (item == null) {
            return;
        }
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(jocVariableName);
            session.beginTransaction();
            session.delete(item);
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

    private DBItemHistoryLog getOrderLogHistory(Long logId) {
        if (logId == null) {
            return null;
        }
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(jocVariableName);
            return session.get(DBItemHistoryLog.class, logId);
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
