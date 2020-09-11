package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.webservices.order.initiator.model.OrderTemplate;

 
public class OrderTemplates {

    private List<OrderTemplate> listOfOrderTemplates;

    public void fillListOfOrderTemplates(OrderTemplateSource orderTemplateSource) throws IOException, SOSHibernateException {
        listOfOrderTemplates = orderTemplateSource.fillListOfOrderTemplates();
    }

    public List<OrderTemplate> getListOfOrderTemplates() {
        return listOfOrderTemplates;
    }
}
