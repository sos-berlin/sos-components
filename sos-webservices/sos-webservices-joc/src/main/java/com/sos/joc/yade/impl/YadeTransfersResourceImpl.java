package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.filters.FilterAfterResponse;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.db.yade.JocYadeFilter;
import com.sos.joc.db.yade.YadeSourceTargetFiles;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.yade.FileFilter;
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
            JsonValidator.validate(inBytes, FileFilter.class);
            TransferFilter in = Globals.objectMapper.readValue(inBytes, TransferFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            in.setSourceFilesRegex(SearchStringHelper.getRegexValue(in.getSourceFilesRegex()));
            in.setTargetFilesRegex(SearchStringHelper.getRegexValue(in.getTargetFilesRegex()));

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);

            Date dateFrom = null;
            Date dateTo = null;
            String from = in.getDateFrom();
            String to = in.getDateTo();
            String timezone = in.getTimeZone();
            Boolean compact = in.getCompact();
            if (from != null && !from.isEmpty()) {
                dateFrom = JobSchedulerDate.getDateFrom(from, timezone);
            }
            if (to != null && !to.isEmpty()) {
                dateTo = JobSchedulerDate.getDateTo(to, timezone);
            }
            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            } else if (limit == -1) {
                limit = null; // unlimited
            }

            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<TransferStateText> states = in.getStates();
            Set<Integer> stateValues = new HashSet<Integer>();
            for (TransferStateText state : states) {
                switch (state) {
                case SUCCESSFUL:
                    stateValues.add(Yade.TransferState.SUCCESSFUL.intValue());
                    break;
                case INCOMPLETE:
                    stateValues.add(Yade.TransferState.INCOMPLETE.intValue());
                    break;
                case FAILED:
                    stateValues.add(Yade.TransferState.FAILED.intValue());
                    break;
                }
            }
            // TODO source and target are wrong
            // If works now only if the array has only one item
            Set<String> sourceHosts = null;
            Set<Integer> sourceProtocols = null;
            if (in.getSources() != null && !in.getSources().isEmpty()) {
                sourceHosts = new HashSet<String>();
                sourceProtocols = new HashSet<Integer>();
                for (ProtocolFragment source : in.getSources()) {
                    if (source.getHost() != null && !source.getHost().isEmpty()) {
                        sourceHosts.add(source.getHost());
                    }
                    if (source.getProtocol() != null) {
                        sourceProtocols.add(getProtocol(source.getProtocol()));
                    }
                }
            }
            Set<String> targetHosts = null;
            Set<Integer> targetProtocols = null;
            if (in.getTargets() != null && !in.getTargets().isEmpty()) {
                targetHosts = new HashSet<String>();
                targetProtocols = new HashSet<Integer>();
                for (ProtocolFragment target : in.getTargets()) {
                    if (target.getHost() != null && !target.getHost().isEmpty()) {
                        targetHosts.add(target.getHost());
                    }
                    if (target.getProtocol() != null) {
                        targetProtocols.add(getProtocol(target.getProtocol()));
                    }
                }
            }
            Set<Integer> operationValues = null;
            if (in.getOperations() != null && !in.getOperations().isEmpty()) {
                operationValues = new HashSet<Integer>();
                for (Operation operation : in.getOperations()) {
                    switch (operation) {
                    case COPY:
                        operationValues.add(Yade.TransferOperation.COPY.intValue());
                        break;
                    case MOVE:
                        operationValues.add(Yade.TransferOperation.MOVE.intValue());
                        break;
                    case GETLIST:
                        operationValues.add(Yade.TransferOperation.GETLIST.intValue());
                        break;
                    case RENAME:
                        operationValues.add(Yade.TransferOperation.RENAME.intValue());
                        break;
                    case COPYTOINTERNET:
                        operationValues.add(Yade.TransferOperation.COPYTOINTERNET.intValue());
                        break;
                    case COPYFROMINTERNET:
                        operationValues.add(Yade.TransferOperation.COPYFROMINTERNET.intValue());
                        break;
                    default:
                        operationValues.add(Yade.TransferOperation.UNKNOWN.intValue());
                        break;
                    }
                }
            }
            List<String> sourceFiles = in.getSourceFiles();
            List<String> targetFiles = in.getTargetFiles();

            JocYadeFilter filter = new JocYadeFilter();
            filter.setControllerId(in.getControllerId());
            filter.setTransferIds(in.getTransferIds());
            filter.setOperations(operationValues);
            filter.setStates(stateValues);
            // filter.setMandator(in.getMandator());
            filter.setSourceHosts(sourceHosts);
            filter.setSourceProtocols(sourceProtocols);
            filter.setTargetHosts(targetHosts);
            filter.setTargetProtocols(targetProtocols);
            // filter.setIsIntervention(in.getIsIntervention());
            // filter.setHasInterventions(in.getHasIntervention());
            filter.setProfiles(in.getProfiles());
            filter.setLimit(limit);
            filter.setDateFrom(dateFrom);
            filter.setDateTo(dateTo);

            Transfers entity = new Transfers();
            List<Transfer> transfers = new ArrayList<Transfer>();
            List<Long> filteredTransferIds = null;
            List<YadeSourceTargetFiles> yadeFiles = new ArrayList<YadeSourceTargetFiles>();
            Pattern sourceFilesPattern = null;
            Pattern targetFilesPattern = null;

            List<DBItemYadeTransfer> items = dbLayer.getFilteredTransfers(filter);
            if (items != null && !items.isEmpty()) {
                boolean withSourceFiles = (sourceFiles != null && !sourceFiles.isEmpty());
                boolean withTargetFiles = (targetFiles != null && !targetFiles.isEmpty());
                boolean withSourceFilesRegex = (!withSourceFiles && in.getSourceFilesRegex() != null && !in.getSourceFilesRegex().isEmpty());
                boolean withTargetFilesRegex = (!withTargetFiles && in.getTargetFilesRegex() != null && !in.getTargetFilesRegex().isEmpty());
                boolean withSourceTargetFilter = (withSourceFiles || withTargetFiles || withSourceFilesRegex || withTargetFilesRegex);

                if (withSourceTargetFilter) {
                    filteredTransferIds = dbLayer.getFilteredTransferIds(filter);
                }
                if ((withSourceFiles || withTargetFiles) && filteredTransferIds != null && !filteredTransferIds.isEmpty()) {
                    filteredTransferIds = dbLayer.transferIdsFilteredBySourceTargetPath(filteredTransferIds, sourceFiles, targetFiles);
                }
                if (withSourceFilesRegex && filteredTransferIds != null && !filteredTransferIds.isEmpty()) {
                    sourceFilesPattern = Pattern.compile(in.getSourceFilesRegex());
                }
                if (withTargetFilesRegex && filteredTransferIds != null && !filteredTransferIds.isEmpty()) {
                    targetFilesPattern = Pattern.compile(in.getTargetFilesRegex());
                }
                if ((withSourceFilesRegex || withTargetFilesRegex) && filteredTransferIds != null && !filteredTransferIds.isEmpty()) {
                    yadeFiles = dbLayer.SourceTargetFilePaths(filteredTransferIds);
                    if (yadeFiles != null) {
                        Set<Long> transferIdSet = new HashSet<Long>();
                        for (YadeSourceTargetFiles f : yadeFiles) {
                            if (FilterAfterResponse.matchRegex(sourceFilesPattern, f.getSourcePath()) && FilterAfterResponse.matchRegex(
                                    targetFilesPattern, f.getTargetPath())) {
                                transferIdSet.add(f.getTransferId());
                            }
                        }
                        filteredTransferIds = new ArrayList<Long>(transferIdSet);
                    }
                }
                if (filteredTransferIds == null) {
                    filteredTransferIds = new ArrayList<Long>();
                }
                // Map<String, Set<Folder>> permittedFoldersMap = folderPermissions.getListOfFoldersForInstance();
                for (DBItemYadeTransfer item : items) {
                    if (withSourceTargetFilter && !filteredTransferIds.contains(item.getId())) {
                        continue;
                    }
                    // if (item.getWorkflowPath() != null && !item.getWorkflowPath().isEmpty()) {
                    // if (!canAdd(item.getWorkflowPath(), permittedFoldersMap.get(""))) {
                    // continue;
                    // }
                    // if (!canAdd(item.getWorkflowPath(), permittedFoldersMap.get(item.getWorkflowPath()))) {
                    // continue;
                    // }
                    // }
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

    private Transfer fillTransfer(JocDBLayerYade dbLayer, DBItemYadeTransfer item, Boolean compact) throws Exception {
        Transfer transfer = new Transfer();
        transfer.setId(item.getId());
        transfer.setControllerId(item.getControllerId());
        // transfer.setHasIntervention(item.getHasIntervention());
        // transfer.setMandator(item.getMandator());
        // transfer.setParent_id(item.getParentTransferId());
        transfer.setHistoryId(item.getHistoryOrderStepId());
        transfer.set_operation(getOperation(Yade.TransferOperation.fromValue(item.getOperation())));
        transfer.setState(getState(Yade.TransferState.fromValue(item.getState())));
        transfer.setProfile(item.getProfileName());
        transfer.setNumOfFiles(item.getNumOfFiles() == null ? null : item.getNumOfFiles().intValue());
        transfer.setStart(item.getStart());
        transfer.setEnd(item.getEnd());
        transfer.setSurveyDate(item.getCreated());
        transfer.setSource(getProtocolFragment(dbLayer, item.getSourceProtocolId()));
        transfer.setTarget(getProtocolFragment(dbLayer, item.getTargetProtocolId()));

        Err err = new Err();
        err.setMessage(item.getErrorMessage());
        transfer.setError(err);

        if (!compact) {
            // TODO consider folder perms
            transfer.setWorkflowPath(item.getWorkflowPath());
            transfer.setOrderId(item.getOrderId());
            transfer.setJob(item.getJob());
            transfer.setJobPosition(item.getJobPosition());
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
                pf.setProtocol(getProtocol(Yade.TransferProtocol.fromValue(protocol.getProtocol())));
                return pf;
            }
        }
        return null;
    }

    private Operation getOperation(Yade.TransferOperation op) {
        switch (op) {
        case COPY:
            return Operation.COPY;
        case MOVE:
            return Operation.MOVE;
        case GETLIST:
            return Operation.GETLIST;
        case RENAME:
            return Operation.RENAME;
        case COPYTOINTERNET:
            return Operation.COPYTOINTERNET;
        case COPYFROMINTERNET:
            return Operation.COPYFROMINTERNET;
        default:
            return null;
        }
    }

    private TransferState getState(Yade.TransferState value) {
        TransferState state = new TransferState();
        switch (value) {
        case SUCCESSFUL:
            state.setSeverity(OrdersHelper.getState(OrderStateText.FINISHED).getSeverity());
            state.set_text(TransferStateText.SUCCESSFUL);
            return state;
        case INCOMPLETE:
            state.setSeverity(OrdersHelper.getState(OrderStateText.INPROGRESS).getSeverity());
            state.set_text(TransferStateText.INCOMPLETE);
            return state;
        case FAILED:
            state.setSeverity(OrdersHelper.getState(OrderStateText.FAILED).getSeverity());
            state.set_text(TransferStateText.FAILED);
            return state;
        default:
            return null;
        }
    }

    private Protocol getProtocol(Yade.TransferProtocol value) {
        switch (value) {
        case LOCAL:
            return Protocol.LOCAL;
        case FTP:
            return Protocol.FTP;
        case FTPS:
            return Protocol.FTPS;
        case SFTP:
            return Protocol.SFTP;
        case HTTP:
            return Protocol.HTTP;
        case HTTPS:
            return Protocol.HTTPS;
        case WEBDAV:
            return Protocol.WEBDAV;
        case WEBDAVS:
            return Protocol.WEBDAVS;
        case SMB:
            return Protocol.SMB;
        default:
            return null;
        }
    }

    private Integer getProtocol(Protocol protocol) {
        switch (protocol) {
        case LOCAL:
            return Yade.TransferProtocol.LOCAL.intValue();
        case FTP:
            return Yade.TransferProtocol.FTP.intValue();
        case FTPS:
            return Yade.TransferProtocol.FTPS.intValue();
        case SFTP:
            return Yade.TransferProtocol.SFTP.intValue();
        case HTTP:
            return Yade.TransferProtocol.HTTP.intValue();
        case HTTPS:
            return Yade.TransferProtocol.HTTPS.intValue();
        case WEBDAV:
            return Yade.TransferProtocol.WEBDAV.intValue();
        case WEBDAVS:
            return Yade.TransferProtocol.WEBDAVS.intValue();
        case SMB:
            return Yade.TransferProtocol.SMB.intValue();
        default:
            return null;
        }
    }
}
