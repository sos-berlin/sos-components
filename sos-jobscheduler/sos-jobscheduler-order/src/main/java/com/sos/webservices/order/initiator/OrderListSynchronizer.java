package com.sos.webservices.order.initiator;

import java.util.ArrayList;
import java.util.List;

import com.sos.jobscheduler.model.order.FreshOrder;

public class OrderListSynchronizer {
    
    private List <FreshOrder> listOfOrders;
    
    public OrderListSynchronizer() {
        super();
        listOfOrders = new ArrayList<FreshOrder>();
    }

    public void add(FreshOrder o) {
        listOfOrders.add(o);
    }

    public void removeAllOrdersFromMaster() {
        // TODO Auto-generated method stub
        
    }

    public void addOrdersToMaster() {
        // TODO Auto-generated method stub
        
    }


}
