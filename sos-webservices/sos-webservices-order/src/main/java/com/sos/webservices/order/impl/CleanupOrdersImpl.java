package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.resource.ICleanupOrderResource;
import com.sos.webservices.order.classes.OrderHelper;

@Path("orders")
public class CleanupOrdersImpl extends JOCResourceImpl implements ICleanupOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupOrdersImpl.class);
    private static final String API_CALL = "./orders/cleanupOrders";
    private List<DBItemDailyPlan> listOfPlannedOrders;
    
    @Override
    public JOCDefaultResponse postCleanupOrders(String xAccessToken) {
        LOGGER.debug("cleanup orders");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", xAccessToken, "scheduler_joc_cockpit", getPermissonsJocCockpit(
                    "scheduler_joc_cockpit", xAccessToken).getJobChain().getExecute().isAddOrder());
 
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderHelper orderHelper = new OrderHelper();
            // TODO: masterId
            List<OrderItem> listOfOrderItems = orderHelper.getListOfOrdersFromMaster("scheduler_joc_cockpit");
            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            FilterDailyPlan filterDailyPlan = new FilterDailyPlan();
            listOfPlannedOrders = new ArrayList<DBItemDailyPlan>();
            for (OrderItem orderItem : listOfOrderItems) {
                filterDailyPlan.setOrderKey(orderItem.getId());
                filterDailyPlan.setWorkflow(orderItem.getWorkflowPosition().getWorkflowId().getPath());
                filterDailyPlan.setMasterId("scheduler_joc_cockpit");
                List <DBItemDailyPlan> listOfOrders = dbLayerDailyPlan.getDailyPlanList(filterDailyPlan, 0);
                if (listOfOrders.size() == 0) {
                    DBItemDailyPlan dbItemDailyPlan = new DBItemDailyPlan();
                    dbItemDailyPlan.setOrderKey(orderItem.getId());
                    listOfPlannedOrders.add(dbItemDailyPlan);
                }
            }
            orderHelper.removeFromJobSchedulerMaster("scheduler_joc_cockpit", listOfPlannedOrders);

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
