package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.proxy.javaapi.data.JControllerState;
import js7.proxy.javaapi.data.JOrderPredicates;

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
            //########################## IN PROGRESS #####################################################
            List<OrderPath> orders = ordersFilter.getOrders();
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            Map<String, Set<String>> ordersPerWorkflow = null;
            
            if (orders != null && !orders.isEmpty()) {
                final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                ordersPerWorkflow = orders.stream().filter(o -> canAdd(o.getWorkflow(), permittedFolders)).collect(Collectors.groupingBy(
                        OrderPath::getWorkflow, Collectors.mapping(OrderPath::getOrderId, Collectors.toSet())));
            }
            
            JControllerState currentState = Proxy.of(ordersFilter.getJobschedulerId()).currentState();
            Date surveyDate = Date.from(Instant.ofEpochMilli(currentState.eventId() / 1000));
			Set<Either<Exception, OrderItem>> jOrders = currentState.ordersBy(JOrderPredicates.any()).map(o -> {
			    Either<Exception, OrderItem> either = null;
			    try {
			        either = Either.right(Globals.objectMapper.readValue(o.toJson(), OrderItem.class));
                } catch (Exception e) {
                    either = Either.left(e);
                }
			    return either;
			}).collect(Collectors.toSet());
			
            OrdersV entity = new OrdersV();
            entity.setOrders(jOrders.stream().filter(e -> e.isRight()).map(e -> e.get()).collect(Collectors.toList()));
            entity.setDeliveryDate(surveyDate);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
