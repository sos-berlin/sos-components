package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DailyPlanAudit;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IDailyPlanSubmitOrderResource;

@Path("daily_plan")
public class DailyPlanSubmitOrdersImpl extends JOCResourceImpl implements IDailyPlanSubmitOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmitOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders/submit";

    private void submitOrdersToController(DailyPlanOrderFilter dailyPlanOrderFilter) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, IOException, ParseException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
        orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
        orderInitiatorSettings.setOverwrite(false);
        orderInitiatorSettings.setSubmit(true);

        orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
        orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin", Globals.DEFAULT_PERIOD_DAILY_PLAN));
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

            Globals.beginTransaction(sosHibernateSession);

            boolean hasPermission = true;
            boolean withFolderFilter = dailyPlanOrderFilter.getFilter().getFolders() != null && !dailyPlanOrderFilter.getFilter().getFolders()
                    .isEmpty();

            Set<Folder> folders = addPermittedFolder(dailyPlanOrderFilter.getFilter().getFolders());

            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                filter.addFolderPaths(new HashSet<Folder>(folders));
            }

            if (hasPermission) {

                if (dailyPlanOrderFilter.getFilter().getControllerIds() == null) {
                    dailyPlanOrderFilter.getFilter().setControllerIds(new ArrayList<String>());
                    dailyPlanOrderFilter.getFilter().getControllerIds().add(dailyPlanOrderFilter.getControllerId());
                } else {
                    if (!dailyPlanOrderFilter.getFilter().getControllerIds().contains(dailyPlanOrderFilter.getControllerId())) {
                        dailyPlanOrderFilter.getFilter().getControllerIds().add(dailyPlanOrderFilter.getControllerId());
                    }
                }

                filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
                filter.setSubmitted(false);
                filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate());
                filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
                filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());

                if (dailyPlanOrderFilter.getFilter().getSchedulePaths() != null) {
                    if (dailyPlanOrderFilter.getFilter().getScheduleNames() == null) {
                        dailyPlanOrderFilter.getFilter().setScheduleNames(new ArrayList<String>());
                    }
                    for (String path : dailyPlanOrderFilter.getFilter().getSchedulePaths()) {
                        String name = Paths.get(path).getFileName().toString();
                        dailyPlanOrderFilter.getFilter().getScheduleNames().add(name);
                    }
                }
                if (dailyPlanOrderFilter.getFilter().getWorkflowPaths() != null) {
                    if (dailyPlanOrderFilter.getFilter().getWorkflowNames() == null) {
                        dailyPlanOrderFilter.getFilter().setWorkflowNames(new ArrayList<String>());
                    }

                    for (String path : dailyPlanOrderFilter.getFilter().getWorkflowPaths()) {
                        String name = Paths.get(path).getFileName().toString();
                        dailyPlanOrderFilter.getFilter().getWorkflowNames().add(name);
                    }
                }
                
                filter.setListOfWorkflowNames(dailyPlanOrderFilter.getFilter().getWorkflowNames());
                filter.setListOfScheduleNames(dailyPlanOrderFilter.getFilter().getScheduleNames());

                for (String controllerId : dailyPlanOrderFilter.getFilter().getControllerIds()) {

                    filter.setControllerId(controllerId);
                    orderInitiatorSettings.setControllerId(controllerId);

                    List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                    Globals.commit(sosHibernateSession);
                    orderInitiatorRunner.submitOrders(listOfPlannedOrders);
                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postSubmitOrders(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("Submit orders to JS7 controller");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(this.getControllerId(xAccessToken, dailyPlanOrderFilter.getControllerId()), xAccessToken).getDailyPlan()
                            .getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            submitOrdersToController(dailyPlanOrderFilter);

            DailyPlanAudit orderAudit = new DailyPlanAudit(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getAuditLog());
            logAuditMessage(orderAudit);
            storeAuditLogEntry(orderAudit);
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

    @Override
    public JOCDefaultResponse postSubmitOrders2(String accessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws Exception {
        return postSubmitOrders(accessToken, dailyPlanOrderFilter);
    }

}
