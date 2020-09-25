package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.js7.order.initiator.db.DBLayerOrderTemplates;
import com.sos.js7.order.initiator.db.FilterOrderTemplates;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import com.sos.webservices.order.initiator.model.OrderTemplateFilter;
import com.sos.webservices.order.initiator.model.OrderTemplatesList;
import com.sos.webservices.order.resource.IOrderTemplatesResource;

@Path("order_templates")
public class OrderTemplates extends JOCResourceImpl implements IOrderTemplatesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplates.class);
    private static final String API_CALL = "./order_templates/list";

    @Override
    public JOCDefaultResponse postOrderTemplates(String xAccessToken, OrderTemplateFilter orderTemplateFilter) {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("reading list of order templates");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderTemplateFilter, xAccessToken, orderTemplateFilter.getControllerId(),
                    getPermissonsJocCockpit(orderTemplateFilter.getControllerId(), xAccessToken).getWorkflow().getExecute().isAddOrder());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderTemplatesList orderTemplatesList = new OrderTemplatesList();
            orderTemplatesList.setOrderTemplates(new ArrayList<OrderTemplate>());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerOrderTemplates dbLayerOrderTemplates = new DBLayerOrderTemplates(sosHibernateSession);
            FilterOrderTemplates filterOrderTemplates = new FilterOrderTemplates();
            filterOrderTemplates.setControllerId(orderTemplateFilter.getControllerId());
            filterOrderTemplates.setPath(orderTemplateFilter.getOrderTemplatePath());

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<DBItemInventoryConfiguration> listOfOrderTemplates = dbLayerOrderTemplates.getOrderTemplates(filterOrderTemplates, 0);
            for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfOrderTemplates) {
                if (dbItemInventoryConfiguration.getContent() != null) {
                    OrderTemplate orderTemplate = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), OrderTemplate.class);
                    orderTemplate.setPath(dbItemInventoryConfiguration.getPath());
                    orderTemplatesList.getOrderTemplates().add(orderTemplate);
                }
            }

            return JOCDefaultResponse.responseStatus200(orderTemplatesList);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
