package com.sos.jitl.jobs.checkhistory.classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryWebserviceExecuterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryWebserviceExecuter.class);

    @Test
    public void testHistoryWebserviceExecuter() throws Exception {
        JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
        jobSchedulerCredentialStoreJOCParameters.setUser("root");
        jobSchedulerCredentialStoreJOCParameters.setPassword("root");
        jobSchedulerCredentialStoreJOCParameters.setJocUrl("http://localhost:4426");
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();
        
        HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(webserviceCredentials);
        OrdersFilter ordersFilter  = new OrdersFilter();
        
      //{"controllerId":"controller","historyIds":["437691"],
      //    "historyStates":["SUCCESSFUL","FAILED"],"states":["FINISHED"],"orders":[{"workflowPath":"Mail"}],"limit":10}
        
        ordersFilter.setControllerId("controller");
        List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
        historyStates.add(HistoryStateText.SUCCESSFUL);
        historyStates.add(HistoryStateText.FAILED);      
        ordersFilter.setHistoryStates(historyStates);
        
        List<OrderStateText> states = new ArrayList<OrderStateText>();
        states.add(OrderStateText.FINISHED);
        ordersFilter.setStates(states);
        
        Set<OrderPath> orders = new HashSet<OrderPath>();
        OrderPath orderPath = new OrderPath();
        orderPath.setWorkflowPath("Mail");
        orders.add(orderPath);
        ordersFilter.setOrders(orders);
        
        ordersFilter.setLimit(10);
        OrderHistoryItem orderHistoryItem = historyWebserviceExecuter.getJobHistoryEntry(ordersFilter);
        System.out.println(orderHistoryItem.getHistoryId());

    }

}
