package com.sos.webservices.order.classes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.CommandType;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.jobscheduler.model.order.OrderList;
import com.sos.jobscheduler.model.order.OrderMode;
import com.sos.jobscheduler.model.order.OrderModeType;
import com.sos.joc.Globals;
import com.sos.webservices.order.impl.RemoveOrdersImpl;

public class OrderHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrdersImpl.class);

    public String removeFromJobSchedulerMaster(String masterId, List<DBItemDailyPlan> listOfPlannedOrders) throws JsonProcessingException,
            SOSException, URISyntaxException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        JSBatchCommands batch = new JSBatchCommands();
        batch.setCommands(new ArrayList<Command>());

        String postBody = "";
        String answer = "";
        for (DBItemDailyPlan dbItemDailyPlan : listOfPlannedOrders) {
            CancelOrder cancelOrder = new CancelOrder();
            cancelOrder.setOrderId(dbItemDailyPlan.getOrderKey());
            batch.getCommands().add(cancelOrder);

        }
        postBody = new ObjectMapper().writeValueAsString(batch);
        answer = sosRestApiClient.postRestService(new URI(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + masterId)
                + "/command"), postBody);
        LOGGER.debug(answer);
        return answer;
    }

    // Not used
    private String removeFromJobSchedulerMaster(String masterId, String orderKey) throws JsonProcessingException, SOSException, URISyntaxException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String postBody = "";
        String answer = "";

        CancelOrder cancelOrder = new CancelOrder();
        cancelOrder.setTYPE(CommandType.CANCEL_ORDER);

        OrderMode orderMode = new OrderMode();
        orderMode.setTYPE(OrderModeType.NOT_STARTED);
        cancelOrder.setMode(orderMode);
        cancelOrder.setOrderId(orderKey);

        postBody = new ObjectMapper().writeValueAsString(cancelOrder);
        answer = sosRestApiClient.postRestService(new URI(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + masterId)
                + "/command"), postBody);
        LOGGER.debug(answer);
        return answer;
    }

    public List<OrderItem> getListOfOrdersFromMaster(String masterId) throws SOSException, JsonParseException, JsonMappingException, IOException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String answer = sosRestApiClient.executeRestService(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + masterId)
                + "/order/?return=Order");
        LOGGER.debug(answer);
        ObjectMapper mapper = new ObjectMapper();
        OrderList orderList = mapper.readValue(answer, OrderList.class);
        if (orderList != null) {
            return orderList.getArray();
        } else {
            return new ArrayList<OrderItem>();
        }
    }

}
