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
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanHistory;
import com.sos.joc.model.dailyplan.DailyPlanHistoryControllerItem;
import com.sos.joc.model.dailyplan.DailyPlanHistoryDateItem;
import com.sos.joc.model.dailyplan.DailyPlanHistoryFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionTimes;
import com.sos.joc.model.dailyplan.DailyplanHistoryOrderItem;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlanHistory;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanHistoryResource;

@Path("daily_plan")

public class DailyPlanHistoryImpl extends JOCOrderResourceImpl implements IDailyPlanHistoryResource {

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
                    Date date = JobSchedulerDate.getDateFrom(dailyPlanHistoryFilter.getFilter().getDateTo(), dailyPlanHistoryFilter.getTimeZone());
                    filter.setDailyPlanDate(date);
                } else {

                    if (dailyPlanHistoryFilter.getFilter().getDateTo() != null) {
                        Date toDate = JobSchedulerDate.getDateTo(dailyPlanHistoryFilter.getFilter().getDateTo(), dailyPlanHistoryFilter
                                .getTimeZone());
                        filter.setDailyPlanDateTo(toDate);
                    }
                    if (dailyPlanHistoryFilter.getFilter().getDateFrom() != null) {
                        Date fromDate = JobSchedulerDate.getDateFrom(dailyPlanHistoryFilter.getFilter().getDateFrom(), dailyPlanHistoryFilter
                                .getTimeZone());
                        filter.setDailyPlanDateFrom(fromDate);
                    }
                }
            }

            filter.setSortMode("desc");
            filter.setOrderCriteria("id");
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

                    List<DBItemDailyPlanHistory> listOfDailyPlanHistory = dbLayerDailyPlanHistory.getDailyPlanHistory(filter, 0);

                    Map<Date, DailyPlanHistoryDateItem> mapOfHistoryDateItems = new HashMap<Date, DailyPlanHistoryDateItem>();
                    Map<ControllerDateKey, DailyPlanHistoryControllerItem> mapOfControllerItems =
                            new HashMap<ControllerDateKey, DailyPlanHistoryControllerItem>();
                    Map<SubmissionControllerDateKey, DailyPlanSubmissionTimes> mapOfSubmissionTimesItems =
                            new HashMap<SubmissionControllerDateKey, DailyPlanSubmissionTimes>();

                    for (DBItemDailyPlanHistory dbItemDailySubmissionHistory : listOfDailyPlanHistory) {

                        if (mapOfHistoryDateItems.get(dbItemDailySubmissionHistory.getDailyPlanDate()) == null) {
                            DailyPlanHistoryDateItem dailyPlanHistoryDateItem = new DailyPlanHistoryDateItem();
                            dailyPlanHistoryDateItem.setControllers(new ArrayList<DailyPlanHistoryControllerItem>());
                            dailyPlanHistoryDateItem.setDailyPlanDate(dbItemDailySubmissionHistory.getDailyPlanDate());
                            mapOfHistoryDateItems.put(dbItemDailySubmissionHistory.getDailyPlanDate(), dailyPlanHistoryDateItem);
                            dailyPlanHistory.getDailyPlans().add(dailyPlanHistoryDateItem);
                        }

                        DailyPlanHistoryDateItem dailyPlanHistoryDateItem = mapOfHistoryDateItems.get(dbItemDailySubmissionHistory
                                .getDailyPlanDate());

                        ControllerDateKey controllerDateKey = new ControllerDateKey();
                        controllerDateKey.setControllerId(dbItemDailySubmissionHistory.getControllerId());
                        controllerDateKey.setDailyPlanDate(dbItemDailySubmissionHistory.getDailyPlanDate());

                        if (mapOfControllerItems.get(controllerDateKey) == null) {

                            DailyPlanHistoryControllerItem dailyPlanHistoryControllerItem = new DailyPlanHistoryControllerItem();
                            dailyPlanHistoryControllerItem.setControllerId(dbItemDailySubmissionHistory.getControllerId());
                            dailyPlanHistoryControllerItem.setSubmissions(new ArrayList<DailyPlanSubmissionTimes>());
                            mapOfControllerItems.put(controllerDateKey, dailyPlanHistoryControllerItem);
                            dailyPlanHistoryDateItem.getControllers().add(dailyPlanHistoryControllerItem);
                        }

                        DailyPlanHistoryControllerItem dailyPlanHistoryControllerItem = mapOfControllerItems.get(controllerDateKey);

                        SubmissionControllerDateKey submissionControllerDateKey = new SubmissionControllerDateKey();
                        submissionControllerDateKey.setControllerId(dbItemDailySubmissionHistory.getControllerId());
                        submissionControllerDateKey.setDailyPlanDate(dbItemDailySubmissionHistory.getDailyPlanDate());
                        submissionControllerDateKey.setSubmissionTime(dbItemDailySubmissionHistory.getSubmissionTime());

                        if (mapOfSubmissionTimesItems.get(submissionControllerDateKey) == null) {
                            DailyPlanSubmissionTimes dailyPlanSubmissionTimes = new DailyPlanSubmissionTimes();
                            dailyPlanSubmissionTimes.setSubmissionTime(dbItemDailySubmissionHistory.getSubmissionTime());
                            dailyPlanSubmissionTimes.setErrorMessages(new ArrayList<String>());
                            dailyPlanSubmissionTimes.setOrderIds(new ArrayList<DailyplanHistoryOrderItem>());
                            dailyPlanSubmissionTimes.setWarnMessages(new ArrayList<String>());
                            mapOfSubmissionTimesItems.put(submissionControllerDateKey, dailyPlanSubmissionTimes);
                            dailyPlanHistoryControllerItem.getSubmissions().add(dailyPlanSubmissionTimes);
                        }

                        DailyPlanSubmissionTimes dailyPlanSubmissionTimes = mapOfSubmissionTimesItems.get(submissionControllerDateKey);

                        DailyplanHistoryOrderItem dailyplanHistoryOrderItem = new DailyplanHistoryOrderItem();
                        dailyplanHistoryOrderItem.setOrderId(dbItemDailySubmissionHistory.getOrderId());
                        dailyplanHistoryOrderItem.setScheduledFor(dbItemDailySubmissionHistory.getScheduledFor());
                        dailyplanHistoryOrderItem.setSubmitted(dbItemDailySubmissionHistory.isSubmitted());
                        dailyplanHistoryOrderItem.setWorkflowPath(dbItemDailySubmissionHistory.getWorkflowPath());

                        if (dbItemDailySubmissionHistory.getMessage() != null) {
                            if (dbItemDailySubmissionHistory.getMessage().startsWith(WARN)) {
                                dailyPlanSubmissionTimes.getWarnMessages().add(dbItemDailySubmissionHistory.getMessage().substring(WARN.length()));
                            }
                            if (dbItemDailySubmissionHistory.getMessage().startsWith(ERROR)) {
                                dailyPlanSubmissionTimes.getErrorMessages().add(dbItemDailySubmissionHistory.getMessage().substring(ERROR.length()));
                            }
                        } else {
                            dailyPlanSubmissionTimes.getOrderIds().add(dailyplanHistoryOrderItem);
                        }

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
