package com.sos.joc.dailyplan.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.resource.IDailyPlanHistoryResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DailyPlanHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.history.MainRequest;
import com.sos.joc.model.dailyplan.history.MainResponse;
import com.sos.joc.model.dailyplan.history.SubmissionsOrdersRequest;
import com.sos.joc.model.dailyplan.history.SubmissionsOrdersResponse;
import com.sos.joc.model.dailyplan.history.SubmissionsRequest;
import com.sos.joc.model.dailyplan.history.SubmissionsResponse;
import com.sos.joc.model.dailyplan.history.items.ControllerItem;
import com.sos.joc.model.dailyplan.history.items.DateItem;
import com.sos.joc.model.dailyplan.history.items.OrderItem;
import com.sos.joc.model.dailyplan.history.items.SubmissionItem;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanHistoryImpl extends JOCResourceImpl implements IDailyPlanHistoryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHistoryImpl.class);
    private static final int DEFAULT_LIMIT = 5_000;
    private static final String PREFIX_ERROR = "ERROR:";
    private static final String PREFIX_WARN = "WARN:";

    @Override
    public JOCDefaultResponse postDates(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_MAIN, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, MainRequest.class);
            MainRequest in = Globals.objectMapper.readValue(inBytes, MainRequest.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }

            JOCDefaultResponse response = initPermissions(controllerId, permitted);
            if (response != null) {
                return response;
            }

            Date dateFrom = toUTCDate(in.getDateFrom());
            Date dateTo = toUTCDate(in.getDateTo());
            Map<Date, DateItem> map = new HashMap<>();

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_MAIN);
            DailyPlanHistoryDBLayer dbLayer = new DailyPlanHistoryDBLayer(session);
            List<Object[]> result = dbLayer.getDates(allowedControllers, dateFrom, dateTo, in.getSubmitted(), getLimit(in.getLimit()));

            for (int i = 0; i < result.size(); i++) {
                Object[] o = (Object[]) result.get(i);
                Date date = (Date) o[0];

                ControllerItem ci = new ControllerItem();
                ci.setControllerId((String) o[1]);
                ci.setCountTotal((Long) o[2]);
                if (in.getSubmitted() == null) {// submitted/not submitted
                    ci.setCountSubmitted(dbLayer.getCountSubmitted(ci.getControllerId(), date, null));
                } else if (in.getSubmitted()) {// only submitted
                    ci.setCountSubmitted(ci.getCountTotal());
                } else {// only not submitted
                    ci.setCountSubmitted(0L);
                }

                DateItem di = null;
                if (map.containsKey(date)) {
                    di = map.get(date);
                    di.setCountTotal(di.getCountTotal() + ci.getCountTotal());
                    di.setCountSubmitted(di.getCountSubmitted() + ci.getCountSubmitted());
                    di.getControllers().add(ci);
                    di.getControllers().sort((e1, e2) -> e1.getControllerId().compareTo(e2.getControllerId()));
                } else {
                    di = new DateItem();
                    di.setDate(date);
                    di.setCountTotal(ci.getCountTotal());
                    di.setCountSubmitted(ci.getCountSubmitted());
                    di.setControllers(new ArrayList<>());
                    di.getControllers().add(ci);
                }
                map.put(date, di);
            }
            session.close();
            session = null;

            MainResponse answer = new MainResponse();
            answer.setDeliveryDate(new Date());
            // sorting by the client
            answer.setDates(map.values().stream().collect(Collectors.toList()));
            // descending sort
            // answer.setDates(map.values().stream().sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate())).collect(Collectors.toList()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public JOCDefaultResponse postSubmissions(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_SUBMISSIONS, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, SubmissionsRequest.class);
            SubmissionsRequest in = Globals.objectMapper.readValue(inBytes, SubmissionsRequest.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getControllerPermissions(in.getControllerId(), accessToken)
                    .getOrders().getView());
            if (response != null) {
                return response;
            }

            checkRequiredParameter("controllerId", in.getControllerId());
            checkRequiredParameter("date", in.getDate());

            Date date = toUTCDate(in.getDate());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SUBMISSIONS);
            DailyPlanHistoryDBLayer dbLayer = new DailyPlanHistoryDBLayer(session);
            List<Object[]> result = dbLayer.getSubmissions(in.getControllerId(), date, in.getSubmitted(), getLimit(in.getLimit()));

            SubmissionsResponse answer = new SubmissionsResponse();
            answer.setDeliveryDate(new Date());
            if (result != null) {
                answer.setSubmissionTimes(result.stream().map(e -> {
                    SubmissionItem item = new SubmissionItem();
                    item.setSubmissionTime((Date) e[0]);
                    item.setCountTotal((Long) e[1]);

                    if (in.getSubmitted() == null) {// submitted/not submitted
                        try {
                            item.setCountSubmitted(dbLayer.getCountSubmitted(in.getControllerId(), date, item.getSubmissionTime()));
                        } catch (SOSHibernateException e1) {
                            LOGGER.error(String.format("[%s][%s][%s]%s", in.getControllerId(), date, item.getSubmissionTime(), e.toString()), e);
                        }
                    } else if (in.getSubmitted()) {// only submitted
                        item.setCountSubmitted(item.getCountTotal());
                    } else {// only not submitted
                        item.setCountSubmitted(0L);
                    }
                    return item;
                }).collect(Collectors.toList()));  // sorting by the client
                // descending sort
                // .sorted((e1, e2) -> e2.getSubmissionTime().compareTo(e1.getSubmissionTime())).collect(Collectors.toList()));
            }
            session.close();
            session = null;

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public JOCDefaultResponse postSubmissionsOrders(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_SUBMISSIONS, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, SubmissionsOrdersRequest.class);
            SubmissionsOrdersRequest in = Globals.objectMapper.readValue(inBytes, SubmissionsOrdersRequest.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getControllerPermissions(in.getControllerId(), accessToken)
                    .getOrders().getView());
            if (response != null) {
                return response;
            }

            checkRequiredParameter("controllerId", in.getControllerId());
            checkRequiredParameter("date", in.getDate());
            checkRequiredParameter("submissionTime", in.getSubmissionTime());

            Date date = toUTCDate(in.getDate());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SUBMISSIONS);
            DailyPlanHistoryDBLayer dbLayer = new DailyPlanHistoryDBLayer(session);
            List<DBItemDailyPlanHistory> result = dbLayer.getSubmissionOrders(in.getControllerId(), date, in.getSubmissionTime(), in.getSubmitted(),
                    getLimit(in.getLimit()));
            session.close();
            session = null;

            final Set<Folder> permittedFolders = addPermittedFolder(null);
            Map<String, Boolean> checkedFolders = new HashMap<>();
            SubmissionsOrdersResponse answer = new SubmissionsOrdersResponse();
            answer.setDeliveryDate(new Date());
            answer.setWarnMessages(new ArrayList<>());
            answer.setErrorMessages(new ArrayList<>());
            if (result != null) {
                answer.setOrders(result.stream().map(e -> {
                    OrderItem item = new OrderItem();
                    item.setOrderId(e.getOrderId());
                    item.setPermitted(isPermitted(e, permittedFolders, checkedFolders));
                    if (item.getPermitted()) {
                        item.setScheduledFor(e.getScheduledFor());
                        item.setSubmitted(e.isSubmitted());
                        item.setWorkflowPath(e.getWorkflowPath());
                        if (!SOSString.isEmpty(e.getMessage())) {
                            if (e.getMessage().startsWith(PREFIX_WARN)) {
                                answer.getWarnMessages().add(e.getMessage().substring(PREFIX_WARN.length()));
                            } else if (e.getMessage().startsWith(PREFIX_ERROR)) {
                                answer.getErrorMessages().add(e.getMessage().substring(PREFIX_ERROR.length()));
                            } else {
                                answer.getErrorMessages().add(e.getMessage());
                            }
                        }
                    }
                    return item;
                }).collect(Collectors.toList())); // sorted by the client
                // ascending sort
                // .sorted((e1, e2) -> e1.getScheduledFor().compareTo(e2.getScheduledFor()))
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private Date toUTCDate(String date) {
        if (date == null) {
            return null;
        }
        if (date.length() == 10) {
            date = date + "T00:00:00Z";
        } else if (date.equals("0")) {
            date = "0d";
        }
        return JobSchedulerDate.getDateFrom(date, "UTC");
    }

    private int getLimit(Integer limit) {
        return limit == null || limit < 0 ? DEFAULT_LIMIT : limit.intValue();
    }

    private boolean isPermitted(DBItemDailyPlanHistory item, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        Boolean result = checkedFolders.get(item.getWorkflowFolder());
        if (result == null) {
            result = canAdd(item.getWorkflowPath(), permittedFolders);
            checkedFolders.put(item.getWorkflowFolder(), result);
        }
        return result;
    }

}
