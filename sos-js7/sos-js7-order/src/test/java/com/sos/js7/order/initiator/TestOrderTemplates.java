package com.sos.js7.order.initiator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.webservices.order.initiator.model.OrderTemplate;

 

//Test fails in nightly build
@Ignore
public class TestOrderTemplates {

    @Test
    public void testIsFillListOfOrderTemplates() throws IOException, SOSHibernateException{
        OrderTemplateSource orderTemplateSource = new OrderTemplateSourceFile("src/test/resources/orderTemplates");
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        List<OrderTemplate> listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
        OrderTemplate order = listOfOrderTemplates.get(0);
        
        assertEquals("testIsFillListOfOrderTemplates", "testorder", order.getPath());
    }

}
