package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlannedOrders;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.webservices.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.initiator.model.OrderCleanup;
import com.sos.webservices.order.resource.ICleanupOrderResource;
import com.sos.webservices.order.classes.OrderHelper;

@Path("orders")
public class CleanupOrdersImpl extends JOCResourceImpl implements ICleanupOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupOrdersImpl.class);
    private static final String API_CALL = "./orders/cleanupOrders";
    private List<DBItemDailyPlannedOrders> listOfPlannedOrders;
    
    @Override
    public JOCDefaultResponse postCleanupOrders(String xAccessToken, OrderCleanup orderCleanup) {
        LOGGER.debug("cleanup orders");
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", xAccessToken, orderCleanup.getJobschedulerId(), getPermissonsJocCockpit(
                    "scheduler_joc_cockpit", xAccessToken).getJobChain().getExecute().isAddOrder());
 
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderHelper orderHelper = new OrderHelper();
            // TODO: masterId
            List<OrderItem> listOfOrderItems = orderHelper.getListOfOrdersFromMaster(orderCleanup.getJobschedulerId());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
            FilterDailyPlannedOrders filterDailyPlan = new FilterDailyPlannedOrders();
            listOfPlannedOrders = new ArrayList<DBItemDailyPlannedOrders>();
            for (OrderItem orderItem : listOfOrderItems) {
                filterDailyPlan.setOrderKey(orderItem.getId());
                filterDailyPlan.setWorkflow(orderItem.getWorkflowPosition().getWorkflowId().getPath());
                filterDailyPlan.setJobSchedulerId(orderCleanup.getJobschedulerId());
                List <DBItemDailyPlannedOrders> listOfOrders = dbLayerDailyPlan.getDailyPlanList(filterDailyPlan, 0);
                if (listOfOrders.size() == 0) {
                    DBItemDailyPlannedOrders dbItemDailyPlannedOrders = new DBItemDailyPlannedOrders();
                    dbItemDailyPlannedOrders.setOrderKey(orderItem.getId());
                    listOfPlannedOrders.add(dbItemDailyPlannedOrders);
                }
            }
            orderHelper.removeFromJobSchedulerMaster(orderCleanup.getJobschedulerId(), listOfPlannedOrders);

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }finally {
            Globals.disconnect(sosHibernateSession);
        }

    }
 
}
