package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.db.yade.JocYadeFilter;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.yade.Operation;
import com.sos.joc.model.yade.Protocol;
import com.sos.joc.model.yade.ProtocolFragment;
import com.sos.joc.model.yade.Transfer;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.model.yade.TransferId;
import com.sos.joc.model.yade.TransferState;
import com.sos.joc.model.yade.TransferStateText;
import com.sos.joc.model.yade.Transfers;
import com.sos.joc.yade.resource.IYadeTransfersResource;
import com.sos.schema.JsonValidator;
import com.sos.yade.commons.Yade;

@Path("yade")
public class YadeTransfersResourceImpl extends JOCResourceImpl implements IYadeTransfersResource {

    private static final String IMPL_PATH_TRANSFERS = "./yade/transfers";
    private static final String IMPL_PATH_TRANSFER = "./yade/transfer";
    private Map<Long, ProtocolFragment> protocolFragments = new HashMap<>();
    
    @Override
    public JOCDefaultResponse postYadeTransfer(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_TRANSFER, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, TransferId.class);
            TransferId in = Globals.objectMapper.readValue(inBytes, TransferId.class);

            Set<String> allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                    availableController -> getControllerPermissions(availableController, accessToken).getView()).collect(Collectors.toSet());

            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRANSFER);
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            DBItemYadeTransfer item = dbLayer.getTransfer(in.getTransferId());
            if (item == null) {
                throw new DBMissingDataException(String.format("Transfer with id '%1$d' not found", in.getTransferId()));
            }

            if (!allowedControllers.contains(item.getControllerId())) {
                return accessDeniedResponse();
            }
            if (item.getWorkflowPath() != null && !item.getWorkflowPath().isEmpty()) {
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders(item.getControllerId());
                if (!canAdd(item.getWorkflowPath(), permittedFolders)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + item.getWorkflowPath().replaceFirst("/[^/]+$", "/"));
                }
            }

            return JOCDefaultResponse.responseStatus200(fillTransfer(dbLayer, item, in.getCompact()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public JOCDefaultResponse postYadeTransfers(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_TRANSFERS, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, TransferFilter.class);
            TransferFilter in = Globals.objectMapper.readValue(inBytes, TransferFilter.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getView()).collect(Collectors.toSet());
            } else if (getControllerPermissions(controllerId, accessToken).getView()) {
                allowedControllers = Collections.singleton(controllerId);
            }

            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            Map<String, Set<Folder>> permittedFoldersMap = folderPermissions.getListOfFolders(allowedControllers);
            if (controllerId.isEmpty() && allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRANSFERS);

            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            } else if (limit == -1) {
                limit = null; // unlimited
            }

            JocYadeFilter filter = new JocYadeFilter();
            filter.setControllerIds(allowedControllers);
            filter.setOperations(in.getOperations());
            filter.setStates(in.getStates());
            filter.setSources(in.getSources());
            filter.setTargets(in.getTargets());
            filter.setProfiles(in.getProfiles());
            filter.setWorkflowNames(in.getWorkflowNames());
            filter.setLimit(limit);
            filter.setDateFrom(JobSchedulerDate.getDateFrom(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), in.getTimeZone()));
            filter.setDateTo(JobSchedulerDate.getDateTo(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), in.getTimeZone()));

            Transfers entity = new Transfers();
            List<Transfer> transfers = new ArrayList<>();

            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<DBItemYadeTransfer> items = dbLayer.getFilteredTransfers(filter);
            if (items != null && !items.isEmpty()) {
                boolean withSourceFiles = in.getSourceFiles() != null && !in.getSourceFiles().isEmpty();
                boolean withTargetFiles = in.getTargetFiles() != null && !in.getTargetFiles().isEmpty();
                boolean withSourceTargetFilter = withSourceFiles || withTargetFiles;

                List<Long> filteredTransferIds = Collections.emptyList();
                if (withSourceTargetFilter) {
                    filteredTransferIds = dbLayer.transferIdsFilteredBySourceTargetPath(in.getSourceFiles(), in.getTargetFiles());
                    if (filteredTransferIds == null) {
                        filteredTransferIds = Collections.emptyList();
                    }
                }
                boolean compact = in.getCompact() == Boolean.TRUE;
                for (DBItemYadeTransfer item : items) {
                    if (withSourceTargetFilter && !filteredTransferIds.remove(item.getId())) {
                        continue;
                    }
                    if (item.getWorkflowPath() != null && !item.getWorkflowPath().isEmpty()) {
                        if (!canAdd(item.getWorkflowPath(), permittedFoldersMap.get(item.getControllerId()))) {
                            continue;
                        }
                    }
                    transfers.add(fillTransfer(dbLayer, item, compact));
                }
            }
            entity.setTransfers(transfers);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private Transfer fillTransfer(JocDBLayerYade dbLayer, DBItemYadeTransfer item, boolean compact) throws Exception {
        Transfer transfer = new Transfer();
        transfer.setId(item.getId());
        transfer.setControllerId(item.getControllerId());
        transfer.setHistoryId(item.getHistoryOrderStepId());
        transfer.set_operation(Operation.fromValue(Yade.TransferOperation.fromValue(item.getOperation()).name()));
        transfer.setState(getState(Yade.TransferState.fromValue(item.getState())));
        transfer.setProfile(item.getProfileName());
        transfer.setNumOfFiles(item.getNumOfFiles());
        transfer.setStart(item.getStart());
        transfer.setEnd(item.getEnd());
        transfer.setSurveyDate(item.getCreated());
        transfer.setWorkflowPath(item.getWorkflowPath());
        transfer.setOrderId(item.getOrderId());
        transfer.setJob(item.getJob());
        transfer.setJobPosition(item.getJobPosition());

        Err err = new Err();
        err.setMessage(item.getErrorMessage());
        transfer.setError(err);

        if (!compact) {
            transfer.setSource(getProtocolFragment(dbLayer, item.getSourceProtocolId()));
            transfer.setTarget(getProtocolFragment(dbLayer, item.getTargetProtocolId()));
            transfer.setJump(getProtocolFragment(dbLayer, item.getJumpProtocolId()));
        }
        return transfer;
    }

    private ProtocolFragment getProtocolFragment(JocDBLayerYade dbLayer, Long id) throws SOSHibernateException {
        if (id != null) {
            ProtocolFragment pf = protocolFragments.get(id);
            if (pf == null) {
                DBItemYadeProtocol protocol = dbLayer.getProtocolById(id);
                if (protocol != null) {
                    pf = new ProtocolFragment();
                    pf.setAccount(protocol.getAccount());
                    pf.setHost(protocol.getHostname());
                    pf.setPort(protocol.getPort());
                    pf.setProtocol(Protocol.fromValue(Yade.TransferProtocol.fromValue(protocol.getProtocol()).name()));
                    protocolFragments.put(id, pf);
                    return pf;
                }
            }
        }
        return null;
    }

    private TransferState getState(Yade.TransferState value) {
        TransferState state = new TransferState();
        switch (value) {
        case SUCCESSFUL:
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            state.set_text(TransferStateText.SUCCESSFUL);
            return state;
        case INCOMPLETE:
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.INPROGRESS));
            state.set_text(TransferStateText.INCOMPLETE);
            return state;
        case FAILED:
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FAILED));
            state.set_text(TransferStateText.FAILED);
            return state;
        default:
            return null;
        }
    }
}
