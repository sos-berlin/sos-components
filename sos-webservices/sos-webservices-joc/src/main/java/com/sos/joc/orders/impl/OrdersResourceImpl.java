package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
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
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;
import js7.proxy.javaapi.data.workflow.JWorkflowId;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource {

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
            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilter.getControllerId(), getPermissonsJocCockpit(ordersFilter
                    .getControllerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<String> orders = ordersFilter.getOrderIds();
            List<WorkflowId> workflowIds = ordersFilter.getWorkflowIds();
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            boolean withOrderIdFilter = orders != null && !orders.isEmpty();
            final Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            
            JControllerState currentState = Proxy.of(ordersFilter.getControllerId()).currentState();
            Stream<JOrder> orderStream = null;
            if (withOrderIdFilter) {
                ordersFilter.setRegex(null);
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()) && orderIsPermitted(o, folders));
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                ordersFilter.setRegex(null);
                Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().map(w -> JWorkflowId.of(JocInventory.pathToName(w.getPath()),
                        w.getVersionId()).asScala()).collect(Collectors.toSet());
                orderStream = currentState.ordersBy(o -> workflowPaths.contains(o.workflowId()) && orderIsPermitted(o, folders));
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
                orderStream = currentState.ordersBy(JOrderPredicates.none());
            } else if (folders != null && !folders.isEmpty()) {
                orderStream = currentState.ordersBy(o -> orderIsPermitted(o, folders));
            } else {
                orderStream = currentState.ordersBy(JOrderPredicates.any());
            }

            List<OrderStateText> states = ordersFilter.getStates();
            // BLOCKED is not a Controller state. It needs a special handling. These are PENDING with scheduledFor in the past
            final boolean withStatesFilter = states != null && !states.isEmpty();
            final boolean lookingForBlocked = withStatesFilter && states.contains(OrderStateText.BLOCKED);
            final boolean lookingForPending = withStatesFilter && states.contains(OrderStateText.PENDING);
            
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
                    Instant dateTo = JobSchedulerDate.getInstantFromDateStr(ordersFilter.getDateTo(), false, ordersFilter.getTimeZone());
                    final Instant until = (dateTo.isBefore(Instant.now())) ? Instant.now() : dateTo;
                    orderStream = orderStream.filter(o -> {
                        Order.State state = o.asScala().state();
                        if (OrderStateText.PENDING.equals(OrdersHelper.getGroupedState(state.getClass()))) {
                            if (!state.maybeDelayedUntil().isEmpty() && state.maybeDelayedUntil().get().toInstant().isAfter(until)) {
                                return false;
                            }
                        }
                        return true;
                    });
                }
            }
            
            List<JOrder> jOrders = orderStream.collect(Collectors.toList());
            
            // Path of name from db
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            Set<String> names = jOrders.stream().map(o -> o.workflowId().path().string()).collect(Collectors.toSet());
            final Map<String, String> namePathMap = dbLayer.getNamePathMapping(ordersFilter.getControllerId(), names, DeployType.WORKFLOW.intValue());

            Stream<JOrder> jOrderStream = jOrders.stream();
            
            if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(ordersFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
                if (namePathMap != null) {
                    jOrderStream = jOrderStream.filter(o -> regex.test(namePathMap.getOrDefault(o.workflowId().path().string(), o.workflowId().path()
                            .string()) + "/" + o.id().string()));
                } else {
                    jOrderStream = jOrderStream.filter(o -> regex.test(o.workflowId().path().string() + "/" + o.id().string()));
                }
            }
            
            
            Long surveyDateMillis = currentState.eventId() / 1000;
            OrdersV entity = new OrdersV();
            entity.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            
            Stream<Either<Exception, OrderV>> ordersV = jOrderStream.map(o -> {
                Either<Exception, OrderV> either = null;
                try {
                    OrderV order = OrdersHelper.mapJOrderToOrderV(o, ordersFilter.getCompact(), namePathMap, surveyDateMillis);
                    // special BLOCKED handling
                    if (withStatesFilter) {
                       if (lookingForBlocked && !lookingForPending && OrderStateText.PENDING.equals(order.getState().get_text())) {
                           order = null;
                       } else if (lookingForPending && !lookingForBlocked && OrderStateText.BLOCKED.equals(order.getState().get_text())) {
                           order = null;
                       }
                    }
                    if (order != null && orderStateWithRequirements.contains(order.getState().get_text())) {
                        order.setRequirements(OrdersHelper.getRequirements(o, currentState));
                    }
                    either = Either.right(order);
                } catch (Exception e) {
                    either = Either.left(e);
                }
                return either;
            });
            // TODO consider Either::isLeft, maybe at least LOGGER usage
            entity.setOrders(ordersV.filter(Either::isRight).map(Either::get).filter(Objects::nonNull).collect(Collectors.toList()));
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
    
    private static boolean orderIsPermitted(Order<Order.State> order, Set<Folder> listOfFolders) {
        return true;
        // TODO order.workflowId().path().string() is only a name
//        if (listOfFolders == null || listOfFolders.isEmpty()) {
//            return true;
//        }
//        return folderIsPermitted(Paths.get(order.workflowId().path().string()).getParent().toString().replace('\\', '/'), listOfFolders);
    }
    
//    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
//        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
//                .getFolder() + "/")));
//        return listOfFolders.stream().parallel().anyMatch(filter);
//    }

}
