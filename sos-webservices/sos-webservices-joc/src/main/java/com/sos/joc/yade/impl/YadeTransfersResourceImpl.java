package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.db.yade.JocYadeFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.yade.Operation;
import com.sos.joc.model.yade.Protocol;
import com.sos.joc.model.yade.ProtocolFragment;
import com.sos.joc.model.yade.Transfer;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.model.yade.TransferState;
import com.sos.joc.model.yade.TransferStateText;
import com.sos.joc.model.yade.Transfers;
import com.sos.joc.yade.resource.IYadeTransfersResource;
import com.sos.schema.JsonValidator;
import com.sos.yade.commons.Yade;

@Path("yade")
public class YadeTransfersResourceImpl extends JOCResourceImpl implements IYadeTransfersResource {

    private static final String IMPL_PATH = "./yade/transfers";

    @Override
    public JOCDefaultResponse postYadeTransfers(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, TransferFilter.class);
            TransferFilter in = Globals.objectMapper.readValue(inBytes, TransferFilter.class);
            
            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getView()).collect(
                                Collectors.toSet());
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
            

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);

            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            } else if (limit == -1) {
                limit = null; // unlimited
            }

            JocYadeFilter filter = new JocYadeFilter();
            filter.setControllerIds(allowedControllers);
            filter.setTransferIds(in.getTransferIds());
            filter.setOperations(in.getOperations());
            filter.setStates(in.getStates());
            filter.setSources(in.getSources());
            filter.setTargets(in.getTargets());
            filter.setProfiles(in.getProfiles());
            filter.setLimit(limit);
            filter.setDateFrom(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()));
            filter.setDateTo(JobSchedulerDate.getDateTo(in.getDateTo(), in.getTimeZone()));

            Transfers entity = new Transfers();
            List<Transfer> transfers = new ArrayList<Transfer>();
            List<Long> filteredTransferIds = null;

            
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<DBItemYadeTransfer> items = dbLayer.getFilteredTransfers(filter);
            if (items != null && !items.isEmpty()) {
                boolean withSourceFiles = in.getSourceFiles() != null && !in.getSourceFiles().isEmpty();
                boolean withTargetFiles = in.getTargetFiles() != null && !in.getTargetFiles().isEmpty();
                boolean withSourceFilePattern = in.getSourceFile() != null && !in.getSourceFile().isEmpty();
                boolean withTargetFilePattern = in.getTargetFile() != null && !in.getTargetFile().isEmpty();
                boolean withSourceTargetFilter = withSourceFiles || withTargetFiles || withSourceFilePattern || withTargetFilePattern;

                if (withSourceTargetFilter) {
                    filteredTransferIds = items.stream().map(DBItemYadeTransfer::getId).distinct().collect(Collectors.toList());
                }
                if ((withSourceFiles || withTargetFiles) && filteredTransferIds != null && !filteredTransferIds.isEmpty()) {
                    filteredTransferIds = dbLayer.transferIdsFilteredBySourceTargetPath(filteredTransferIds, in.getSourceFiles(), in.getTargetFiles(),
                            in.getSourceFile(), in.getTargetFile());
                }
                if (filteredTransferIds == null) {
                    filteredTransferIds = Collections.emptyList();
                }
                boolean compact = in.getCompact() == Boolean.TRUE;
                for (DBItemYadeTransfer item : items) {
                    if (withSourceTargetFilter && !filteredTransferIds.contains(item.getId())) {
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

    private ProtocolFragment getProtocolFragment(JocDBLayerYade dbLayer, Long id) {
        if (id != null) {
            DBItemYadeProtocol protocol = dbLayer.getProtocolById(id);
            if (protocol != null) {
                ProtocolFragment pf = new ProtocolFragment();
                pf.setAccount(protocol.getAccount());
                pf.setHost(protocol.getHostname());
                pf.setPort(protocol.getPort());
                pf.setProtocol(Protocol.fromValue(Yade.TransferProtocol.fromValue(protocol.getProtocol()).name()));
                return pf;
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
