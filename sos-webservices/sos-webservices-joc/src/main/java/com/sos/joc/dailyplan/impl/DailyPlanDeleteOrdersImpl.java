package com.sos.joc.dailyplan.impl;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
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
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanDeleteOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);

    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Delete orders from the daily plan");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilterDef.class);
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);

            JOCDefaultResponse response = initPermissions(null, deleteOrders(in, accessToken, true));
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public boolean deleteOrders(DailyPlanOrderFilterDef in, String accessToken, boolean withEvent) throws JocConfigurationException,
            DBConnectionRefusedException, ControllerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, InterruptedException, ExecutionException {

        boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
        boolean permitted = true;
        Set<String> allowedControllers = Collections.emptySet();
        if (!noControllerAvailable) {
            Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
            if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
                controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
            }
            allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
                    accessToken).getOrders().getCreate()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
        }
        
        if (!permitted) {
            return false;
        }
        
        if (folderPermissions == null) {
            folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        }
        
        storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN);
        setSettings();
        boolean sendEvent = false;
        
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
                sendEvent = true;
            } catch (Exception e) {
                Globals.rollback(session);
                throw e;
            } finally {
                Globals.disconnect(session);
            }
        }
        
        if (withEvent && sendEvent) {
            EventBus.getInstance().post(new DailyPlanEvent(in.getDailyPlanDateFrom())); //TODO consider getDailyPlanDateTo
        }
        
        return true;
    }

}
