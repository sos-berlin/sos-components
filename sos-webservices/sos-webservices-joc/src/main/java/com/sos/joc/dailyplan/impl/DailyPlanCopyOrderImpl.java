package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.ModifyOrdersHelper;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.OrderListSynchronizer;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanScheduleWorkflow;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.DailyPlanUtils;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.resource.IDailyPlanCopyOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.dailyplan.Cycle;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.order.OrderIdMap;
import com.sos.joc.model.order.OrderIdMap200;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanCopyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanCopyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanCopyOrderImpl.class);
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    private static final Comparator<DBItemDailyPlanOrder> comp = Comparator.comparing(DBItemDailyPlanOrder::getOrderId);

    @Override
    public JOCDefaultResponse postCopyOrder(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, DailyPlanModifyOrder.class);
            ModifyOrdersHelper in = Globals.objectMapper.readValue(filterBytes, ModifyOrdersHelper.class);
            String controllerId = in.getControllerId();

            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).map(p -> p.getOrders()
                    .getCreate()));
            if (response != null) {
                return response;
            }

            // DailyPlan Orders: orderIds.get(Boolean.FALSE), Adhoc Orders: orderIds.get(Boolean.TRUE)
            Map<Boolean, Set<String>> orderIds = in.getOrderIds().stream().collect(Collectors.groupingBy(id -> id.matches(".*#T[0-9]+-.*"), Collectors
                    .toSet()));
            orderIds.putIfAbsent(Boolean.FALSE, Collections.emptySet());
            orderIds.putIfAbsent(Boolean.TRUE, Collections.emptySet());

            // TODO mabe exception if adhoc-order in the request: !orderIds.get(Boolean.TRUE).isEmpty()

            // CategoryType category = orderIds.get(Boolean.FALSE).isEmpty() ? CategoryType.CONTROLLER : CategoryType.DAILYPLAN;
            // DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId(), category);
            DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId(), CategoryType.DAILYPLAN);

            List<DBItemDailyPlanOrder> dailyPlanOrderItems = null;
            if (!orderIds.get(Boolean.FALSE).isEmpty()) {
                dailyPlanOrderItems = getDailyPlanOrders(controllerId, DailyPlanUtils.getDistinctOrderIds(orderIds.get(Boolean.FALSE)));
            }
            if (dailyPlanOrderItems == null) {
                dailyPlanOrderItems = Collections.emptyList();
            }
            // boolean someDailyPlanOrdersAreSubmitted = dailyPlanOrderItems.stream().anyMatch(DBItemDailyPlanOrder::getSubmitted);
            // boolean onlyStarttimeModifications = hasOnlyStarttimeModifications(in);

            setSettings(IMPL_PATH);
            ZoneId zoneId = getZoneId(IMPL_PATH);
            OrderIdMap dailyPlanResult = null;

            if (!dailyPlanOrderItems.isEmpty()) {
                dailyPlanResult = modifyStartTime(in, dailyPlanOrderItems, auditlog, zoneId);
            } else {
                LOGGER.debug("0 dailyplan orders found");
            }

            OrderIdMap200 entity = new OrderIdMap200();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setOrderIds(dailyPlanResult);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<DBItemDailyPlanOrder> getDailyPlanOrders(String controllerId, List<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            return new DBLayerDailyPlannedOrders(session).getDailyPlanOrders(controllerId, orderIds);
        } finally {
            Globals.disconnect(session);
        }
    }

    private OrderIdMap modifyStartTime(ModifyOrdersHelper in, List<DBItemDailyPlanOrder> mainItems, DBItemJocAuditLog auditlog, ZoneId zoneId)
            throws Exception {
        OrderIdMap result = new OrderIdMap();

        // only for one cyclic order the period can be changed for all values such as begin, end , repeat
        if (mainItems.size() == 1 && mainItems.get(0).isCyclic() && in.getCycle() != null) {
            DBItemDailyPlanOrder cyclicOrder = mainItems.get(0);

            if (in.getScheduledFor() == null) {
                in.setScheduledFor(cyclicOrder.getDailyPlanDate(getSettings().getTimeZone(), getSettings().getPeriodBegin()));
            }
            if (!in.getScheduledFor().matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new JocBadRequestException("'scheduledFor' has to be in the form yyyy-mm-dd");
            }

            in.initScheduledFor(false);
            in.setCycle(getCycle(in.getCycle(), cyclicOrder, in.getTimeZone()));

            Optional<String> newOrderId = modifyStartTimeCycle(in, cyclicOrder, null, auditlog);
            if (newOrderId.isPresent()) {
                result.getAdditionalProperties().put(cyclicOrder.getOrderId(), newOrderId.get());
            }
        }
        // only start time can be changed, i.e. the whole period will be move for cyclic orders with unchanged repeat interval
        else {
            if (in.getScheduledFor() == null) {
                throw new JocMissingRequiredParameterException("Modify Start Time: missing start time.");
            }

            boolean isbulkOperation = mainItems.size() > 1 || mainItems.get(0).isCyclic();
            in.initScheduledFor(isbulkOperation);

            // can have multiple items - of the same schedule or workflow
            result = modifyStartTimeSingle(in, mainItems, auditlog, zoneId);
        }

        return result;
    }

    private Cycle getCycle(Cycle cycle, DBItemDailyPlanOrder cyclicOrder, String timezone) {
        Cycle _cycle = (cycle == null) ? new Cycle() : cycle;
        if (_cycle.getBegin() == null) {
            _cycle.setBegin(getPeriodBeginEnd(cyclicOrder.getPeriodBegin(), timezone));
        }
        if (_cycle.getEnd() == null) {
            _cycle.setEnd(getPeriodBeginEnd(cyclicOrder.getPeriodEnd(), timezone));
        }
        if (_cycle.getRepeat() == null) {
            _cycle.setRepeat(getPeriodRepeat(cyclicOrder.getRepeatInterval()));
        }
        return _cycle;
    }

    private static String getPeriodBeginEnd(Date date, String timeZone) {
        timeFormatter.setTimeZone(TimeZone.getTimeZone(timeZone == null ? SOSDate.TIMEZONE_UTC : timeZone));
        return timeFormatter.format(date);
    }

    private static String getPeriodRepeat(Long repeat) {
        timeFormatter.setTimeZone(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
        return timeFormatter.format(Date.from(Instant.ofEpochMilli(0).plusSeconds(repeat)));
    }

    private static String getDailyPlanDate(Instant plannedStart, String timeZone, long periodBeginSeconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        return format.format(Date.from(plannedStart.minusSeconds(periodBeginSeconds)));
    }

    private OrderIdMap modifyStartTimeSingle(ModifyOrdersHelper in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog, ZoneId zoneId)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, SOSInvalidDataException,
            SOSHibernateException, ParseException, IOException {

        Set<Long> submissionIds = items.stream().map(DBItemDailyPlanOrder::getSubmissionHistoryId).collect(Collectors.toSet());
        Set<String> oldDailyPlanDates = items.stream().map(DBItemDailyPlanOrder::getDailyPlanDateFromOrderId).collect(Collectors.toSet());

        SOSHibernateSession session = null;
        boolean isBulkOperation = items.size() > 1;
        boolean stickDailyPlan = in.getStickDailyPlanDate() == Boolean.TRUE;
        final String settingTimeZone = getSettings().getTimeZone();
        Instant now = Instant.now();
        Long settingPeriodBeginSecondsOpt = JobSchedulerDate.getSecondsOfHHmmss(getSettings().getPeriodBegin());
        final long settingPeriodBeginSeconds = settingPeriodBeginSecondsOpt != null ? settingPeriodBeginSecondsOpt.longValue() : 0;
        // Set<String> dailyPlanDates = new HashSet<>();
        Map<String, DBItemDailyPlanSubmission> dailyPlanSubmissions = new HashMap<>();
        OrderIdMap result = new OrderIdMap();
        Map<String, String> cycleOrderIdMap = new HashMap<>();
        // Map<String, TreeSet<DBItemDailyPlanOrder>> cyclicOrders = new HashMap<>();
        Set<DBItemDailyPlanOrder> allItems = new HashSet<>();

        // true if cyclic
        Map<Boolean, List<DBItemDailyPlanOrder>> itemsMap = items.stream().collect(Collectors.groupingBy(DBItemDailyPlanOrder::isCyclic));

        try {
            // boolean hasCycle = itemsMap.containsKey(Boolean.TRUE); // items.stream().anyMatch(DBItemDailyPlanOrder::isCyclic);
            // DBLayerDailyPlannedOrders dbLayer = null;
            // if (hasCycle) {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTime]");
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            // }

            itemsMap.putIfAbsent(Boolean.TRUE, Collections.emptyList());
            itemsMap.putIfAbsent(Boolean.FALSE, Collections.emptyList());

            // prepare orders, e.g. calculate new orders id, new planned start
            for (DBItemDailyPlanOrder item : itemsMap.get(Boolean.TRUE)) { // cyclic orders

                TreeSet<DBItemDailyPlanOrder> cyclicOrdersOfItem = dbLayer.getDailyPlanOrdersByCyclicMainPart(in.getControllerId(), OrdersHelper
                        .getCyclicOrderIdMainPart(item.getOrderId())).stream().collect(Collectors.toCollection(() -> new TreeSet<>(comp)));

                DBItemDailyPlanOrder firstOrderOfCycle = cyclicOrdersOfItem.first();
                Instant newPlannedStartOfFirst = in.getNewPlannedStart(firstOrderOfCycle.getPlannedStart());

                if ("never".equals(in.getScheduledFor())) {

                    firstOrderOfCycle.setPlannedStart(Date.from(newPlannedStartOfFirst));
                    firstOrderOfCycle.setExpectedEnd(null);
                    firstOrderOfCycle.setIsLastOfCyclic(true);

                    String dailyPlanDateOfFirst = stickDailyPlan ? firstOrderOfCycle.getDailyPlanDateFromOrderId() : firstOrderOfCycle
                            .getDailyPlanDate(SOSDate.TIMEZONE_UTC, 0);
                    // dailyPlanDates.add(dailyPlanDateOfFirst);
                    if (!dailyPlanSubmissions.containsKey(dailyPlanDateOfFirst)) {
                        DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), dailyPlanDateOfFirst);
                        session.save(submission);
                        dailyPlanSubmissions.put(dailyPlanDateOfFirst, submission);
                    }

                    String newOrderId = OrdersHelper.generateNewFromOldOrderId(firstOrderOfCycle.getOrderId(), dailyPlanDateOfFirst, zoneId);
                    result.getAdditionalProperties().put(item.getOrderId(), newOrderId);
                    cycleOrderIdMap.put(firstOrderOfCycle.getOrderId(), newOrderId);

                    allItems.addAll(cyclicOrdersOfItem);

                } else {

                    DBItemDailyPlanOrder lastOrderOfCycle = cyclicOrdersOfItem.last();
                    // Instant newPlannedStartOfLast = in.getNewPlannedStart(lastOrderOfCycle.getPlannedStart());
                    Instant newPlannedStartOfLast = lastOrderOfCycle.getPlannedStart().toInstant().plusMillis(newPlannedStartOfFirst.toEpochMilli()
                            - firstOrderOfCycle.getPlannedStart().getTime());

                    if (newPlannedStartOfLast.isBefore(now)) {

                        newPlannedStartOfLast = now;

                        if (lastOrderOfCycle.getExpectedEnd() != null) {
                            long expectedDuration = lastOrderOfCycle.getExpectedEnd().getTime() - lastOrderOfCycle.getPlannedStart().getTime();
                            lastOrderOfCycle.setExpectedEnd(Date.from(newPlannedStartOfLast.plusMillis(expectedDuration)));
                        }
                        lastOrderOfCycle.setPlannedStart(Date.from(newPlannedStartOfLast));
                        lastOrderOfCycle.setIsLastOfCyclic(true);

                        String dailyPlanDateOfLast = stickDailyPlan ? lastOrderOfCycle.getDailyPlanDateFromOrderId() : lastOrderOfCycle
                                .getDailyPlanDate(settingTimeZone, settingPeriodBeginSeconds);
                        // dailyPlanDates.add(dailyPlanDateOfLast);
                        if (!dailyPlanSubmissions.containsKey(dailyPlanDateOfLast)) {
                            DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), dailyPlanDateOfLast);
                            session.save(submission);
                            dailyPlanSubmissions.put(dailyPlanDateOfLast, submission);
                        }

                        String newOrderId = OrdersHelper.generateNewFromOldOrderId(lastOrderOfCycle.getOrderId(), dailyPlanDateOfLast, zoneId);
                        result.getAdditionalProperties().put(item.getOrderId(), newOrderId);
                        cycleOrderIdMap.put(lastOrderOfCycle.getOrderId(), newOrderId);

                        // TODO ? dailyplandate from first cycle item?
                        // DBItemDailyPlanOrder firstOrderOfCycle = cyclicOrdersOfItem.first();
                        // Instant dailyPlanDateOfFirst = in.getNewPlannedStart(firstOrderOfCycle.getPlannedStart());
                        // TODO setPeriodBegin and ..End with new dailyplandate?

                        allItems.addAll(cyclicOrdersOfItem);

                    } else {

                        String dailyPlanDateOfFirst = getDailyPlanDate(newPlannedStartOfFirst, settingTimeZone, settingPeriodBeginSeconds);
                        String stickyDailyPlanDateOfFirst = stickDailyPlan ? firstOrderOfCycle.getDailyPlanDateFromOrderId() : dailyPlanDateOfFirst;

                        Cycle cycle = new Cycle();
                        cycle.setRepeat(getPeriodRepeat(item.getRepeatInterval()));
                        Instant newPeriodbegin = in.getNewPlannedStart(item.getPeriodBegin());
                        long periodIntervalLength = item.getPeriodEnd().getTime() - item.getPeriodBegin().getTime();
                        cycle.setBegin(getPeriodBeginEnd(Date.from(newPeriodbegin), in.getTimeZone()));
                        cycle.setEnd(getPeriodBeginEnd(Date.from(newPeriodbegin.plusMillis(periodIntervalLength)), in.getTimeZone()));
                        if ("00:00:00".equals(cycle.getEnd())) {
                            cycle.setEnd("24:00:00");
                        }

                        // TODO check if end before begin
                        if (Integer.valueOf(cycle.getBegin().replace(":", "")).intValue() >= Integer.valueOf(cycle.getEnd().replace(":", ""))
                                .intValue()) {
                            cycle.setEnd("24:00:00");
                        }

                        if (!dailyPlanSubmissions.containsKey(stickyDailyPlanDateOfFirst)) {
                            DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), stickyDailyPlanDateOfFirst);
                            session.save(submission);
                            dailyPlanSubmissions.put(stickyDailyPlanDateOfFirst, submission);
                        }

                        modifyStartTimeCycle(in, dailyPlanDateOfFirst, cycle, item, cyclicOrdersOfItem, dailyPlanSubmissions.get(
                                stickyDailyPlanDateOfFirst), auditlog).ifPresent(newOrderId -> {
                                    result.getAdditionalProperties().put(item.getOrderId(), newOrderId);
                                });
                    }
                }
            }
            for (DBItemDailyPlanOrder item : itemsMap.get(Boolean.FALSE)) { // single start orders

                Instant newPlannedStart = in.getNewPlannedStart(item.getPlannedStart());
                if (newPlannedStart.isBefore(now)) {
                    if (isBulkOperation) {
                        newPlannedStart = now;
                    } else {
                        throw new JocBadRequestException("The planned start time must be in the future.");
                    }
                }

                if (item.getExpectedEnd() != null) {
                    if ("never".equals(in.getScheduledFor())) {
                        item.setExpectedEnd(null);
                    } else {
                        long expectedDuration = item.getExpectedEnd().getTime() - item.getPlannedStart().getTime();
                        item.setExpectedEnd(Date.from(newPlannedStart.plusMillis(expectedDuration)));
                    }
                }
                item.setPlannedStart(Date.from(newPlannedStart));

                // TODO dailyPlanDate from above newPlannedStart (not from now)?
                String dailyPlanDate = stickDailyPlan ? item.getDailyPlanDateFromOrderId() : item.getDailyPlanDate(settingTimeZone,
                        settingPeriodBeginSeconds);
                // dailyPlanDates.add(dailyPlanDate);
                if (!dailyPlanSubmissions.containsKey(dailyPlanDate)) {
                    DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), dailyPlanDate);
                    session.save(submission);
                    dailyPlanSubmissions.put(dailyPlanDate, submission);
                }

                result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.generateNewFromOldOrderId(item.getOrderId(), dailyPlanDate,
                        zoneId));

                allItems.add(item);
            }
        } finally {
            Globals.disconnect(session);
        }

        // OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), allItems).thenAccept(either -> {
        // SOSHibernateSession sessionNew = null;
        try {

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeSingle]");
            session.setAutoCommit(false);

            session.beginTransaction();
            DBLayerOrderVariables ovDbLayer = new DBLayerOrderVariables(session);
            List<DBItemDailyPlanOrder> toSubmit = new ArrayList<>();
            for (DBItemDailyPlanOrder item : allItems) {

                if (item.isCyclic()) {
                    if (!item.isLastOfCyclic()) { // only one item of cycle will be copied.
                        continue;
                    }
                }

                String oldOrderId = item.getOrderId();
                String newOrderId = result.getAdditionalProperties().get(oldOrderId);
                if (newOrderId == null) {
                    newOrderId = cycleOrderIdMap.get(oldOrderId);
                }

                if (newOrderId == null) {
                    continue;
                }

                // update variables
                if (item.isCyclic()) {
                    if (item.isLastOfCyclic()) {
                        ovDbLayer.copy(item.getControllerId(), oldOrderId, newOrderId, true);
                    }
                } else {
                    ovDbLayer.copy(item.getControllerId(), oldOrderId, newOrderId, false);
                }
                OrderTags.copyTagsOfOrder(item.getControllerId(), oldOrderId, newOrderId, item.getPlannedStart(), session);

                DBItemDailyPlanOrder copiedItem = new DBItemDailyPlanOrder();
                copiedItem.setCalendarId(item.getCalendarId());
                copiedItem.setControllerId(item.getControllerId());
                copiedItem.setCreated(Date.from(Instant.now()));
                copiedItem.setDailyPlanDate(item.getDailyPlanDate());
                copiedItem.setExpectedEnd(item.getExpectedEnd());
                copiedItem.setId(null);
                copiedItem.setModified(copiedItem.getCreated());
                copiedItem.setOrderId(newOrderId);
                copiedItem.setOrderName(item.getOrderName());
                copiedItem.setOrderParameterisation(item.getOrderParameterisation());
                copiedItem.setPeriodBegin(item.getPeriodBegin());
                copiedItem.setPeriodEnd(item.getPeriodEnd());
                copiedItem.setPlannedStart(item.getPlannedStart());
                copiedItem.setRepeatInterval(item.getRepeatInterval());
                copiedItem.setScheduleFolder(item.getScheduleFolder());
                copiedItem.setScheduleName(item.getScheduleName());
                copiedItem.setSchedulePath(item.getSchedulePath());
                copiedItem.setStartMode(item.getStartMode());
                copiedItem.setSubmitted(false);
                copiedItem.setSubmitTime(item.getSubmitTime());
                copiedItem.setWorkflowFolder(item.getWorkflowFolder());
                copiedItem.setWorkflowName(item.getWorkflowName());
                copiedItem.setWorkflowPath(item.getWorkflowPath());

                if (dailyPlanSubmissions.size() == 1) {
                    copiedItem.setSubmissionHistoryId(dailyPlanSubmissions.values().iterator().next().getId());
                } else if (dailyPlanSubmissions.size() > 1) {
                    DBItemDailyPlanSubmission submission = dailyPlanSubmissions.get(item.getDailyPlanDate());
                    if (submission == null) {
                        copiedItem.setSubmissionHistoryId(dailyPlanSubmissions.values().iterator().next().getId());
                    } else {
                        copiedItem.setSubmissionHistoryId(submission.getId());
                    }
                }

                if (item.getSubmitted()) {
                    toSubmit.add(copiedItem);
                }
                session.save(copiedItem);
            }
            session.commit();
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            for (Long submissionId : submissionIds) {
                deleteNotUsedSubmission(session, dbLayer, in.getControllerId(), submissionId);
            }
            session.close();
            session = null;

            if (toSubmit.size() > 0) {
                submitOrdersToController(toSubmit, in.getForceJobAdmission());
            }

            dailyPlanSubmissions.keySet().forEach(dailyPlanDate -> EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(),
                    dailyPlanDate)));
            oldDailyPlanDates.forEach(dailyPlanDate -> EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), dailyPlanDate)));

            OrdersHelper.storeAuditLogDetails(allItems.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), in
                    .getControllerId())).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                            either2, getAccessToken(), getJocError(), in.getControllerId()));

        } catch (Exception e) {
            Globals.rollback(session);
        } finally {
            Globals.disconnect(session);
        }
        return result;
    }

    private Optional<String> modifyStartTimeCycle(ModifyOrdersHelper in, DBItemDailyPlanOrder mainItem,
            TreeSet<DBItemDailyPlanOrder> cyclicOrdersOfItem, DBItemJocAuditLog auditlog) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, SOSInvalidDataException, ParseException,
            IOException {
        String submissionForDate = in.getStickDailyPlanDate() == Boolean.TRUE ? mainItem.getDailyPlanDateFromOrderId() : in.getScheduledFor();
        return modifyStartTimeCycle(in, in.getScheduledFor(), in.getCycle(), mainItem, cyclicOrdersOfItem, newSubmission(in.getControllerId(),
                submissionForDate), auditlog);
    }

    private Optional<String> modifyStartTimeCycle(ModifyOrdersHelper in, String dailyplanDate, Cycle cycle, DBItemDailyPlanOrder mainItem,
            TreeSet<DBItemDailyPlanOrder> cyclicOrdersOfItem, DBItemDailyPlanSubmission submission, DBItemJocAuditLog auditlog)
            throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException,
            SOSInvalidDataException, ParseException, IOException {

        String oldDailyPlanDate = mainItem.getDailyPlanDateFromOrderId();

        SOSHibernateSession session = null;
        Map<PlannedOrderKey, PlannedOrder> generatedOrders = Collections.emptyMap();
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[copyStartTimeCycle][" + mainItem.getOrderId() + "]");
            session.setAutoCommit(false);

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            if (cyclicOrdersOfItem == null) {
                String mainPart = OrdersHelper.getCyclicOrderIdMainPart(mainItem.getOrderId());
                cyclicOrdersOfItem = dbLayer.getDailyPlanOrdersByCyclicMainPart(in.getControllerId(), mainPart).stream().collect(Collectors
                        .toCollection(() -> new TreeSet<>(comp)));

                Instant now = Instant.now();

                DBItemDailyPlanOrder firstOrderOfCycle = cyclicOrdersOfItem.first();
                Instant newPlannedStartOfFirst = in.getNewPlannedStart(firstOrderOfCycle.getPlannedStart());

                DBItemDailyPlanOrder lastOrderOfCycle = cyclicOrdersOfItem.last();
                Instant newPlannedStartOfLast = lastOrderOfCycle.getPlannedStart().toInstant().plusMillis(newPlannedStartOfFirst.toEpochMilli()
                        - firstOrderOfCycle.getPlannedStart().getTime());

                if (newPlannedStartOfLast.isBefore(now)) {
                    throw new JocBadRequestException("The planned start time must be in the future.");
                }
            }

            // get variables
            DBItemDailyPlanVariable variable = new DBLayerOrderVariables(session).getOrderVariable(mainItem.getControllerId(), mainItem.getOrderId(),
                    true);

            Set<String> orderTags = OrderTags.getTagsOfOrderId(mainItem.getControllerId(), mainItem.getOrderId(), session);

            String submissionForDate = SOSDate.getDateAsString(submission.getSubmissionForDate());
            if (submission.getId() == null) {
                submission = insertNewSubmission(submission, session);
            }

            // TODO submission.getSubmissionForDate() or dailyplanDate?
            DailyPlanRunner runner = getDailyPlanRunner(mainItem.getSubmitted(), submission.getSubmissionForDate());

            OrderListSynchronizer synchronizer = calculateStartTimes(in, cycle, runner, dailyplanDate, submission, mainItem, variable, orderTags);
            synchronizer.substituteOrderIds();
            generatedOrders = synchronizer.getPlannedOrders();

            session.close();
            session = null;

            runner.addPlannedOrderToControllerAndDB(StartupMode.webservice, in.getControllerId(), dailyplanDate, mainItem.getSubmitted(),
                    synchronizer);

            EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), oldDailyPlanDate));
            if (!oldDailyPlanDate.equals(submissionForDate)) {
                EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), submissionForDate));
            }

            Set<AuditLogDetail> auditLogDetails = new HashSet<>();
            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), mainItem
                        .getControllerId()));
            }

            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                    either2, getAccessToken(), getJocError(), mainItem.getControllerId()));

        } finally {
            Globals.disconnect(session);
        }

        Optional<String> newOrderId = Optional.empty();
        if (generatedOrders != null && generatedOrders.size() > 0) {
            newOrderId = generatedOrders.entrySet().stream().findFirst().map(Map.Entry<PlannedOrderKey, PlannedOrder>::getValue).map(
                    PlannedOrder::getFreshOrder).map(FreshOrder::getId);
        }
        return newOrderId;
    }

    private synchronized void deleteNotUsedSubmission(SOSHibernateSession session, DBLayerDailyPlannedOrders dbLayer, String controllerId,
            Long submissionId) {
        if (submissionId != null) {
            try {
                Long count = dbLayer.getCountOrdersBySubmissionId(controllerId, submissionId);
                if (count.equals(0L)) {
                    session.beginTransaction();
                    dbLayer.deleteSubmission(submissionId);
                    session.commit();
                }
            } catch (Exception e1) {
                LOGGER.warn(e1.toString());
            }
        }
    }

    // private boolean isCyclicOrders(List<DBItemDailyPlanOrder> items) throws Exception {
    // boolean hasSingle = false;
    // boolean hasCyclic = false;
    // for (DBItemDailyPlanOrder item : items) {
    // if (hasSingle && hasCyclic) {
    // break;
    // }
    // if (OrdersHelper.isCyclicOrderId(item.getOrderId())) {
    // hasCyclic = true;
    // } else {
    // hasSingle = true;
    // }
    // }
    // if (hasSingle && hasCyclic) {
    // throw new Exception("Modify Start Time operation is not allowed. Single and Cyclic orders detected.");
    // }
    // return hasCyclic;
    // }

    private DailyPlanRunner getDailyPlanRunner(boolean withSubmit, Date dailyPlanDate) {
        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
        settings.setOverwrite(true);
        settings.setSubmit(withSubmit);
        settings.setTimeZone(getSettings().getTimeZone());
        settings.setPeriodBegin(getSettings().getPeriodBegin());
        settings.setDailyPlanDate(dailyPlanDate);
        settings.setSubmissionTime(new Date());

        return new DailyPlanRunner(settings);
    }

    private OrderListSynchronizer calculateStartTimes(DailyPlanModifyOrder in, Cycle cycle, DailyPlanRunner runner, String dailyPlanDate,
            DBItemDailyPlanSubmission newSubmission, final DBItemDailyPlanOrder mainItem, DBItemDailyPlanVariable variable, Set<String> orderTags) {

        try {
            Schedule schedule = new Schedule();
            schedule.setVersion("");
            schedule.setPath(mainItem.getSchedulePath());
            schedule.setWorkflowNames(Arrays.asList(mainItem.getWorkflowName()));
            if (JocInventory.SCHEDULE_CONSIDER_WORKFLOW_NAME) {
                schedule.setWorkflowName(mainItem.getWorkflowName());
            }
            schedule.setTitle("");
            schedule.setDocumentationName("");
            schedule.setSubmitOrderToControllerWhenPlanned(mainItem.getSubmitted());
            schedule.setPlanOrderAutomatically(true);
            schedule.setOrderParameterisations(new ArrayList<OrderParameterisation>());
            OrderParameterisation orderParameterisation = new OrderParameterisation();
            orderParameterisation.setOrderName(mainItem.getOrderName());
            Variables variables = new Variables();
            if (variable != null && variable.getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(variable.getVariableValue(), Variables.class);
            }
            // TODO order positions??
            // orderParameterisation.setStartPosition(null);
            // orderParameterisation.setEndPosition(null);
            orderParameterisation.setVariables(variables);
            orderParameterisation.setTags(orderTags);
            if (!orderParameterisation.getVariables().getAdditionalProperties().isEmpty() || !orderParameterisation.getTags().isEmpty()) {
                schedule.getOrderParameterisations().add(orderParameterisation);
            }

            schedule.setCalendars(new ArrayList<AssignedCalendars>());
            AssignedCalendars calendars = new AssignedCalendars();
            Calendar calendar = getCalendarById(mainItem.getCalendarId());
            calendars.setCalendarName(calendar.getName());
            calendars.setPeriods(new ArrayList<Period>());
            calendars.setTimeZone(in.getTimeZone() == null ? SOSDate.TIMEZONE_UTC : in.getTimeZone());
            Period period = new Period();
            period.setBegin(cycle.getBegin());
            period.setEnd(cycle.getEnd());
            period.setRepeat(cycle.getRepeat());
            calendars.getPeriods().add(period);
            schedule.getCalendars().add(calendars);

            DailyPlanScheduleWorkflow w = new DailyPlanScheduleWorkflow(mainItem.getWorkflowName(), mainItem.getWorkflowPath(), null);
            DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(w));

            return runner.calculateStartTimes(StartupMode.webservice, in.getControllerId(), Arrays.asList(dailyPlanSchedule), dailyPlanDate,
                    newSubmission, calendar.getId(), getJocError(), getAccessToken());

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
                | ExecutionException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
        }
        return runner.getEmptySynchronizer();
    }

    // private Map<PlannedOrderKey, PlannedOrder> recreateCyclicOrder(DailyPlanModifyOrder in, DBItemDailyPlanSubmission newSubmission,
    // final DBItemDailyPlanOrder mainItem, DBItemDailyPlanVariable variable, DBItemJocAuditLog auditlog) {
    // String controllerId = in.getControllerId();
    //// String dDate = in.getDailyPlanDate();
    //// if (dDate == null) {
    //// dDate = OrdersHelper.getDateFromOrderId(mainItem.getOrderId());
    //// }
    //
    // LOGGER.debug("recreateCyclicOrder: main orderId=" + mainItem.getOrderId());
    //
    // Map<PlannedOrderKey, PlannedOrder> generatedOrders = null;
    // try {
    // Schedule schedule = new Schedule();
    // schedule.setVersion("");
    // schedule.setPath(mainItem.getSchedulePath());
    // schedule.setWorkflowNames(Arrays.asList(mainItem.getWorkflowName()));
    // if (JocInventory.SCHEDULE_CONSIDER_WORKFLOW_NAME) {
    // schedule.setWorkflowName(mainItem.getWorkflowName());
    // }
    // schedule.setTitle("");
    // schedule.setDocumentationName("");
    // schedule.setSubmitOrderToControllerWhenPlanned(mainItem.getSubmitted());
    // schedule.setPlanOrderAutomatically(true);
    // schedule.setOrderParameterisations(new ArrayList<OrderParameterisation>());
    // OrderParameterisation orderParameterisation = new OrderParameterisation();
    // orderParameterisation.setOrderName(mainItem.getOrderName());
    // Variables variables = new Variables();
    // if (variable != null && variable.getVariableValue() != null) {
    // variables = Globals.objectMapper.readValue(variable.getVariableValue(), Variables.class);
    // }
    // // TODO order positions??
    // // orderParameterisation.setStartPosition(null);
    // // orderParameterisation.setEndPosition(null);
    // orderParameterisation.setVariables(variables);
    // if (orderParameterisation.getVariables().getAdditionalProperties().size() > 0) {
    // schedule.getOrderParameterisations().add(orderParameterisation);
    // }
    //
    // schedule.setCalendars(new ArrayList<AssignedCalendars>());
    // AssignedCalendars calendars = new AssignedCalendars();
    // Calendar calendar = getCalendarById(mainItem.getCalendarId());
    // calendars.setCalendarName(calendar.getName());
    // calendars.setPeriods(new ArrayList<Period>());
    // calendars.setTimeZone(in.getTimeZone() == null ? SOSDate.TIMEZONE_UTC : in.getTimeZone());
    // Period period = new Period();
    // period.setBegin(in.getCycle().getBegin());
    // period.setEnd(in.getCycle().getEnd());
    // period.setRepeat(in.getCycle().getRepeat());
    // calendars.getPeriods().add(period);
    // schedule.getCalendars().add(calendars);
    //
    // DailyPlanRunner runner = getDailyPlanRunner(mainItem.getSubmitted(), newSubmission.getSubmissionForDate());
    //
    // DailyPlanScheduleWorkflow w = new DailyPlanScheduleWorkflow(mainItem.getWorkflowName(), mainItem.getWorkflowPath(), null);
    // DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(w));
    //
    // generatedOrders = runner.generateDailyPlan(StartupMode.manual, controllerId, Arrays.asList(dailyPlanSchedule), mainItem.getDailyPlanDate(
    // getSettings().getTimeZone(), getSettings().getPeriodBegin()), newSubmission, mainItem.getSubmitted(), getJocError(), getAccessToken());
    //
    // Set<AuditLogDetail> auditLogDetails = new HashSet<>();
    // for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
    // auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), controllerId));
    // }
    //
    // EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), SOSDate.getDateAsString(newSubmission.getSubmissionForDate())));
    //
    // OrdersHelper.storeAuditLogDetails(auditLogDetails, auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
    // either2, getAccessToken(), getJocError(), controllerId));
    // } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
    // | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
    // | ExecutionException e) {
    // ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
    // }
    // return generatedOrders;
    // }

    private Calendar getCalendarById(Long id) throws JsonParseException, JsonMappingException, SOSHibernateException, IOException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[getCalendarById=" + id + "]");
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            DBItemInventoryReleasedConfiguration config = dbLayer.getReleasedConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("Couldn't find calendar '%s'", id));
            }

            Calendar calendar = Globals.objectMapper.readValue(config.getContent(), Calendar.class);
            calendar.setName(config.getName());
            calendar.setPath(config.getPath());
            calendar.setId(id);
            return calendar;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void submitOrdersToController(List<DBItemDailyPlanOrder> items, Boolean forceJobAdmission) throws JsonParseException,
            JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException,
            SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        LOGGER.debug("submitOrdersToController: size=" + items.size());

        if (items.size() > 0) {
            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            settings.setOverwrite(false);
            settings.setSubmit(true);
            settings.setTimeZone(getSettings().getTimeZone());
            settings.setPeriodBegin(getSettings().getPeriodBegin());

            DailyPlanRunner runner = new DailyPlanRunner(settings);
            runner.submitOrders(StartupMode.webservice, items.get(0).getControllerId(), items, "", forceJobAdmission, getJocError(),
                    getAccessToken());
        }
    }

    // private DBItemDailyPlanSubmission insertNewSubmission(String controllerId, String dailyPlanDate, SOSHibernateSession session)
    // throws SOSHibernateException, SOSInvalidDataException {
    // DBItemDailyPlanSubmission item = newSubmission(controllerId, dailyPlanDate);
    // return insertNewSubmission(item, dailyPlanDate, session);
    // }

    private DBItemDailyPlanSubmission insertNewSubmission(DBItemDailyPlanSubmission item, SOSHibernateSession session) throws SOSHibernateException,
            SOSInvalidDataException {
        boolean sessionIsNull = session == null;
        try {
            if (sessionIsNull) {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[insertNewSubmission]");
            }
            session.beginTransaction();
            session.save(item);
            session.commit();
        } finally {
            if (sessionIsNull) {
                Globals.disconnect(session);
            }
        }
        return item;
    }

    private DBItemDailyPlanSubmission newSubmission(String controllerId, String dailyPlanDate) throws SOSInvalidDataException {
        return newSubmission(controllerId, SOSDate.parse(dailyPlanDate, SOSDate.DATE_FORMAT));
    }

    private DBItemDailyPlanSubmission newSubmission(String controllerId, Date dailyPlanDate) throws SOSInvalidDataException {
        DBItemDailyPlanSubmission item = new DBItemDailyPlanSubmission();
        item.setControllerId(controllerId);
        item.setSubmissionForDate(dailyPlanDate);
        item.setUserAccount(getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
        item.setCreated(new Date());
        item.setId(null);
        return item;
    }

}
