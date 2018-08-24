package com.sos.webservices.order.initiator;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestOrderInitiator {

    @Test
    public void testOrderInitatorGo()   {
        
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner();
        orderInitiatorRunner.run();
         
    //    assertEquals("testIsFillListOfOrderTemplates", "myOrder", order.getOrderKey());
    }

}
