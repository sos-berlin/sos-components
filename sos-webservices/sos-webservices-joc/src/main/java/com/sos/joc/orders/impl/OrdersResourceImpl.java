package com.sos.joc.orders.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Path;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.item.ItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;
import js7.proxy.javaapi.data.workflow.JWorkflowId;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource {

    private static final String API_CALL = "./orders";

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
		try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilter.getJobschedulerId(), getPermissonsJocCockpit(ordersFilter
                    .getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<String> orders = ordersFilter.getOrderIds();
            List<WorkflowId> workflowIds = ordersFilter.getWorkflowIds();
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            
            JControllerState currentState = Proxy.of(ordersFilter.getJobschedulerId()).currentState();
            Stream<JOrder> orderStream = null;
            if (orders != null && !orders.isEmpty()) {
                ordersFilter.setRegex(null);
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()) && orderIsPermitted(o, folders));
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                ordersFilter.setRegex(null);
                Set<ItemId<WorkflowPath>> workflowPaths = workflowIds.stream().map(w -> JWorkflowId.of(w.getPath(), w.getVersionId()).asScala())
                        .collect(Collectors.toSet());
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
            if (states != null && !states.isEmpty()) {
                orderStream = orderStream.filter(o -> states.contains(OrdersHelper.getGroupedState(o.asScala().state().getClass())));
            }

            if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(ordersFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
                orderStream = orderStream.filter(o -> regex.test(o.workflowId().path().string() + "/" + o.id().string()));
            }
            
            Long surveyDateMillis = currentState.eventId() / 1000;
            
            OrdersV entity = new OrdersV();
            entity.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            Stream<Either<Exception, OrderV>> ordersV = orderStream.map(o -> {
                Either<Exception, OrderV> either = null;
                try {
                    either = Either.right(OrdersHelper.mapJOrderToOrderV(o, ordersFilter.getCompact(), surveyDateMillis, false));
                } catch (Exception e) {
                    either = Either.left(e);
                }
                return either;
            });
            // TODO consider Either::isLeft, maybe at least LOGGER usage
            entity.setOrders(ordersV.filter(Either::isRight).map(Either::get).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static boolean orderIsPermitted(Order<Order.State> order, Set<Folder> listOfFolders) {
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        return folderIsPermitted(Paths.get(order.workflowId().path().string()).getParent().toString().replace('\\', '/'), listOfFolders);
    }
    
    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }

}
