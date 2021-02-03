package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanHistory;
import com.sos.joc.model.dailyplan.DailyPlanHistoryFilter;
import com.sos.joc.model.dailyplan.DailyPlanHistoryItem;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionTimes;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.FilterDailyPlanHistory;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanHistoryResource;

@Path("daily_plan")
public class DailyPlanHistoryImpl extends JOCResourceImpl implements IDailyPlanHistoryResource {

    private static final String ERROR = "ERROR:";
    private static final String WARN = "WARN:";
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHistoryImpl.class);
    private static final String API_CALL = "./daily_plan/history";

    @Override
    public JOCDefaultResponse postDailyPlanHistory(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);
            DailyPlanHistoryFilter dailyPlanHistoryFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanHistoryFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanHistoryFilter, accessToken, getControllerId(accessToken,
                    dailyPlanHistoryFilter.getControllerId()), getPermissonsJocCockpit(dailyPlanHistoryFilter.getControllerId(), accessToken)
                            .getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlanHistory filter = new FilterDailyPlanHistory();
            filter.setControllerId(dailyPlanHistoryFilter.getControllerId());

             if (dailyPlanHistoryFilter.getFilter() != null) {
 
                filter.setSubmitted(dailyPlanHistoryFilter.getFilter().getSubmitted());
                if ((dailyPlanHistoryFilter.getFilter().getDateTo() != null) && (dailyPlanHistoryFilter.getFilter().getDateFrom() == null)) {
                    Date date = JobSchedulerDate.getDateFrom(dailyPlanHistoryFilter.getFilter().getDateFrom(), dailyPlanHistoryFilter.getTimeZone());
                    filter.setDailyPlanDate(date);
                } else {

                    if (dailyPlanHistoryFilter.getFilter().getDateTo() != null) {
                        Date toDate = JobSchedulerDate.getDateTo(dailyPlanHistoryFilter.getFilter().getDateTo(), dailyPlanHistoryFilter
                                .getTimeZone());
                        filter.setDailyPlanDateTo(toDate);
                    }
                    if (dailyPlanHistoryFilter.getFilter().getDateFrom() != null) {
                        Date toDate = JobSchedulerDate.getDateTo(dailyPlanHistoryFilter.getFilter().getDateTo(), dailyPlanHistoryFilter
                                .getTimeZone());
                        filter.setDailyPlanDateFrom(toDate);
                    }
                }
            }

            filter.setSortMode("desc");
            filter.setOrderCriteria("id");
            DailyPlanHistory dailyPlanHistory = new DailyPlanHistory();
            List<DailyPlanHistoryItem> result = new ArrayList<DailyPlanHistoryItem>();

            List<DBItemDailyPlanHistory> listOfDailyPlanSubmissions = dbLayerDailyPlanHistory.getDailyPlanHistory(filter, 0);
            Map<Date, Map<Date, DailyPlanSubmissionTimes>> mapOfSubmissionTimesByDate = new HashMap<Date, Map<Date, DailyPlanSubmissionTimes>>();

            for (DBItemDailyPlanHistory dbItemDailySubmissionHistory : listOfDailyPlanSubmissions) {

                if (mapOfSubmissionTimesByDate.get(dbItemDailySubmissionHistory.getDailyPlanDate()) == null) {
                    Map<Date, DailyPlanSubmissionTimes> mapOfSubmissionTimes = new HashMap<Date, DailyPlanSubmissionTimes>();
                    mapOfSubmissionTimesByDate.put(dbItemDailySubmissionHistory.getDailyPlanDate(), mapOfSubmissionTimes);
                }
                Map<Date, DailyPlanSubmissionTimes> mapOfSubmissionTimes = mapOfSubmissionTimesByDate.get(dbItemDailySubmissionHistory
                        .getDailyPlanDate());

                if (mapOfSubmissionTimes.get(dbItemDailySubmissionHistory.getSubmissionTime()) == null) {

                    DailyPlanSubmissionTimes p = new DailyPlanSubmissionTimes();
                    p.setSubmissionTime(dbItemDailySubmissionHistory.getSubmissionTime());
                    p.setErrorMessages(new ArrayList<String>());
                    p.setOrderIds(new ArrayList<String>());
                    p.setWarnMessages(new ArrayList<String>());
                    mapOfSubmissionTimes.put(dbItemDailySubmissionHistory.getSubmissionTime(), p);
                }

                DailyPlanSubmissionTimes dailyPlanSubmissionTimes = mapOfSubmissionTimes.get(dbItemDailySubmissionHistory.getSubmissionTime());
                dailyPlanSubmissionTimes.getOrderIds().add(dbItemDailySubmissionHistory.getOrderId());

                if (dbItemDailySubmissionHistory.getMessage() != null) {
                    if (dbItemDailySubmissionHistory.getMessage().startsWith(WARN)) {
                        dailyPlanSubmissionTimes.getWarnMessages().add(dbItemDailySubmissionHistory.getMessage().substring(WARN.length() - 1));
                    }
                    if (dbItemDailySubmissionHistory.getMessage().startsWith(ERROR)) {
                        dailyPlanSubmissionTimes.getErrorMessages().add(dbItemDailySubmissionHistory.getMessage().substring(ERROR.length() - 1));
                    }
                }

            }

            for (Entry<Date, Map<Date, DailyPlanSubmissionTimes>> entry : mapOfSubmissionTimesByDate.entrySet()) {

                DailyPlanHistoryItem dailyPlanHistoryItem = new DailyPlanHistoryItem();
                dailyPlanHistoryItem.setControllerId(dailyPlanHistoryFilter.getControllerId());
                dailyPlanHistoryItem.setDailyPlanDate(entry.getKey());
                dailyPlanHistoryItem.setSubmissions(new ArrayList<DailyPlanSubmissionTimes>());
                for (Entry<Date, DailyPlanSubmissionTimes> submissionTime : entry.getValue().entrySet()) {
                    dailyPlanHistoryItem.getSubmissions().add(submissionTime.getValue());
                }

                result.add(dailyPlanHistoryItem);
            }

            dailyPlanHistory.setDailyPlans(result);
            dailyPlanHistory.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(dailyPlanHistory);

        } catch (

        JocException e) {
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
