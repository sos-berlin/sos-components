package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.webservices.order.initiator.model.OrderTemplate;
 
public abstract class OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSource.class);

    public abstract List<OrderTemplate> fillListOfOrderTemplates() throws IOException;

    protected boolean checkMandatory(OrderTemplate orderTemplate) {
        if (orderTemplate.getPath() == null ) {
            LOGGER.warn("Adding order for controller:" + orderTemplate.getControllerId() + " and workflow: " + orderTemplate.getWorkflowPath()
                    + " --> orderTemplateName: must not be null or empty.");
            return false;
        }
        if (orderTemplate.getWorkflowPath() == null || orderTemplate.getWorkflowPath().isEmpty()) {
            LOGGER.warn("Adding order: " + orderTemplate.getPath() + " for controller:" + orderTemplate.getControllerId()
                    + " --> workflowPath: must not be null or empty.");
            return false;
        }

        if (orderTemplate.getControllerId() == null || orderTemplate.getControllerId().isEmpty()) {
            LOGGER.warn("Adding order: " + orderTemplate.getPath() + " for workflow: " + orderTemplate.getWorkflowPath()
                    + " --> JobSchedulerId: must not be null or empty.");
            return false;
        }

        return true;

    }
}
