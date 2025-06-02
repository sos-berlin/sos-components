package com.sos.joc.approvals.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IModifyStateResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.FourEyesRequestIds;
import com.sos.joc.model.security.foureyes.RequestorState;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;

@Path("approvals")
public class ModifyStateImpl extends JOCResourceImpl implements IModifyStateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyStateImpl.class);

    private static final String API_CALL = "./approvals/";

    private enum Action {
        APPROVE, REJECT, WITHDRAW
    }

    @Override
    public JOCDefaultResponse postApprove(String accessToken, byte[] filterBytes) {
        return postApproverState(Action.APPROVE, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postReject(String accessToken, byte[] filterBytes) {
        return postApproverState(Action.REJECT, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postWithdraw(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JOCDefaultResponse response = init(Action.WITHDRAW, accessToken, filterBytes);
            if (response != null) {
                return response;
            }

            FourEyesRequestIds in = Globals.objectMapper.readValue(filterBytes, FourEyesRequestIds.class);
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL + Action.WITHDRAW.name().toLowerCase());
            session.setAutoCommit(false);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);

            final String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            List<DBItemJocApprovalRequest> items = dbLayer.getApprovalRequests(in.getIds());
            Map<Boolean, List<Either<Err419, DBItemJocApprovalRequest>>> result = items.stream().map(item -> {
                Either<Err419, DBItemJocApprovalRequest> either = null;
                try {
                    if (!item.getRequestor().equals(curAccountName)) {
                        throw new JocBadRequestException("The current user is not the requestor of the approval request with id " + item.getId());
                    }
                    if (RequestorState.EXECUTED.equals(item.getRequestorStateAsEnum())) {
                        throw new JocBadRequestException(String.format("The approval request '%s' is already %s", item.getTitle(), item.getRequestorStateAsEnum().value().toLowerCase()));
                    }
                    either = Either.right(item);
                    if (RequestorState.WITHDRAWN.equals(item.getRequestorStateAsEnum())) {
                        //throw new JocBadRequestException(String.format("The approval request '%s' is already %s", item.getTitle(), item.getRequestorStateAsEnum().value().toLowerCase()));
                        either = null;
                    }
                } catch (Exception ex) {
                    either = Either.left(new BulkError(LOGGER).get(ex, getJocError(), (String) null));
                }
                return either;
            }).filter(Objects::nonNull).collect(Collectors.groupingBy(Either::isRight));

            if (result.containsKey(Boolean.TRUE)) {
                List<Long> ids = result.get(Boolean.TRUE).stream().map(Either::get).map(DBItemJocApprovalRequest::getId).toList();
                dbLayer.updateRequestorStatusInclusiveTransaction(ids, RequestorState.WITHDRAWN);

                Map<String, Long> approversForEvents = new HashMap<>();
                result.get(Boolean.TRUE).stream().map(Either::get).filter(item -> item.getApproverState().equals(
                        ApproverState.PENDING.intValue())).map(DBItemJocApprovalRequest::getApprover).distinct().forEach(approver -> {
                            try {
                                long numOfPendingApprovals = dbLayer.getNumOfPendingApprovals(approver);
                                approversForEvents.put(approver, numOfPendingApprovals);
                                //return new ApprovalUpdatedEvent(approver, numOfPendingApprovals);
                            } catch (Exception e) {
                                //
                            }
                        });

                if (approversForEvents.isEmpty()) {
                    EventBus.getInstance().post(new ApprovalUpdatedEvent());
                } else {
                    EventBus.getInstance().post(new ApprovalUpdatedEvent(null, approversForEvents));
                }
            }

            if (result.containsKey(Boolean.FALSE)) {
                return JOCDefaultResponse.responseStatus419(result.get(Boolean.FALSE).stream().map(Either::getLeft).collect(Collectors.toList()));
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse postApproverState(Action action, String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JOCDefaultResponse response = init(action, accessToken, filterBytes);
            if (response != null) {
                return response;
            }
            if (!jobschedulerUser.getSOSAuthCurrentAccount().isApprover()) {
                throw new JocAccessDeniedException("The current user is not an approver.");
            }

            FourEyesRequestIds in = Globals.objectMapper.readValue(filterBytes, FourEyesRequestIds.class);
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL + action.name().toLowerCase());
            session.setAutoCommit(false);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);

            List<DBItemJocApprovalRequest> items = dbLayer.getApprovalRequests(in.getIds());
            final ApproverState newState = action.equals(Action.APPROVE) ? ApproverState.APPROVED : ApproverState.REJECTED;
            final String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            
            Map<Boolean, List<Either<Err419, DBItemJocApprovalRequest>>> result = items.stream().map(item -> {
                Either<Err419, DBItemJocApprovalRequest> either = null;
                try {
                    ApproverState prevApproverState = item.getApproverStateAsEnum();
                    RequestorState requestorState = item.getRequestorStateAsEnum();
                    
                    switch (requestorState) {
                    case EXECUTED:
                        throw new JocBadRequestException(String.format("The approval request '%s' has already been used by the requestor", item.getTitle()));
                    case WITHDRAWN:
                        //throw new JocBadRequestException(String.format("The approval request '%s' is already withdrawn by the requestor", item.getTitle()));
                    default: // REQUESTED
                        break;
                    }
                    either = Either.right(item);
                    if (newState.equals(prevApproverState)) {
//                        switch (prevApproverState) {
//                        case APPROVED:
//                            throw new JocBadRequestException(String.format("The approval request '%s' is already approved", item.getTitle()));
//                        case REJECTED:
//                            throw new JocBadRequestException(String.format("The approval request '%s' is already rejected", item.getTitle()));
//                        default: // PENDING never reached
//                            break;
//                        }
                        either = null;
                    }
                } catch (Exception ex) {
                    either = Either.left(new BulkError(LOGGER).get(ex, getJocError(), (String) null));
                }
                return either;
            }).filter(Objects::nonNull).collect(Collectors.groupingBy(Either::isRight));
            
            if (result.containsKey(Boolean.TRUE)) {
                Set<Long> ids = new HashSet<>();
                Set<Long> takeOverIds = new HashSet<>();
                Map<String, Long> approversForEvents = new HashMap<>();
                Map<String, Map<String, Long>> requestorsForEvents = new HashMap<>();
                
                result.get(Boolean.TRUE).stream().map(Either::get).forEach(item -> {
                    requestorsForEvents.put(item.getRequestor(), null);
                    approversForEvents.put(item.getApprover(), null);
                    if (item.getApprover().equals(curAccountName)) {
                        ids.add(item.getId());
                    } else {    
                        // with take over
                        //approversForEvents.put(item.getApprover(), null);
                        takeOverIds.add(item.getId());
                    }
                });
                
                if (!ids.isEmpty()) {
                    dbLayer.updateApproverStatusInclusiveTransaction(ids, newState);
                }
                if (!takeOverIds.isEmpty()) {
                    dbLayer.updateApproverStatusInclusiveTransaction(takeOverIds, newState, curAccountName);
                }
                
                if (!approversForEvents.isEmpty()) {
                    approversForEvents.keySet().stream().forEach(approver -> {
                        try {
                            approversForEvents.put(approver, dbLayer.getNumOfPendingApprovals(approver));
                        } catch (Exception e) {
                            //
                        }
                    });
                    approversForEvents.values().removeIf(Objects::isNull);
                }
                
                if (!requestorsForEvents.isEmpty()) {
                    approversForEvents.keySet().stream().forEach(requestor -> {
                        try {
                            requestorsForEvents.put(requestor, dbLayer.getNumOfApprovedRejectedRequests(requestor));
                        } catch (Exception e) {
                            //
                        }
                    });
                    requestorsForEvents.values().removeIf(Objects::isNull);
                }
                
                if (!approversForEvents.isEmpty() || !requestorsForEvents.isEmpty()) {
                    EventBus.getInstance().post(new ApprovalUpdatedEvent(requestorsForEvents, approversForEvents));
                }
            }

            if (result.containsKey(Boolean.FALSE)) {
                return JOCDefaultResponse.responseStatus419(result.get(Boolean.FALSE).stream().map(Either::getLeft).collect(Collectors.toList()));
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse init(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.OTHERS);
        JsonValidator.validateFailFast(filterBytes, FourEyesRequestIds.class);
        return initPermissions(null, true);
    }

}