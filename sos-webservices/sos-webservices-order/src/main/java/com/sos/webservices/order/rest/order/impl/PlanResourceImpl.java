package com.sos.webservices.order.rest.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.plan.Period;
import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.PlanFilter;
import com.sos.joc.model.plan.PlanItem;
import com.sos.joc.model.plan.PlanState;
import com.sos.joc.model.plan.PlanStateText;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.rest.order.resource.IPlanResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("orders")
public class PlanResourceImpl extends JOCResourceImpl implements IPlanResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanResourceImpl.class);
    private static final int SUCCESSFUL = 0;
    private static final int SUCCESSFUL_LATE = 1;
    private static final int INCOMPLETE = 6;
    private static final int INCOMPLETE_LATE = 5;
    private static final int FAILED = 2;
    private static final int PLANNED_LATE = 5;
    private static final Integer PLANNED = 4;
    private static final String API_CALL = "./orders/plan";

    private PlanItem createPlanItem(DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory) {

        PlanItem p = new PlanItem();
        p.setLate(dbItemDailyPlanWithHistory.isLate());

        Period period = new Period();
        period.setBegin(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getPeriodBegin());
        period.setEnd(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getPeriodEnd());
        period.setRepeat(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getRepeatInterval());
        p.setPeriod(period);

        p.setPlannedStartTime(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getPlannedStart());
        p.setExpectedEndTime(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getExpectedEnd());

        PlanState planState = new PlanState();

        if (PlanStateText.FAILED.name().equals(dbItemDailyPlanWithHistory.getState())) {
            planState.set_text(PlanStateText.FAILED);
            planState.setSeverity(FAILED);
        }

        if (PlanStateText.PLANNED.name().equals(dbItemDailyPlanWithHistory.getState())) {
            planState.set_text(PlanStateText.PLANNED);
            if (dbItemDailyPlanWithHistory.isLate()) {
                planState.setSeverity(PLANNED_LATE);
            } else {
                planState.setSeverity(PLANNED);
            }
        }

        if (PlanStateText.INCOMPLETE.name().equals(dbItemDailyPlanWithHistory.getState())) {
            planState.set_text(PlanStateText.INCOMPLETE);
            if (dbItemDailyPlanWithHistory.isLate()) {
                planState.setSeverity(INCOMPLETE_LATE);
            } else {
                planState.setSeverity(INCOMPLETE);
            }
        }
        if (PlanStateText.SUCCESSFUL.name().equals(dbItemDailyPlanWithHistory.getState())) {
            planState.set_text(PlanStateText.SUCCESSFUL);
            if (dbItemDailyPlanWithHistory.isLate()) {
                planState.setSeverity(SUCCESSFUL_LATE);
            } else {
                planState.setSeverity(SUCCESSFUL);
            }
        }
        p.setState(planState);
        p.setSurveyDate(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getCreated());

        return p;

    }

    @Override
    public JOCDefaultResponse postPlan(String xAccessToken, String accessToken, PlanFilter planFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, planFilter, accessToken, planFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    planFilter.getJobschedulerId(), accessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            boolean withFolderFilter = planFilter.getFolders() != null && !planFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            List<Folder> folders = addPermittedFolder(planFilter.getFolders());

            Globals.beginTransaction(sosHibernateSession);

            Date maxDate = dbLayerDailyPlan.getMaxPlannedStart(planFilter.getJobschedulerId());
            Date fromDate = null;
            Date toDate = null;

            dbLayerDailyPlan.getFilter().setMasterId(planFilter.getJobschedulerId());
            dbLayerDailyPlan.getFilter().setWorkflow(planFilter.getJobChain());
            dbLayerDailyPlan.getFilter().setOrderName(planFilter.getOrderId());
            if (planFilter.getDateFrom() != null) {
                fromDate = JobSchedulerDate.getDateFrom(planFilter.getDateFrom(), planFilter.getTimeZone());
                dbLayerDailyPlan.getFilter().setPlannedStartFrom(fromDate);
            }
            if (planFilter.getDateTo() != null) {
                toDate = JobSchedulerDate.getDateTo(planFilter.getDateTo(), planFilter.getTimeZone());
                dbLayerDailyPlan.getFilter().setPlannedStartTo(toDate);
            }
            dbLayerDailyPlan.getFilter().setLate(planFilter.getLate());

            for (PlanStateText state : planFilter.getStates()) {
                dbLayerDailyPlan.getFilter().addState(state.name());
            }

            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                dbLayerDailyPlan.getFilter().addFolderPaths(new HashSet<Folder>(folders));
            }

            Matcher regExMatcher = null;
            if (planFilter.getRegex() != null && !planFilter.getRegex().isEmpty()) {
                planFilter.setRegex(SearchStringHelper.getRegexValue(planFilter.getRegex()));
                regExMatcher = Pattern.compile(planFilter.getRegex()).matcher("");
            }

            ArrayList<PlanItem> result = new ArrayList<PlanItem>();
            Plan entity = new Plan();

            if (hasPermission) {
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanWithHistoryList(0);
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                    boolean add = true;
                    PlanItem p = createPlanItem(dbItemDailyPlanWithHistory);
                    p.setStartMode(dbItemDailyPlanWithHistory.getStartMode());

                    if (regExMatcher != null) {
                        regExMatcher.reset(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getWorkflow() + "," + dbItemDailyPlanWithHistory
                                .getDbItemDailyPlan().getOrderName());
                        add = regExMatcher.find();
                    }

                    p.setJobChain(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getWorkflow());
                    p.setOrderId(dbItemDailyPlanWithHistory.getDbItemDailyPlan().getOrderKey());

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

            entity.setPlanItems(result);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
