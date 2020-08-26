package com.sos.joc.orders.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.orders.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource {

    private static final String API_CALL = "./orders";

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
		try {
            JsonValidator.validateFailFast(filterBytes, OrdersFilter.class);
            OrdersFilter ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilter.class);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken, ordersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<OrderPath> orders = ordersFilter.getOrders();
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            Map<String, Set<String>> ordersOrWorkflows = null;
            
            if (orders != null && !orders.isEmpty()) {
                ordersFilter.setRegex(null);
                final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                ordersOrWorkflows = orders.stream().filter(o -> canAdd(o.getWorkflow(), permittedFolders)).collect(Collectors.groupingBy(o -> {
                    return (o.getOrderId() == null || o.getOrderId().isEmpty()) ? "workflows" : "orderIds";
                }, Collectors.mapping(o -> {
                    return (o.getOrderId() == null || o.getOrderId().isEmpty()) ? o.getWorkflow() : o.getWorkflow() + "/" + o.getOrderId();
                }, Collectors.toSet())));
            }
            
            JControllerState currentState = Proxy.of(ordersFilter.getJobschedulerId()).currentState();
            Stream<JOrder> orderStream = null;
            if (ordersOrWorkflows != null && !ordersOrWorkflows.isEmpty()) {
                ordersOrWorkflows.putIfAbsent("workflows", Collections.emptySet());
                ordersOrWorkflows.putIfAbsent("orderIds", Collections.emptySet());
                final Map<String, Set<String>> c = Collections.unmodifiableMap(ordersOrWorkflows);
                orderStream = currentState.ordersBy(o -> c.get("orderIds").contains(o.workflowId().path().string() + "/" + o.id().string()) || c.get(
                        "workflows").contains(o.workflowId().path().string()));
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
                orderStream = currentState.ordersBy(JOrderPredicates.none());
            } else if (folders != null && !folders.isEmpty()) {
                orderStream = currentState.ordersBy(o -> orderIsPermitted(o.workflowId().path().string(), folders));
            } else {
                orderStream = currentState.ordersBy(JOrderPredicates.any());
            }

            List<OrderStateText> states = ordersFilter.getStates();
            if (states != null && !states.isEmpty()) {
                orderStream = orderStream.filter(o -> states.contains(OrdersHelper.getGroupedState(o.asScala().state().getClass())));
            }

            if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(ordersFilter.getRegex()).asPredicate();
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
            entity.setOrders(ordersV.filter(e -> e.isRight()).map(e -> e.get()).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static boolean orderIsPermitted(String orderPath, Set<Folder> listOfFolders) {
        return folderIsPermitted(Paths.get(orderPath).getParent().toString().replace('\\', '/'), listOfFolders);
    }
    
    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }

}
