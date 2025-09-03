package com.sos.joc.history.controller.yade;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.yade.history.YADETransferHistoryTerminated;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

public class YADEHandler implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEHandler.class);
    private static final String IDENTIFIER = ClusterServices.history.name();
    private static final String IDENTIFIER_YADE = IDENTIFIER + "-yade";

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final String controllerId;
    private final String jocVariableName;

    private AtomicBoolean closed = new AtomicBoolean(false);
    // lazy initialization - initialized only if YADE is used in the current JS7 environment
    // queue holding entries to process; BlockingQueue.take() blocks efficiently when empty
    private BlockingQueue<YADEHandlerEntry> queue;
    // dispatcher thread processes entries serially in background
    private Thread dispatcherThread;
    private ConcurrentHashMap<String, ProtocolEntry> protocols;

    private ThreadGroup threadGroup;

    public YADEHandler(String controllerId, String jocVariableName) {
        this.controllerId = controllerId;
        this.jocVariableName = jocVariableName;
    }

    public void start(ThreadGroup threadGroup) {
        this.closed.set(false);

        this.threadGroup = threadGroup;
        deserializeQueue();
    }

    // logging: IDENTIFIER instead of contollerId - due to the same format as the history service when starting/closing
    @Override
    public void close() {
        closed.set(true);

        if (!initialized.get()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info(String.format("[%s][%s][%s][close][skip]no initialized", IDENTIFIER, controllerId, IDENTIFIER_YADE));
            }
            return;
        }

        if (dispatcherThread != null) {
            dispatcherThread.interrupt(); // wake up take()
            try {
                dispatcherThread.join(); // wait for clean termination
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        serializeQueue();
    }

    public void add(YADEHandlerEntry entry) {
        initIfNeeded();

        queue.add(entry);
    }

    public void clearCache(long currentSeconds, long maxAgeSeconds) {
        if (!initialized.get()) {
            return;
        }
        // synchronized (protocols) {
        // protocols.entrySet().removeIf(e -> currentSeconds - e.getValue().timestampSeconds > maxAgeSeconds);
        // }
        protocols.entrySet().removeIf(e -> currentSeconds - e.getValue().timestampSeconds > maxAgeSeconds);
    }

    public int getProtocolsSize() {
        if (!initialized.get()) {
            return 0;
        }
        return protocols.size();
    }

    private void initIfNeeded() {
        if (initialized.compareAndSet(false, true)) {
            protocols = new ConcurrentHashMap<String, ProtocolEntry>();
            queue = new LinkedBlockingQueue<>();

            dispatcherThread = new Thread(this.threadGroup, this::dispatchLoop, IDENTIFIER_YADE);
            dispatcherThread.setDaemon(true); // does not block JVM shutdown
            dispatcherThread.start();
        }
    }

    /** Dispatcher loop: processes entries continuously.<br/>
     * Uses BlockingQueue.take() to block efficiently when the queue is empty.<br/>
     * - This avoids any CPU busy-wait and minimizes memory overhead.<br/>
     * - Note: take() blocks until an entry is available; element is removed from the queue. */
    private void dispatchLoop() {
        while (!closed.get()) {
            try {
                insertEntry(queue.take());
            } catch (InterruptedException e) {
                // if "close" requested, exit loop cleanly
                if (closed.get()) {
                    break;
                }
                // otherwise, restore interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    // logging: IDENTIFIER instead of contollerId - due to the same format as the history service when starting/closing
    private void serializeQueue() {
        int size = queue.size();
        if (size > 0) {
            try {
                saveJocVariable(new SOSSerializer<ArrayList<YADEHandlerEntry>>().serializeCompressed2bytes(new ArrayList<>(queue)));
                LOGGER.info(String.format("[%s][%s][%s][close][%s][serialized]entries=%s", IDENTIFIER, controllerId, IDENTIFIER_YADE, jocVariableName,
                        size));
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][%s][%s][close][%s][serializeQueue]%s", IDENTIFIER, controllerId, IDENTIFIER_YADE, jocVariableName, e
                        .toString()), e);
            }
            queue.clear();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s][close][%s][serializeQueue][skip]no entries found", IDENTIFIER, controllerId, IDENTIFIER_YADE,
                        jocVariableName));
            }
        }
    }

    // logging: IDENTIFIER instead of contollerId - due to the same format as the history service when starting/closing
    private void deserializeQueue() {
        DBItemJocVariable item = null;
        try {
            item = getJocVariable();
            if (item == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][%s][%s][start][%s][deserializeQueue][skip]because compressed data not found", IDENTIFIER,
                            controllerId, IDENTIFIER_YADE, jocVariableName, jocVariableName));
                }
                return;
            }
            deserializeQueue(item);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s][start][%s][deserializeQueue]%s", IDENTIFIER, controllerId, IDENTIFIER_YADE, jocVariableName, e
                    .toString()), e);
        } finally {
            deleteJocVariable(item);
        }
    }

    private void deserializeQueue(DBItemJocVariable item) throws Exception {
        List<YADEHandlerEntry> sr = new SOSSerializer<ArrayList<YADEHandlerEntry>>().deserializeCompressed(item.getBinaryValue());
        if (SOSCollection.isEmpty(sr)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info(String.format("[%s][%s][%s][start][%s][deserializeQueue][skip]no entries found", IDENTIFIER, controllerId,
                        IDENTIFIER_YADE, jocVariableName));
            }
        } else {
            LOGGER.info(String.format("[%s][%s][%s][start][%s][deserialized]entries=%s", IDENTIFIER, controllerId, IDENTIFIER_YADE, jocVariableName,
                    sr.size()));
            for (YADEHandlerEntry entry : sr) {
                add(entry);
            }
        }
    }

    private YadeTransferResult deserializeEntry(YADEHandlerEntry entry) {
        YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<YadeTransferResult>();

        try {
            return serializer.deserialize(entry.getData());
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][%s][workflow=%s][orderId=%s][job name=%s, pos=%s][cannot deserialize]%s", controllerId, IDENTIFIER_YADE,
                    entry.getWorkflowPath(), entry.getOrderId(), entry.getStepJobName(), entry.getStepWorkflowPosition(), e.toString()), e);
            return null;
        }
    }

    // insertEntry is thread-local per entry, safe for parallel execution
    // because all variables inside insertEntry (session, result, transferId) are local to the thread,
    // each thread works on its own copy of the entry, and DB sessions are independent
    private void insertEntry(YADEHandlerEntry entry) {
        if (entry == null) {
            return;
        }
        JocClusterServiceLogger.setLogger(IDENTIFIER);
        YadeTransferResult result = deserializeEntry(entry);
        if (result == null) {
            return;
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(controllerId + "][" + IDENTIFIER_YADE);
            session.beginTransaction();

            Long transferId = insertTransfer(session, result, entry);
            insertTransferEntries(session, transferId, result.getEntries());

            session.commit(); // has no effect due to JOC autocommit=true
            session.close();
            session = null;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][stored transferId=%s][workflow=%s][orderId=%s][job name=%s, pos=%s, historyId=%s]", controllerId,
                        IDENTIFIER_YADE, transferId, entry.getWorkflowPath(), entry.getOrderId(), entry.getStepJobName(), entry
                                .getStepWorkflowPosition(), entry.getStepHistoryId()));
            }
            postEventTransferHistoryTerminated(transferId);
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (Throwable ex) {
                }
            }
            String logMsg = String.format("%s][%s][workflow=%s][orderId=%s][job name=%s, pos=%s, historyId=%s", controllerId, IDENTIFIER_YADE, entry
                    .getWorkflowPath(), entry.getOrderId(), entry.getStepJobName(), entry.getStepWorkflowPosition(), entry.getStepHistoryId());
            LOGGER.error(String.format("[%s]%s", logMsg, e.toString()), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private Long insertTransfer(SOSHibernateSession session, YadeTransferResult result, YADEHandlerEntry entry) throws SOSHibernateException {
        DBItemYadeTransfer item = new DBItemYadeTransfer();
        item.setControllerId(controllerId);
        item.setWorkflowPath(entry.getWorkflowPath());
        item.setWorkflowName(JocClusterUtil.getBasenameFromPath(entry.getWorkflowPath()));
        item.setOrderId(entry.getOrderId());
        item.setJob(entry.getStepJobName());
        item.setJobPosition(entry.getStepWorkflowPosition());
        item.setHistoryOrderStepId(entry.getStepHistoryId());
        item.setSourceProtocolId(getProtocolId(session, result.getSource()));
        item.setTargetProtocolId(getProtocolId(session, result.getTarget()));
        item.setJumpProtocolId(getProtocolId(session, result.getJump()));
        item.setOperation(getOperation(result.getOperation()));
        item.setProfileName(result.getProfile());
        item.setStart(Date.from(result.getStart()));
        item.setEnd(Date.from(result.getEnd()));
        item.setNumOfFiles(result.getEntries() == null ? 0L : result.getEntries().size());
        item.setState(SOSString.isEmpty(result.getErrorMessage()) ? Yade.TransferState.SUCCESSFUL.intValue() : Yade.TransferState.FAILED.intValue());
        item.setErrorMessage(result.getErrorMessage());
        item.setCreated(new Date());

        // no error handling - if inserting metadata fails - inserting transfer "file" entries is not possible anyway
        session.save(item);
        return item.getId();
    }

    private void insertTransferEntries(SOSHibernateSession session, Long transferId, List<YadeTransferResultEntry> entries)
            throws SOSHibernateException {
        if (entries == null || entries.size() == 0) {
            return;
        }

        for (YadeTransferResultEntry entry : entries) {
            DBItemYadeFile item = new DBItemYadeFile();
            item.setTransferId(transferId);
            item.setSourcePath(entry.getSource());
            item.setTargetPath(entry.getTarget());
            item.setSize(entry.getSize());
            item.setModificationDate(getUTCFromTimestamp(entry.getModificationDate()));
            item.setState(Yade.TransferEntryState.fromValue(entry.getState()).intValue());
            item.setIntegrityHash(entry.getIntegrityHash());
            item.setErrorMessage(entry.getErrorMessage());
            item.setCreated(new Date());

            // error handling
            // - the error can occur, for example, with entry <n>.
            // -- this means that all previous entries have already been inserted (due to JOC autocommit=true).
            // -- in this case, the inserted history entries do not match the files YADE transferred
            // -- so why not insert all entries that can be inserted..?
            try {
                session.save(item);
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][%s][%s][insertTransferEntries][transferId=%s][save][%s]%s", IDENTIFIER, controllerId,
                        IDENTIFIER_YADE, transferId, SOSHibernate.toString(item), e.toString()), e);
            }
        }
    }

    private Long getProtocolId(SOSHibernateSession session, YadeTransferResultProtocol protocol) {
        if (protocol == null) {
            return null;
        }

        Integer protocolIntVal = Yade.TransferProtocol.fromValue(protocol.getProtocol()).intValue();
        String key = new StringBuilder(protocol.getHost()).append(protocol.getPort()).append(protocolIntVal).append(protocol.getAccount()).toString();

        ProtocolEntry protocolEntry = protocols.get(key);
        if (protocolEntry != null) {
            protocolEntry.refresh();
            return protocolEntry.id;
        }

        Long id = 0L;
        boolean run = true;
        int count = 0;
        while (run) {
            count = count + 1;
            try {
                id = getProtocolId(session, protocol.getHost(), protocol.getPort(), protocolIntVal, protocol.getAccount());
                if (id == null) {
                    DBItemYadeProtocol item = new DBItemYadeProtocol();
                    item.setHostname(protocol.getHost());
                    item.setPort(protocol.getPort());
                    item.setProtocol(protocolIntVal);
                    item.setAccount(protocol.getAccount());
                    item.setCreated(new Date());

                    session.save(item);
                    id = item.getId();
                }
                protocols.put(key, new ProtocolEntry(id));
                return id;
            } catch (SOSHibernateException e) {
                if (count >= 3) {
                    run = false;
                } else {
                    try {
                        Thread.sleep(2 * 1_000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
        return id;
    }

    private Long getProtocolId(SOSHibernateSession session, String hostname, Integer port, Integer protocol, String account)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id ");
        hql.append("from ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" ");
        hql.append("where hostname=:hostname ");
        hql.append("and port=:port ");
        hql.append("and protocol=:protocol ");
        hql.append("and account=:account");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("hostname", hostname);
        query.setParameter("port", port);
        query.setParameter("protocol", protocol);
        query.setParameter("account", account);

        List<Long> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private DBItemJocVariable getJocVariable() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(controllerId + "][" + IDENTIFIER_YADE + "][" + jocVariableName);
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
            session = Globals.createSosHibernateStatelessConnection(controllerId + "][" + IDENTIFIER_YADE + "][" + jocVariableName);
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
            session = Globals.createSosHibernateStatelessConnection(controllerId + "][" + IDENTIFIER_YADE + "][" + jocVariableName);
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

    // TODO check this method
    private Date getUTCFromTimestamp(long timestamp) {
        if (timestamp < 0L) {
            return null;
        }
        return new Date(timestamp - TimeZone.getDefault().getOffset(timestamp));
    }

    private void postEventTransferHistoryTerminated(Long transferId) {
        if (transferId == null) {
            return;
        }
        EventBus.getInstance().post(new YADETransferHistoryTerminated(controllerId, transferId));
    }

    private Integer getOperation(String val) {
        if (val != null && val.equalsIgnoreCase("delete")) {// Workaround
            return Yade.TransferOperation.REMOVE.intValue();
        }
        return Yade.TransferOperation.fromValue(val).intValue();
    }

    private static class ProtocolEntry {

        final Long id;
        long timestampSeconds;

        ProtocolEntry(Long id) {
            this.id = id;
            this.timestampSeconds = SOSDate.getSeconds(new Date());
        }

        void refresh() {
            this.timestampSeconds = SOSDate.getSeconds(new Date());
        }
    }

}
