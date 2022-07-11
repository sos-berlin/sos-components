package com.sos.jitl.jobs.checkhistory.classes;

import org.junit.Ignore;
import org.junit.Test;

public class HistoryWebserviceExecuterTest {

    @Ignore
    @Test
    public void testHistoryWebserviceExecuter() throws Exception {
       /* JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
        jobSchedulerCredentialStoreJOCParameters.setUser("root");
        jobSchedulerCredentialStoreJOCParameters.setPassword("root");
        jobSchedulerCredentialStoreJOCParameters.setJocUrl("http://localhost:4426");
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(null,jobSchedulerCredentialStoreJOCParameters);
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();

        HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(null, webserviceCredentials);
        OrdersFilter ordersFilter = new OrdersFilter();

        // {"controllerId":"controller","historyIds":["437691"],
        // "historyStates":["SUCCESSFUL","FAILED"],"states":["FINISHED"],"orders":[{"workflowPath":"Mail"}],"limit":10}

        ordersFilter.setControllerId("controller");
        List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
        historyStates.add(HistoryStateText.SUCCESSFUL);
        historyStates.add(HistoryStateText.FAILED);
        ordersFilter.setHistoryStates(historyStates);

        Set<OrderPath> orders = new HashSet<OrderPath>();
        OrderPath orderPath = new OrderPath();
        orderPath.setWorkflowPath("Mail");
        orders.add(orderPath);
        ordersFilter.setOrders(orders);

        ordersFilter.setLimit(10);
        OrderHistory orderHistory = historyWebserviceExecuter.getWorkflowHistoryEntry(ordersFilter);
       System.out.println(orderHistory.getHistory().get(0).getHistoryId());*/ 

    }

}
