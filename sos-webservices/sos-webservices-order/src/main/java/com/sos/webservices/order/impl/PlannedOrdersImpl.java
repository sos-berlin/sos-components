package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.plan.Period;
import com.sos.joc.model.plan.PlannedOrderItem;
import com.sos.joc.model.plan.PlannedOrderState;
import com.sos.joc.model.plan.PlannedOrderStateText;
import com.sos.joc.model.plan.PlannedOrders;
import com.sos.joc.model.plan.PlannedOrdersFilter;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IPlannedOrdersResource;

@Path("orders")
public class PlannedOrdersImpl extends JOCResourceImpl implements IPlannedOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannedOrdersImpl.class);
    private static final int SUCCESSFUL = 0;
    private static final int SUCCESSFUL_LATE = 1;
    private static final int INCOMPLETE = 6;
    private static final int INCOMPLETE_LATE = 5;
    private static final int FAILED = 2;
    private static final int PLANNED_LATE = 5;
    private static final Integer PLANNED = 4;
    private static final String API_CALL = "./orders/plan";

    private PlannedOrderItem createPlanItem(DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory) {

        PlannedOrderItem p = new PlannedOrderItem();
        p.setLate(dbItemDailyPlanWithHistory.isLate());
        Period period = new Period();
        period.setBegin(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPeriodBegin());
        period.setEnd(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPeriodEnd());
        period.setRepeat(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getRepeatInterval());
        p.setPeriod(period);

        p.setPlannedStartTime(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPlannedStart());
        p.setExpectedEndTime(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getExpectedEnd());

        PlannedOrderState plannedOrderState = new PlannedOrderState();

        if (PlannedOrderStateText.FAILED.name().equalsIgnoreCase(dbItemDailyPlanWithHistory.getState())) {
            plannedOrderState.set_text(PlannedOrderStateText.FAILED);
            plannedOrderState.setSeverity(FAILED);
        }

        if (PlannedOrderStateText.PLANNED.name().equalsIgnoreCase(dbItemDailyPlanWithHistory.getState())) {
            plannedOrderState.set_text(PlannedOrderStateText.PLANNED);
            if (dbItemDailyPlanWithHistory.isLate()) {
                plannedOrderState.setSeverity(PLANNED_LATE);
            } else {
                plannedOrderState.setSeverity(PLANNED);
            }
        }

        if (PlannedOrderStateText.INCOMPLETE.name().equalsIgnoreCase(dbItemDailyPlanWithHistory.getState())) {
            plannedOrderState.set_text(PlannedOrderStateText.INCOMPLETE);
            if (dbItemDailyPlanWithHistory.isLate()) {
                plannedOrderState.setSeverity(INCOMPLETE_LATE);
            } else {
                plannedOrderState.setSeverity(INCOMPLETE);
            }
        }
        if (PlannedOrderStateText.SUCCESSFUL.name().equalsIgnoreCase(dbItemDailyPlanWithHistory.getState())) {
            plannedOrderState.set_text(PlannedOrderStateText.SUCCESSFUL);
            if (dbItemDailyPlanWithHistory.isLate()) {
                plannedOrderState.setSeverity(SUCCESSFUL_LATE);
            } else {
                plannedOrderState.setSeverity(SUCCESSFUL);
            }
        }
        p.setState(plannedOrderState);
        p.setSurveyDate(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getCreated());

        return p;

    }

    @Override
    public JOCDefaultResponse postPlan(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            boolean withFolderFilter = plannedOrdersFilter.getFolders() != null && !plannedOrdersFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            List<Folder> folders = addPermittedFolder(plannedOrdersFilter.getFolders());

            Globals.beginTransaction(sosHibernateSession);

            Date fromDate = null;
            Date toDate = null;

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setJobSchedulerId(plannedOrdersFilter.getJobschedulerId());
            filter.setWorkflow(plannedOrdersFilter.getWorkflow());
            filter.setOrderTemplateName(plannedOrdersFilter.getOrderId());
            if (plannedOrdersFilter.getDateFrom() != null) {
                fromDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateFrom(), plannedOrdersFilter.getTimeZone());
                filter.setPlannedStartFrom(fromDate);
            }
            if (plannedOrdersFilter.getDateTo() != null) {
                toDate = JobSchedulerDate.getDateTo(plannedOrdersFilter.getDateTo(), plannedOrdersFilter.getTimeZone());
                filter.setPlannedStartTo(toDate);
            }
            filter.setLate(plannedOrdersFilter.getLate());

            for (PlannedOrderStateText state : plannedOrdersFilter.getStates()) {
                filter.addState(state.name().toLowerCase());
            }

            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                filter.addFolderPaths(new HashSet<Folder>(folders));
            }

            Matcher regExMatcher = null;
            if (plannedOrdersFilter.getRegex() != null && !plannedOrdersFilter.getRegex().isEmpty()) {
                plannedOrdersFilter.setRegex(SearchStringHelper.getRegexValue(plannedOrdersFilter.getRegex()));
                regExMatcher = Pattern.compile(plannedOrdersFilter.getRegex()).matcher("");
            }

            ArrayList<PlannedOrderItem> result = new ArrayList<PlannedOrderItem>();
            PlannedOrders entity = new PlannedOrders();

            if (hasPermission) {
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanWithHistoryList(filter, 0);
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                    boolean add = true;
                    PlannedOrderItem p = createPlanItem(dbItemDailyPlanWithHistory);
                    p.setStartMode(dbItemDailyPlanWithHistory.getStartMode());

                    if (regExMatcher != null) {
                        regExMatcher.reset(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getWorkflow() + "," + dbItemDailyPlanWithHistory
                                .getDbItemDailyPlannedOrders().getOrderTemplateName());
                        add = regExMatcher.find();
                    }

                    p.setWorkflow(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getWorkflow());
                    p.setOrderId(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getOrderKey());

                    if (dbItemDailyPlanWithHistory.getDbItemOrder() != null) {
                        p.setStartTime(dbItemDailyPlanWithHistory.getDbItemOrder().getStartTime());
                        p.setEndTime(dbItemDailyPlanWithHistory.getDbItemOrder().getEndTime());
                        p.setHistoryId(String.valueOf(dbItemDailyPlanWithHistory.getDbItemOrder().getId()));
                    }

                    if (add) {
                        result.add(p);
                    }
                }
            }

            entity.setPlannedOrderItems(result);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
