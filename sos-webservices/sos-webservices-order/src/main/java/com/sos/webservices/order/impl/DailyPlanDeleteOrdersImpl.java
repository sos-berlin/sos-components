package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanDeleteOrderResource;

@Path("daily_plan")
public class DailyPlanDeleteOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);
    private static final String API_CALL_DELETE = "./daily_plan/orders/delete";

    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Delete orders from the daily plan");
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);

            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getFilter()
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getCancel()).collect(Collectors.toSet());
            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());
            storeAuditLog(dailyPlanOrderFilter.getAuditLog(), dailyPlanOrderFilter.getControllerId(), CategoryType.DAILYPLAN);
            setSettings();

            deleteOrdersFromPlan(allowedControllers, dailyPlanOrderFilter);
                 
            EventBus.getInstance().post(new DailyPlanEvent(dailyPlanOrderFilter.getFilter().getDailyPlanDate()));
            
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

   
    
    private void deleteOrdersFromPlan(Set<String> allowedControllers, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocConfigurationException,
            DBConnectionRefusedException, ControllerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, InterruptedException, ExecutionException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            for (String controllerId : allowedControllers) {
                Globals.beginTransaction(sosHibernateSession);
                FilterDailyPlannedOrders filter = getOrderFilter(controllerId, dailyPlanOrderFilter);
                filter.addState(DailyPlanOrderStateText.PLANNED);
                dbLayerDailyPlannedOrders.deleteCascading(filter);
                Globals.commit(sosHibernateSession);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
