package com.sos.js7.history.controller.yade;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.yade.history.YadeTransferHistoryTerminated;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.history.helper.HistoryUtil;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

import js7.data.value.Value;

public class YadeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(YadeHandler.class);
    private static final String IDENTIFIER = ClusterServices.history.name();

    private final String controllerId;
    private final ConcurrentHashMap<String, Long> protocols;

    public YadeHandler(String controllerId) {
        this.controllerId = controllerId;
        this.protocols = new ConcurrentHashMap<String, Long>();
    }

    public void process(SOSHibernateFactory dbFactory, Value value, String workflowPath, String orderId, Long historyOrderStepId, String job,
            String jobPosition) {

        CompletableFuture<Long> save = CompletableFuture.supplyAsync(() -> {
            AJocClusterService.setLogger(IDENTIFIER);
            SOSHibernateSession session = null;
            String logMsg = String.format("%s][%s][%s][job name=%s,pos=%s,id=%s", controllerId, workflowPath, orderId, job, jobPosition,
                    historyOrderStepId);
            try {
                String serialized = value.convertToString();
                if (SOSString.isEmpty(serialized)) {
                    LOGGER.warn(String.format("[%s][%s]is empty", logMsg, Yade.JOB_ARGUMENT_NAME_RETURN_VALUES));
                    return null;
                }

                YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<YadeTransferResult>();
                YadeTransferResult result = serializer.deserialize(serialized);
                if (result == null) {
                    return null;
                }

                session = dbFactory.openStatelessSession("yade");
                session.beginTransaction();

                Long transferId = saveTransfer(session, result, workflowPath, orderId, historyOrderStepId, job, jobPosition);
                saveTransferEntries(session, transferId, result.getEntries());

                session.commit();
                session.close();
                session = null;
                return transferId;
            } catch (Throwable e) {
                if (session != null) {
                    try {
                        session.rollback();
                    } catch (Throwable ex) {
                    }
                }
                LOGGER.error(String.format("[%s]%s", logMsg, e.toString()), e);
                return null;
            } finally {
                if (session != null) {
                    session.close();
                }
                AJocClusterService.clearLogger();
            }
        });
        save.thenAccept(transferId -> {
            if (transferId != null) {
                AJocClusterService.setLogger(IDENTIFIER);
                LOGGER.debug("[stored]transferId=" + transferId);
                AJocClusterService.clearLogger();
                postEventTransferHistoryTerminated(transferId);
            }
        });
    }

    private Long saveTransfer(SOSHibernateSession session, YadeTransferResult result, String workflowPath, String orderId, Long historyOrderStepId,
            String job, String jobPosition) throws SOSHibernateException {
        DBItemYadeTransfer item = new DBItemYadeTransfer();
        item.setControllerId(controllerId);
        item.setWorkflowPath(workflowPath);
        item.setWorkflowName(HistoryUtil.getBasenameFromPath(workflowPath));
        item.setOrderId(orderId);
        item.setJob(job);
        item.setJobPosition(jobPosition);
        item.setHistoryOrderStepId(historyOrderStepId);
        item.setSourceProtocolId(getProtocolId(session, result.getSource()));
        item.setTargetProtocolId(getProtocolId(session, result.getTarget()));
        item.setJumpProtocolId(getProtocolId(session, result.getJump()));
        item.setOperation(Yade.TransferOperation.fromValue(result.getOperation()).intValue());
        item.setProfileName(result.getProfile());
        item.setStart(Date.from(result.getStart()));
        item.setEnd(Date.from(result.getEnd()));
        item.setNumOfFiles(result.getEntries() == null ? 0L : result.getEntries().size());
        item.setState(SOSString.isEmpty(result.getErrorMessage()) ? Yade.TransferState.SUCCESSFUL.intValue() : Yade.TransferState.FAILED.intValue());
        item.setErrorMessage(result.getErrorMessage());
        item.setCreated(new Date());

        session.save(item);
        return item.getId();
    }

    private void saveTransferEntries(SOSHibernateSession session, Long transferId, List<YadeTransferResultEntry> entries)
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

            session.save(item);
        }
    }

    private Long getProtocolId(SOSHibernateSession session, YadeTransferResultProtocol protocol) {
        if (protocol == null) {
            return null;
        }

        Integer protocolIntVal = Yade.TransferProtocol.fromValue(protocol.getProtocol()).intValue();
        String key = new StringBuilder(protocol.getHost()).append(protocol.getPort()).append(protocolIntVal).append(protocol.getAccount()).toString();

        // TODO deleted protocols handling ...
        if (protocols.containsKey(key)) {
            return protocols.get(key);
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
                protocols.put(key, id);
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

    public Long getProtocolId(SOSHibernateSession session, String hostname, Integer port, Integer protocol, String account)
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
        EventBus.getInstance().post(new YadeTransferHistoryTerminated(controllerId, transferId));
    }

}
