package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.ICleanupOrderResource;

import js7.proxy.javaapi.data.order.JOrder;

@Path("orders")
public class CleanupOrdersImpl extends JOCResourceImpl implements ICleanupOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupOrdersImpl.class);
    private static final String API_CALL = "./orders/cleanup";
    private List<DBItemDailyPlanOrders> listOfPlannedOrders;
    
    @Override
    public JOCDefaultResponse postCleanupOrders(String xAccessToken, OrderFilter orderCleanup) {
        LOGGER.debug("cleanup orders");
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", xAccessToken, orderCleanup.getControllerId(), getPermissonsJocCockpit(
                    orderCleanup.getControllerId(), xAccessToken).getWorkflow().getExecute().isAddOrder());
 
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            OrderHelper orderHelper = null;
            orderHelper = new OrderHelper( );

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
          // removeOrdersFromControllerNotInDB(sosHibernateSession, orderHelper, orderCleanup);
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

    private void removeOrdersFromControllerNotInDB(SOSHibernateSession sosHibernateSession,  OrderFilter orderCleanup) throws JsonParseException, JsonMappingException, SOSException, IOException, JocConfigurationException, DBOpenSessionException, URISyntaxException, JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, InterruptedException {
        Set<JOrder> listOfOrderItems = OrderHelper.getListOfJOrdersFromController(orderCleanup.getControllerId());
        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
        DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
        FilterDailyPlannedOrders filterDailyPlan = new FilterDailyPlannedOrders();
        listOfPlannedOrders = new ArrayList<DBItemDailyPlanOrders>();
        for (JOrder orderItem : listOfOrderItems) {
//            filterDailyPlan.setOrderKey(orderItem.getId());
//            filterDailyPlan.setWorkflow(orderItem.getWorkflowPosition().getWorkflowId().getPath());
//            filterDailyPlan.setControllerId(orderCleanup.getJobschedulerId());
//            List <DBItemDailyPlanOrders> listOfOrders = dbLayerDailyPlan.getDailyPlanList(filterDailyPlan, 0);
//            if (listOfOrders.size() == 0) {
//                DBItemDailyPlanOrders dbItemDailyPlannedOrders = new DBItemDailyPlanOrders();
//                dbItemDailyPlannedOrders.setOrderKey(orderItem.getId());
//                listOfPlannedOrders.add(dbItemDailyPlannedOrders);
//            }
        }
        OrderHelper.removeFromJobSchedulerController(orderCleanup.getControllerId(), listOfPlannedOrders);
    }
    
   
}
