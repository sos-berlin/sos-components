package com.sos.webservices.order.initiator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private static String JOC_URL = "http://localhost:4244/master/api";
    private List<PlannedOrder> listOfOrders;

    public OrderListSynchronizer() {
        super();
        listOfOrders = new ArrayList<PlannedOrder>();
    }

    public void add(PlannedOrder o) {
        listOfOrders.add(o);
    }

    public void removeAllOrdersFromMaster() {
        // TODO Auto-generated method stub
    }

    public void addOrdersToMaster() throws JsonProcessingException, SOSException, URISyntaxException, JocConfigurationException,
            DBConnectionRefusedException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String postBody = "";
        String answer = "";
        for (PlannedOrder plannedOrder : listOfOrders) {
            if (!plannedOrder.orderExist()) {
                postBody = new ObjectMapper().writeValueAsString(plannedOrder.getFreshOrder());
                answer = sosRestApiClient.postRestService(new URI(JOC_URL + "/order"), postBody);
                LOGGER.debug(answer);
            }

        }

    }

}
