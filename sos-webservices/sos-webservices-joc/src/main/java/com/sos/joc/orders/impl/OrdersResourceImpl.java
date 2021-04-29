package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import scala.Function1;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceImpl.class);
    private static final String API_CALL = "./orders";
    private final List<OrderStateText> orderStateWithRequirements = Arrays.asList(OrderStateText.PENDING, OrderStateText.BLOCKED,
            OrderStateText.SUSPENDED);

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilter.getControllerId(), getControllerPermissions(ordersFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Set<String> orders = ordersFilter.getOrderIds();
            Set<WorkflowId> workflowIds = ordersFilter.getWorkflowIds();
            boolean withOrderIdFilter = orders != null && !orders.isEmpty();
            if (withOrderIdFilter) {
                ordersFilter.setFolders(null);
            }
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());

            Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> o
                    .id().string().matches(".*#C[0-9]+-.*"));
            Function1<Order<Order.State>, Object> notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);

            List<OrderStateText> states = ordersFilter.getStates();
            // BLOCKED is not a Controller state. It needs a special handling. These are PENDING with scheduledFor in the past
            final boolean withStatesFilter = states != null && !states.isEmpty();
            final boolean lookingForBlocked = withStatesFilter && states.contains(OrderStateText.BLOCKED);
            final boolean lookingForPending = withStatesFilter && states.contains(OrderStateText.PENDING);

            JControllerState currentState = Proxy.of(ordersFilter.getControllerId()).currentState();
            Stream<JOrder> orderStream = Stream.empty();
            Stream<JOrder> cycledOrderStream = Stream.empty();

            if (withOrderIdFilter) {
                ordersFilter.setRegex(null);
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                ordersFilter.setRegex(null);
                Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
                Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                        .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
                Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                        .pathToName(w.getPath()))).collect(Collectors.toSet());
                Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                        .workflowId().path()));
                orderStream = currentState.ordersBy(JOrderPredicates.and(workflowFilter, notCycledOrderFilter));
                cycledOrderStream = currentState.ordersBy(JOrderPredicates.and(workflowFilter, cycledOrderFilter));
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
                // orderStream = currentState.ordersBy(JOrderPredicates.none());
            } else if (folders != null && !folders.isEmpty()) {
                orderStream = currentState.ordersBy(notCycledOrderFilter);
                cycledOrderStream = currentState.ordersBy(cycledOrderFilter);
            } else {
                orderStream = currentState.ordersBy(notCycledOrderFilter);
                cycledOrderStream = currentState.ordersBy(cycledOrderFilter);
            }

            // grouping cycledOrders and return the first pending Order of the group to orderStream
            Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
            Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.collect(Collectors.groupingBy(o -> o.id().string().substring(0, 24),
                    Collectors.toCollection(() -> new TreeSet<>(comp)))).values();
            cycledOrderStream = cycledOrderColl.stream().map(t -> t.first());
            Map<String, CyclicOrderInfos> cycleInfos = cycledOrderColl.stream().filter(t -> !t.isEmpty()).map(t -> {
                CyclicOrderInfos cycle = new CyclicOrderInfos();
                cycle.setCount(t.size());
                cycle.setFirstOrderId(t.first().id().string());
                if (t.first().scheduledFor().isPresent()) {
                    cycle.setFirstStart(Date.from(t.first().scheduledFor().get()));
                }
                cycle.setLastOrderId(t.last().id().string());
                if (t.last().scheduledFor().isPresent()) {
                    cycle.setLastStart(Date.from(t.last().scheduledFor().get()));
                }
                return cycle;
            }).collect(Collectors.toMap(CyclicOrderInfos::getFirstOrderId, Function.identity()));

            // merge cycledOrders to orderStream and grouping by workflow name for folder permissions
            Map<String, List<JOrder>> groupedByWorkflowPath = Stream.concat(orderStream, cycledOrderStream).collect(Collectors.groupingBy(o -> o
                    .workflowId().path().string()));

            orderStream = groupedByWorkflowPath.entrySet().stream().filter(e -> canAdd(WorkflowPaths.getPath(e.getKey()), folders)).flatMap(e -> e
                    .getValue().stream());

            if (withStatesFilter) {
                // special BLOCKED handling
                if (lookingForBlocked && !lookingForPending) {
                    states.add(OrderStateText.PENDING);
                }
                if (states.contains(OrderStateText.SUSPENDED)) {
                    orderStream = orderStream.filter(o -> o.asScala().isSuspended() || states.contains(OrdersHelper.getGroupedState(o.asScala()
                            .state().getClass())));
                } else {
                    orderStream = orderStream.filter(o -> !o.asScala().isSuspended() && states.contains(OrdersHelper.getGroupedState(o.asScala()
                            .state().getClass())));
                }

            }

            // OrderIds beat dateTo
            if (!withOrderIdFilter && ordersFilter.getDateTo() != null && !ordersFilter.getDateTo().isEmpty()) {
                // only necessary if fresh orders in orderStream
                if (!withStatesFilter || lookingForPending) {
                    // temp. workaround
                    String dateTo = ordersFilter.getDateTo();
                    if ("0d".equals(dateTo)) {
                        dateTo = "1d";
                    }
                    Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, ordersFilter.getTimeZone());
                    final Instant until = (dateToInstant.isBefore(Instant.now())) ? Instant.now() : dateToInstant;
                    Predicate<JOrder> dateToFilter = o -> {
                        if (OrderStateText.PENDING.equals(OrdersHelper.getGroupedState(o.asScala().state().getClass()))) {
                            if (o.scheduledFor().isPresent() && o.scheduledFor().get().isAfter(until)) {
                                return false;
                            }
                        }
                        return true;
                    };
                    orderStream = orderStream.filter(dateToFilter);
                }
            }

            if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(ordersFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
                orderStream = orderStream.filter(o -> regex.test(WorkflowPaths.getPath(o.workflowId().path().string()) + "/" + o.id().string()));
            }

            Long surveyDateMillis = currentState.eventId() / 1000;
            OrdersV entity = new OrdersV();
            entity.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            //Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            Stream<Either<Exception, OrderV>> ordersV = orderStream.map(o -> {
                Either<Exception, OrderV> either = null;
                try {
                    OrderV order = OrdersHelper.mapJOrderToOrderV(o, ordersFilter.getCompact(), null, surveyDateMillis);
                    // special BLOCKED handling
                    if (withStatesFilter) {
                        if (lookingForBlocked && !lookingForPending && OrderStateText.PENDING.equals(order.getState().get_text())) {
                            order = null;
                        } else if (lookingForPending && !lookingForBlocked && OrderStateText.BLOCKED.equals(order.getState().get_text())) {
                            order = null;
                        }
                    }
                    if (order != null) {
                        order.setCyclicOrder(cycleInfos.get(order.getOrderId()));
                        if (orderStateWithRequirements.contains(order.getState().get_text())) {
                            order.setRequirements(OrdersHelper.getRequirements(o, currentState));
                        }
                    }
                    either = Either.right(order);
                } catch (Exception e) {
                    if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                        LOGGER.info(getJocError().printMetaInfo());
                        getJocError().clearMetaInfo();
                    }
                    either = Either.left(e);
                    LOGGER.error(String.format("[%s] %s", o.id().string(), e.toString()));
                }
                return either;
            });
            entity.setOrders(ordersV.filter(Either::isRight).map(Either::get).filter(Objects::nonNull).sorted(Comparator.comparingLong(
                    o -> o.getScheduledFor() == null ? surveyDateMillis : o.getScheduledFor())).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}
