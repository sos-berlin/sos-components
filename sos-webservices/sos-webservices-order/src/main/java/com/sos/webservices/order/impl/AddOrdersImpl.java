package com.sos.webservices.order.impl;

import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.initiator.OrderListSynchronizer;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import com.sos.webservices.order.resource.IAddOrderResource;

@Path("orders")
public class AddOrdersImpl extends JOCResourceImpl implements IAddOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddOrdersImpl.class);
    private static final String API_CALL = "./orders/addOrders";

    @Override
    public JOCDefaultResponse postAddOrders(String xAccessToken, OrderTemplate orderTemplate) {
        LOGGER.debug("adding order the to the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderTemplate, xAccessToken, orderTemplate.getJobschedulerId(), getPermissonsJocCockpit(
                    orderTemplate.getJobschedulerId(), xAccessToken).getJobChain().getExecute().isAddOrder());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
           
            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            
            if (Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + orderTemplate.getJobschedulerId()) != null){
                Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + orderTemplate.getJobschedulerId());
            } else {
                orderInitiatorSettings.setJobschedulerUrl(this.dbItemInventoryInstance.getUri());
            }
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer(orderInitiatorSettings);
           
         //   FreshOrder freshOrder = buildFreshOrder(orderTemplate, 0);
            PlannedOrder plannedOrder = new PlannedOrder();
           // plannedOrder.setFreshOrder(freshOrder);
           // plannedOrder.setPlanId(dbItemDaysPlanned.getId());
            plannedOrder.setOrderTemplate(orderTemplate);
            orderListSynchronizer.add(plannedOrder);
            orderListSynchronizer.addPlannedOrderToMasterAndDB();

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
