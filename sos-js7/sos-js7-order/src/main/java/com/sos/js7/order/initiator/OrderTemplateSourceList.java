package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.js7.order.initiator.db.DBLayerOrderTemplates;
import com.sos.js7.order.initiator.db.FilterOrderTemplates;
import com.sos.webservices.order.initiator.model.OrderTemplate;

public class OrderTemplateSourceList extends OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSourceList.class);
    private String controllerId;
    private List <String>orderTemplates;

    public OrderTemplateSourceList(String controllerId,List<String> orderTemplates) {
        super();
        this.controllerId = controllerId;
        this.orderTemplates = orderTemplates;
    }

    
    @Override
    public List<OrderTemplate> fillListOfOrderTemplates() throws IOException, SOSHibernateException {        
        List<OrderTemplate> listOfOrderTemplates = new ArrayList<OrderTemplate>();
        FilterOrderTemplates filterOrderTemplates = new FilterOrderTemplates();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderTemplateSourceDB");
        DBLayerOrderTemplates dbLayerOrderTemplates = new DBLayerOrderTemplates(sosHibernateSession);

        filterOrderTemplates.setControllerId(controllerId);
        for (String orderTemplatePath: this.orderTemplates) {
            filterOrderTemplates.addOrderTemplatePath(orderTemplatePath);
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<DBItemInventoryConfiguration> listOfOrderTemplatesDbItems = dbLayerOrderTemplates.getOrderTemplates(filterOrderTemplates, 0);
        for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfOrderTemplatesDbItems) {
            OrderTemplate orderTemplate = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), OrderTemplate.class);
            orderTemplate.setPath(dbItemInventoryConfiguration.getPath());
            if (orderTemplate.getControllerId().equals(this.controllerId)) {
                listOfOrderTemplates.add(orderTemplate);
            }
        }

        return listOfOrderTemplates;
    }

    @Override
    public String fromSource() {
        return "List";
    }

}
