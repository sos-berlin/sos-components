package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.js7.order.initiator.model.OrderTemplate;

public abstract class OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSource.class);

    public abstract List<OrderTemplate> fillListOfOrderTemplates() throws IOException;

    protected boolean checkMandatory(OrderTemplate orderTemplate) {
        if (orderTemplate.getTemplateId() == null ) {
            LOGGER.warn("Adding order for controller:" + orderTemplate.getJobschedulerId() + " and workflow: " + orderTemplate.getWorkflowPath()
                    + " --> templateId: must not be null or empty.");
            return false;
        }
        if (orderTemplate.getWorkflowPath() == null || orderTemplate.getWorkflowPath().isEmpty()) {
            LOGGER.warn("Adding order: " + orderTemplate.getOrderTemplateName() + " for controller:" + orderTemplate.getJobschedulerId()
                    + " --> workflowPath: must not be null or empty.");
            return false;
        }

        if (orderTemplate.getJobschedulerId() == null || orderTemplate.getJobschedulerId().isEmpty()) {
            LOGGER.warn("Adding order: " + orderTemplate.getOrderTemplateName() + " for workflow: " + orderTemplate.getWorkflowPath()
                    + " --> JobSchedulerId: must not be null or empty.");
            return false;
        }

        return true;

    }
}
