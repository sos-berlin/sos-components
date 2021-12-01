package com.sos.joc.dailyplan.impl;

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
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanDeleteOrderResource;
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
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanDeleteOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);

    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Delete orders from the daily plan");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);
            DailyPlanOrderFilter in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            Set<String> allowedControllers = getAllowedControllersOrdersView(in.getControllerId(), in.getFilter().getControllerIds(), accessToken)
                    .stream().filter(availableController -> getControllerPermissions(availableController, accessToken).getOrders().getCancel())
                    .collect(Collectors.toSet());
            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            this.checkRequiredParameter("filter", in.getFilter());
            this.checkRequiredParameter("dailyPlanDate", in.getFilter().getDailyPlanDate());
            storeAuditLog(in.getAuditLog(), in.getControllerId(), CategoryType.DAILYPLAN);
            setSettings();

            deleteOrdersFromPlan(allowedControllers, in);

            EventBus.getInstance().post(new DailyPlanEvent(in.getFilter().getDailyPlanDate()));

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void deleteOrdersFromPlan(Set<String> allowedControllers, DailyPlanOrderFilter in) throws JocConfigurationException,
            DBConnectionRefusedException, ControllerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, InterruptedException, ExecutionException {

        for (String controllerId : allowedControllers) {
            FilterDailyPlannedOrders filter = getOrderFilter("deleteOrdersFromPlan", controllerId, in, true);
            if (filter == null) {
                continue;
            }
            filter.addState(DailyPlanOrderStateText.PLANNED);

            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                session.setAutoCommit(false);
                Globals.beginTransaction(session);
                dbLayer.deleteCascading(filter);
                Globals.commit(session);
            } finally {
                Globals.disconnect(session);
            }
        }

    }

}
