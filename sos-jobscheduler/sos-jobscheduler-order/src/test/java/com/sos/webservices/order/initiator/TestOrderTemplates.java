package com.sos.webservices.order.initiator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.sos.webservices.order.initiator.model.OrderTemplate;



public class TestOrderTemplates {

    @Test
    public void testIsFillListOfOrderTemplates() throws IOException{
        OrderTemplateSource orderTemplateSource = new OrderTemplateSourceFile("src/test/resources/orderTemplates");
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        List<OrderTemplate> listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
        OrderTemplate order = listOfOrderTemplates.get(0);
        
        assertEquals("testIsFillListOfOrderTemplates", "testorder", order.getOrderTemplateName());
    }

}
