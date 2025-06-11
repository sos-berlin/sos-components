package com.sos.joc.dailyplan.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanModifyPriority;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanChangePriority;
import com.sos.joc.orders.impl.OrdersResourceModifyImpl;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanModifyPriorityImpl extends JOCOrderResourceImpl implements IDailyPlanModifyPriority {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyPriorityImpl.class);
    private JControllerState currentState = null;

    @Override
    public JOCDefaultResponse postModifyPriority(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
            JsonValidator.validate(filterBytes, DailyPlanChangePriority.class);
            DailyPlanChangePriority in = Globals.objectMapper.readValue(filterBytes, DailyPlanChangePriority.class);
            String controllerId = in.getControllerId();

            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).map(p -> p.getOrders()
                    .getModify()));
            if (response != null) {
                return response;
            }
            
            DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId());

            Map<Boolean, List<String>> orderIds = in.getOrderIds().stream().distinct().collect(Collectors.groupingBy(id -> id.matches(
                    "#[^#]+#[PC][0-9]+-.*")));
            orderIds.putIfAbsent(Boolean.TRUE, Collections.emptyList());
            orderIds.putIfAbsent(Boolean.FALSE, Collections.emptyList());
            
            Set<DBItemDailyPlanOrder> submittedOrdersWithChangedPrio = new HashSet<>();
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Stream<AuditLogDetail> auditLogDetails = Stream.empty();
            List<AuditLogDetail> auditLogDetails2 = Collections.emptyList();
            
            if (!orderIds.get(Boolean.FALSE).isEmpty()) {

                currentState = Proxy.of(controllerId).currentState();
                
                auditLogDetails2 = OrdersHelper.getPermittedJOrdersFromOrderIds(orderIds.get(Boolean.FALSE).stream().map(OrderId::of),
                        permittedFolders, currentState).map(o -> new AuditLogDetail(WorkflowPaths.getPath(o.workflowId().path().string()), o.id()
                                .string(), controllerId)).toList();
            }
            
            Date now = Date.from(Instant.now());
            
            if (!orderIds.get(Boolean.TRUE).isEmpty()) {

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                session.setAutoCommit(false);
                Globals.beginTransaction(session);
                
                Function<DBItemDailyPlanOrder, DBItemDailyPlanOrder> setPrio = item -> {
                    try {
                        OrderParameterisation op = new OrderParameterisation();
                        if (item.getOrderParameterisation() == null) {
                            if (in.getPriority() == 0) {
                                return null;
                            }
                            op.setPriority(in.getPriority());
                        } else {
                            op = Globals.objectMapper.readValue(item.getOrderParameterisation(), OrderParameterisation.class);
                            Integer origPrio = op.getPriority();
                            if (origPrio == null) {
                                origPrio = 0;
                            }
                            if (origPrio == in.getPriority()) {
                                return null;
                            }
                            op.setPriority(in.getPriority());
                        }
                        item.setOrderParameterisation(Globals.objectMapper.writeValueAsString(op));
                        LOGGER.info(item.toString());
                        return item;
                    } catch (Exception e) {
                        LOGGER.error("", e);
                        return null;
                    }
                };

                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                auditLogDetails = getDailyPlanOrders(controllerId, orderIds.get(Boolean.TRUE), dbLayer).stream().filter(item -> folderIsPermitted(item
                        .getWorkflowFolder(), permittedFolders)).map(setPrio).filter(Objects::nonNull).map(item -> {
                            if (item.getSubmitted()) {
                                submittedOrdersWithChangedPrio.add(item);
                            } else {
                                dbLayer.updateOrderParameterisation(item, now);
                            }
                            return item;
                        }).map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), controllerId));

                Globals.commit(session);
            }

            List<AuditLogDetail> auditLogDetails3 = Stream.concat(auditLogDetails, auditLogDetails2.stream()).toList();

            command(in, submittedOrdersWithChangedPrio, auditLogDetails2).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    if (!submittedOrdersWithChangedPrio.isEmpty()) {
                        SOSHibernateSession session1 = null;
                        try {
                            session1 = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                            session1.setAutoCommit(false);
                            Globals.beginTransaction(session1);
                            DBLayerDailyPlannedOrders dbLayer1 = new DBLayerDailyPlannedOrders(session1);
                            for (DBItemDailyPlanOrder item : submittedOrdersWithChangedPrio) {
                                dbLayer1.updateOrderParameterisation(item, now);
                            }
                            Globals.commit(session1);
                        } catch (Exception e) {
                            Globals.rollback(session1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(session1);
                        }
                    }

                    try {
                        JocAuditLog.storeAuditLogDetails(auditLogDetails3, auditlog.getId());
                    } catch (Exception e) {
                        //
                    }
                }
            });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private CompletableFuture<Either<Problem, ControllerCommand.Response>> command(DailyPlanChangePriority in,
            Set<DBItemDailyPlanOrder> submittedOrdersWithChangedPrio, List<AuditLogDetail> auditLogDetails2)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        Stream<OrderId> oIdsStream = auditLogDetails2.stream().map(AuditLogDetail::getOrderId).map(OrderId::of);
        
        if (!submittedOrdersWithChangedPrio.isEmpty()) {
            if (currentState == null) {
                currentState = Proxy.of(in.getControllerId()).currentState();
            }

            Set<OrderId> knownOrderIds = currentState.orderIds();
            List<String> submittedOrderIdsWithChangedPrio = submittedOrdersWithChangedPrio.stream().map(DBItemDailyPlanOrder::getOrderId).toList();
            oIdsStream = Stream.concat(oIdsStream, submittedOrderIdsWithChangedPrio.stream().map(OrderId::of).filter(knownOrderIds::contains));
            oIdsStream = Stream.concat(oIdsStream, OrdersResourceModifyImpl.cyclicFreshOrderIds(submittedOrderIdsWithChangedPrio, currentState).map(
                    JOrder::id)).distinct();
        }

        Optional<BigDecimal> prio = Optional.of(new BigDecimal(in.getPriority()));
        List<JControllerCommand> commands = oIdsStream.map(oId -> JControllerCommand.changeOrder(oId, prio)).toList();
        if (commands.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> Either.right(null));
        } else if (commands.size() == 1) {
            return ControllerApi.of(in.getControllerId()).executeCommand(commands.get(0));
        } else {
            return ControllerApi.of(in.getControllerId()).executeCommand(JControllerCommand.batch(commands));
        }
    }

    private List<DBItemDailyPlanOrder> getDailyPlanOrders(String controllerId, List<String> dailyPlanOrderIds, DBLayerDailyPlannedOrders dbLayer)
            throws SOSHibernateException {
        List<DBItemDailyPlanOrder> dailyPlanOrderItems = null;
        if (!dailyPlanOrderIds.isEmpty()) {
            dailyPlanOrderItems = dbLayer.getDailyPlanOrders(controllerId, dailyPlanOrderIds);
        }
        if (dailyPlanOrderItems == null) {
            dailyPlanOrderItems = Collections.emptyList();
        }
        return dailyPlanOrderItems;
    }

}
