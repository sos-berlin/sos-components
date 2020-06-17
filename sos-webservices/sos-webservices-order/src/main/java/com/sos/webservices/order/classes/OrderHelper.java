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
import com.sos.jobscheduler.db.orders.DBItemDailyPlannedOrders;
import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.CommandType;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.jobscheduler.model.order.OrderList;
import com.sos.jobscheduler.model.order.OrderMode;
import com.sos.jobscheduler.model.order.OrderModeType;
import com.sos.webservices.order.impl.RemoveOrdersImpl;

public class OrderHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrdersImpl.class);
    private String jobSchedulerUrl;

    public OrderHelper(String jobSchedulerUrl) {
        super();
        this.jobSchedulerUrl = jobSchedulerUrl + "/master/api";
    }

    public String removeFromJobSchedulerMaster(String jobschedulerId, List<DBItemDailyPlannedOrders> listOfPlannedOrders) throws JsonProcessingException,
            SOSException, URISyntaxException {

        
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        JSBatchCommands batch = new JSBatchCommands();
        batch.setCommands(new ArrayList<Command>());

        String postBody = "";
        String answer = "";
        for (DBItemDailyPlannedOrders dbItemDailyPlannedOrders : listOfPlannedOrders) {
            CancelOrder cancelOrder = new CancelOrder();
            cancelOrder.setOrderId(dbItemDailyPlannedOrders.getOrderKey());
            batch.getCommands().add(cancelOrder);

        }
        postBody = new ObjectMapper().writeValueAsString(batch);
        answer = sosRestApiClient.postRestService(new URI(jobSchedulerUrl+ "/command"), postBody);
        LOGGER.debug(answer);
        return answer;
    }

    // Not used
    private String removeFromJobSchedulerMaster(String jobschedulerId, String orderKey) throws JsonProcessingException, SOSException,
            URISyntaxException {
        
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String postBody = "";
        String answer = "";

        CancelOrder cancelOrder = new CancelOrder();
        // Hi Uwe, setType sollte nicht noetig sein. Die Klasse bestimmt bereits den TYPE siehe com.sos.jobscheduler.model.command.Command
        cancelOrder.setTYPE(CommandType.CANCEL_ORDER);

        OrderMode orderMode = new OrderMode();
        orderMode.setTYPE(OrderModeType.NOT_STARTED);
        cancelOrder.setMode(orderMode);
        cancelOrder.setOrderId(orderKey);

        postBody = new ObjectMapper().writeValueAsString(cancelOrder);
        answer = sosRestApiClient.postRestService(new URI(jobSchedulerUrl + "/command"), postBody);
        LOGGER.debug(answer);
        return answer;
    }

    public List<OrderItem> getListOfOrdersFromMaster(String masterId) throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String answer = sosRestApiClient.executeRestService(jobSchedulerUrl + "/order/?return=Order");
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
