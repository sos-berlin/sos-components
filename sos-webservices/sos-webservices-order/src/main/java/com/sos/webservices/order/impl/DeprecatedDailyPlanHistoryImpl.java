package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanHistory;
import com.sos.joc.model.dailyplan.DailyPlanHistoryControllerItem;
import com.sos.joc.model.dailyplan.DailyPlanHistoryDateItem;
import com.sos.joc.model.dailyplan.DailyPlanHistoryFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionTimes;
import com.sos.joc.model.dailyplan.DailyplanHistoryOrderItem;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.FilterDailyPlanHistory;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.DeprecatedIDailyPlanHistoryResource;

/** see com.sos.joc.dailyplan.impl.DailyPlanHistoryImpl */
@Deprecated
@Path("daily_plan")
public class DeprecatedDailyPlanHistoryImpl extends JOCOrderResourceImpl implements DeprecatedIDailyPlanHistoryResource {

    class ControllerDateKey {

        Date dailyPlanDate;
        String controllerId;

        public Date getDailyPlanDate() {
            return dailyPlanDate;
        }

        public void setDailyPlanDate(Date dailyPlanDate) {
            this.dailyPlanDate = dailyPlanDate;
        }

        public String getControllerId() {
            return controllerId;
        }

        public void setControllerId(String controllerId) {
            this.controllerId = controllerId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((controllerId == null) ? 0 : controllerId.hashCode());
            result = prime * result + ((dailyPlanDate == null) ? 0 : dailyPlanDate.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ControllerDateKey other = (ControllerDateKey) obj;
            if (controllerId == null) {
                if (other.controllerId != null)
                    return false;
            } else if (!controllerId.equals(other.controllerId))
                return false;
            if (dailyPlanDate == null) {
                if (other.dailyPlanDate != null)
                    return false;
            } else if (!dailyPlanDate.equals(other.dailyPlanDate))
                return false;
            return true;
        }
    }

    class SubmissionControllerDateKey {

        Date submissionTime;
        Date dailyPlanDate;
        String controllerId;

        public Date getSubmissionTime() {
            return submissionTime;
        }

        public void setSubmissionTime(Date submissionTime) {
            this.submissionTime = submissionTime;
        }

        public Date getDailyPlanDate() {
            return dailyPlanDate;
        }

        public void setDailyPlanDate(Date dailyPlanDate) {
            this.dailyPlanDate = dailyPlanDate;
        }

        public String getControllerId() {
            return controllerId;
        }

        public void setControllerId(String controllerId) {
            this.controllerId = controllerId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((controllerId == null) ? 0 : controllerId.hashCode());
            result = prime * result + ((dailyPlanDate == null) ? 0 : dailyPlanDate.hashCode());
            result = prime * result + ((submissionTime == null) ? 0 : submissionTime.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SubmissionControllerDateKey other = (SubmissionControllerDateKey) obj;
            if (controllerId == null) {
                if (other.controllerId != null)
                    return false;
            } else if (!controllerId.equals(other.controllerId))
                return false;
            if (dailyPlanDate == null) {
                if (other.dailyPlanDate != null)
                    return false;
            } else if (!dailyPlanDate.equals(other.dailyPlanDate))
                return false;
            if (submissionTime == null) {
                if (other.submissionTime != null)
                    return false;
            } else if (!submissionTime.equals(other.submissionTime))
                return false;
            return true;
        }
    }

    private static final int DEFAULT_LIMIT = 10000;
    private static final String ERROR = "ERROR:";
    private static final String WARN = "WARN:";
    private static final String API_CALL = "./daily_plan/history";

    @Override
    public JOCDefaultResponse postDailyPlanHistory(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);
            DailyPlanHistoryFilter dailyPlanHistoryFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanHistoryFilter.class);

            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanHistoryFilter.getControllerId(), dailyPlanHistoryFilter
                    .getFilter().getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getView()).collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);

            Globals.beginTransaction(sosHibernateSession);

            DailyPlanHistory dailyPlanHistory = new DailyPlanHistory();
            dailyPlanHistory.setDailyPlans(new ArrayList<DailyPlanHistoryDateItem>());

            FilterDailyPlanHistory filter = new FilterDailyPlanHistory();
            if (dailyPlanHistoryFilter.getFilter() != null) {

                filter.setSubmitted(dailyPlanHistoryFilter.getFilter().getSubmitted());
                if ((dailyPlanHistoryFilter.getFilter().getDateTo() != null) && (dailyPlanHistoryFilter.getFilter().getDateFrom() == null)) {
                    String d = dailyPlanHistoryFilter.getFilter().getDateTo();
                    if (d.length() == 10) {
                        d = d + "T00:00:00";
                    }

                    Date date = JobSchedulerDate.getDateFrom(d, dailyPlanHistoryFilter.getTimeZone());
                    filter.setDailyPlanDate(date);
                } else {

                    if (dailyPlanHistoryFilter.getFilter().getDateTo() != null) {
                        String d = dailyPlanHistoryFilter.getFilter().getDateTo();
                        if (d.length() == 10) {
                            d = d + "T00:00:00";
                        }
                        Date toDate = JobSchedulerDate.getDateTo(d, dailyPlanHistoryFilter.getTimeZone());
                        filter.setDailyPlanDateTo(toDate);
                    }
                    if (dailyPlanHistoryFilter.getFilter().getDateFrom() != null) {
                        String d = dailyPlanHistoryFilter.getFilter().getDateFrom();
                        if (d.length() == 10) {
                            d = d + "T00:00:00";
                        }
                        Date fromDate = JobSchedulerDate.getDateFrom(d, dailyPlanHistoryFilter.getTimeZone());
                        filter.setDailyPlanDateFrom(fromDate);
                    }
                }
            }

            filter.setSortMode("desc");
            filter.setOrderCriteria("order_id,scheduled_for");

            int limit = DEFAULT_LIMIT;
            if (dailyPlanHistoryFilter.getFilter().getLimit() != 0) {
                limit = dailyPlanHistoryFilter.getFilter().getLimit();
            }

            boolean haveEntries = true;
            if (dailyPlanHistoryFilter.getFilter().getAuditLogId() != null) {
                List<String> orderIds = dbLayerDailyPlanHistory.getOrderIdsByAuditLog(dailyPlanHistoryFilter.getFilter().getAuditLogId());
                if (orderIds.isEmpty()) {
                    haveEntries = false;
                } else {
                    filter.addListOfOrderIds(orderIds);
                }
            }
            if (haveEntries) {
                for (String controllerId : allowedControllers) {
                    folderPermissions.setSchedulerId(controllerId);
                    filter.setControllerId(controllerId);
                    Set<Folder> permittedFolders = folderPermissions.getListOfFolders(controllerId);
                    if (permittedFolders.size() > 0) {
                        filter.addFolder(permittedFolders);
                    }

                    List<DBItemDailyPlanHistory> listOfDailyPlanHistory = dbLayerDailyPlanHistory.getDailyPlanHistory(filter, limit);

                    Map<Date, DailyPlanHistoryDateItem> mapOfHistoryDateItems = new HashMap<Date, DailyPlanHistoryDateItem>();
                    Map<ControllerDateKey, DailyPlanHistoryControllerItem> mapOfControllerItems =
                            new HashMap<ControllerDateKey, DailyPlanHistoryControllerItem>();
                    Map<SubmissionControllerDateKey, DailyPlanSubmissionTimes> mapOfSubmissionTimesItems =
                            new HashMap<SubmissionControllerDateKey, DailyPlanSubmissionTimes>();

                    for (DBItemDailyPlanHistory dbItemDailyPlanHistory : listOfDailyPlanHistory) {

                        if (mapOfHistoryDateItems.get(dbItemDailyPlanHistory.getDailyPlanDate()) == null) {
                            DailyPlanHistoryDateItem dailyPlanHistoryDateItem = new DailyPlanHistoryDateItem();
                            dailyPlanHistoryDateItem.setControllers(new ArrayList<DailyPlanHistoryControllerItem>());
                            dailyPlanHistoryDateItem.setDailyPlanDate(dbItemDailyPlanHistory.getDailyPlanDate());
                            mapOfHistoryDateItems.put(dbItemDailyPlanHistory.getDailyPlanDate(), dailyPlanHistoryDateItem);
                            dailyPlanHistory.getDailyPlans().add(dailyPlanHistoryDateItem);
                        }

                        DailyPlanHistoryDateItem dailyPlanHistoryDateItem = mapOfHistoryDateItems.get(dbItemDailyPlanHistory.getDailyPlanDate());

                        ControllerDateKey controllerDateKey = new ControllerDateKey();
                        controllerDateKey.setControllerId(dbItemDailyPlanHistory.getControllerId());
                        controllerDateKey.setDailyPlanDate(dbItemDailyPlanHistory.getDailyPlanDate());

                        if (mapOfControllerItems.get(controllerDateKey) == null) {

                            DailyPlanHistoryControllerItem dailyPlanHistoryControllerItem = new DailyPlanHistoryControllerItem();
                            dailyPlanHistoryControllerItem.setControllerId(dbItemDailyPlanHistory.getControllerId());
                            dailyPlanHistoryControllerItem.setSubmissions(new ArrayList<DailyPlanSubmissionTimes>());
                            mapOfControllerItems.put(controllerDateKey, dailyPlanHistoryControllerItem);
                            dailyPlanHistoryDateItem.getControllers().add(dailyPlanHistoryControllerItem);
                        }

                        DailyPlanHistoryControllerItem dailyPlanHistoryControllerItem = mapOfControllerItems.get(controllerDateKey);

                        SubmissionControllerDateKey submissionControllerDateKey = new SubmissionControllerDateKey();
                        submissionControllerDateKey.setControllerId(dbItemDailyPlanHistory.getControllerId());
                        submissionControllerDateKey.setDailyPlanDate(dbItemDailyPlanHistory.getDailyPlanDate());
                        submissionControllerDateKey.setSubmissionTime(dbItemDailyPlanHistory.getSubmissionTime());

                        if (mapOfSubmissionTimesItems.get(submissionControllerDateKey) == null) {
                            DailyPlanSubmissionTimes dailyPlanSubmissionTimes = new DailyPlanSubmissionTimes();
                            dailyPlanSubmissionTimes.setSubmissionTime(dbItemDailyPlanHistory.getSubmissionTime());
                            dailyPlanSubmissionTimes.setErrorMessages(new ArrayList<String>());
                            dailyPlanSubmissionTimes.setOrderIds(new ArrayList<DailyplanHistoryOrderItem>());
                            dailyPlanSubmissionTimes.setWarnMessages(new ArrayList<String>());
                            mapOfSubmissionTimesItems.put(submissionControllerDateKey, dailyPlanSubmissionTimes);
                            dailyPlanHistoryControllerItem.getSubmissions().add(dailyPlanSubmissionTimes);
                        }

                        DailyPlanSubmissionTimes dailyPlanSubmissionTimes = mapOfSubmissionTimesItems.get(submissionControllerDateKey);

                        DailyplanHistoryOrderItem dailyplanHistoryOrderItem = new DailyplanHistoryOrderItem();
                        dailyplanHistoryOrderItem.setOrderId(dbItemDailyPlanHistory.getOrderId());
                        dailyplanHistoryOrderItem.setScheduledFor(dbItemDailyPlanHistory.getScheduledFor());
                        dailyplanHistoryOrderItem.setSubmitted(dbItemDailyPlanHistory.isSubmitted());
                        dailyplanHistoryOrderItem.setWorkflowPath(dbItemDailyPlanHistory.getWorkflowPath());

                        if (dbItemDailyPlanHistory.getMessage() != null) {
                            if (dbItemDailyPlanHistory.getMessage().startsWith(WARN)) {
                                dailyPlanSubmissionTimes.getWarnMessages().add(dbItemDailyPlanHistory.getMessage().substring(WARN.length()));
                            } else {
                                if (dbItemDailyPlanHistory.getMessage().startsWith(ERROR)) {
                                    dailyPlanSubmissionTimes.getErrorMessages().add(dbItemDailyPlanHistory.getMessage().substring(ERROR.length()));
                                } else {
                                    dailyPlanSubmissionTimes.getErrorMessages().add(dbItemDailyPlanHistory.getMessage());
                                }
                            }
                        }
                        dailyPlanSubmissionTimes.getOrderIds().add(dailyplanHistoryOrderItem);
                        mapOfSubmissionTimesItems.put(submissionControllerDateKey, dailyPlanSubmissionTimes);
                    }
                }
            }

            dailyPlanHistory.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(dailyPlanHistory);

        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
