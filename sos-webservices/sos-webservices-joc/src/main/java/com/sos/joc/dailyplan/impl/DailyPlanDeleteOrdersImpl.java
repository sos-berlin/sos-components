package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.common.DailyPlanUtils;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanDeleteOrderResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocAccessDeniedException;
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
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken);
            // validation without required dailyPlanDateFrom 
            JsonValidator.validateFailFast(filterBytes, "orderManagement/dailyplan/dailyPlanOrdersFilterDef-schema.json");
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);
            
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            if (!deleteOrders(in, accessToken, true, true)) {
                return accessDeniedResponse();
            }
            
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public boolean deleteOrders(DailyPlanOrderFilterDef in, String accessToken, boolean withAudit, boolean withEvent) throws SOSHibernateException {
        return deleteOrders(in, accessToken, withAudit, withEvent, true);
    }

    
    public boolean deleteOrders(DailyPlanOrderFilterDef in, String accessToken, boolean withAudit, boolean withEvent, boolean evalPermissions)
            throws SOSHibernateException {

        boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
        boolean permitted = true;
        Set<String> allowedControllers = Collections.emptySet();
        if (!noControllerAvailable) {
            Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
            if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
                controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
            }
            allowedControllers = controllerIds.filter(availableController -> getBasicControllerPermissions(availableController,
                    accessToken).getOrders().getCreate()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
        } else {
            initGetPermissions(accessToken);
        }
        
        if (!permitted) {
            return false;
        }
        
        if (folderPermissions == null) {
            folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        }
        
        if (withAudit) {
            storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN).getId();
        }
        setSettings(IMPL_PATH);
        
        for (String controllerId : allowedControllers) {
            FilterDailyPlannedOrders filter = getOrderFilter("deleteOrdersFromPlan", controllerId, in, true, evalPermissions);
            if (filter == null) {
                continue;
            }
            
            filter.setOrderPlannedStartFrom(null);
            filter.setOrderPlannedStartTo(null);
            filter.setSubmissionForDateFrom(toUTCDate(in.getDailyPlanDateFrom()));
            filter.setSubmissionForDateTo(toUTCDate(in.getDailyPlanDateTo()));
            filter.addState(DailyPlanOrderStateText.PLANNED);

            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                session.setAutoCommit(false);
                //Globals.beginTransaction(session);
                dbLayer.deleteCascading(filter);
                //Globals.commit(session);
                if (withEvent) {
                    if (in.getDailyPlanDateFrom() != null) {
                        EventBus.getInstance().post(new DailyPlanEvent(controllerId, in.getDailyPlanDateFrom()));
                        if (in.getDailyPlanDateTo() != null) {
                            Instant from = JobSchedulerDate.getInstantFromISO8601String(in.getDailyPlanDateFrom() + "T00:00:00Z");
                            Instant to = JobSchedulerDate.getInstantFromISO8601String(in.getDailyPlanDateTo() + "T00:00:00Z");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
                            if (from != null && to != null) {
                                from = from.plusSeconds(86400); // plus one day
                                int i = 0;
                                while (from.isBefore(to) && i < 31) { // one month is max in GUI
                                    i++;
                                    try {
                                        EventBus.getInstance().post(new DailyPlanEvent(controllerId, formatter.format(from)));
                                    } catch (Exception e) {
                                        //
                                    }
                                    from = from.plusSeconds(86400); // plus one day
                                }
                            }
                            EventBus.getInstance().post(new DailyPlanEvent(controllerId, in.getDailyPlanDateTo()));
                        }
                    } else {
                        EventBus.getInstance().post(new DailyPlanEvent(controllerId, null));
                    }
                }
            } catch (Exception e) {
                //Globals.rollback(session);
                throw e;
            } finally {
                Globals.disconnect(session);
            }
        }
        
        return true;
    }

    public Map<String, List<DBItemDailyPlanOrder>> getPlannedOrderIdsFromDailyplanDate(DailyPlanOrderFilterDef in, String accessToken,
            boolean withAudit, boolean withEvent) throws SOSHibernateException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        setSettings(IMPL_PATH);
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerIds = DailyPlanUtils.getOrderIdsFromDailyplanDate(in, getSettings(), false,
                IMPL_PATH);
        if (!ordersPerControllerIds.isEmpty()) {
            ordersPerControllerIds = ordersPerControllerIds.entrySet().stream().filter(availableController -> 
                    getBasicControllerPermissions(availableController.getKey(), accessToken).getOrders().getCancel())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            if (ordersPerControllerIds.keySet().isEmpty()) {
                throw new JocAccessDeniedException("No permissions to delete dailyplan orders");
            }
        } else {
            initGetPermissions(accessToken);
        }
        return ordersPerControllerIds;
    }
    
}
