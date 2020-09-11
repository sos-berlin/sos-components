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

public class OrderTemplateSourceDB extends OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSourceDB.class);
    private String controllerId;

    public OrderTemplateSourceDB(String controllerId) {
        super();
        this.controllerId = controllerId;
    }

    @Override
    public List<OrderTemplate> fillListOfOrderTemplates() throws IOException, SOSHibernateException {
        FilterOrderTemplates filterOrderTemplates = new FilterOrderTemplates();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderTemplateSourceDB");
        List<OrderTemplate> listOfOrderTemplates = new ArrayList<OrderTemplate>();
        DBLayerOrderTemplates dbLayerOrderTemplates = new DBLayerOrderTemplates(sosHibernateSession);

        filterOrderTemplates.setControllerId(controllerId);

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
        return "Databae";
    }

}
