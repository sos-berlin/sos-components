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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.ModifyOrdersHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
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
import com.sos.joc.dailyplan.resource.IDailyPlanModifyOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
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
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.dailyplan.Cycle;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.order.BlockPosition;
import com.sos.joc.model.order.OrderIdMap;
import com.sos.joc.model.order.OrderIdMap200;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerState;
import js7.proxy.javaapi.JControllerProxy;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanModifyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    private static final Comparator<DBItemDailyPlanOrder> comp = Comparator.comparing(DBItemDailyPlanOrder::getOrderId);

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, DailyPlanModifyOrder.class);
            ModifyOrdersHelper in = Globals.objectMapper.readValue(filterBytes, ModifyOrdersHelper.class);
            String controllerId = in.getControllerId();

            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders().getModify());
            if (response != null) {
                return response;
            }

            boolean hasManagePositionsPermission = getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
            if (!hasManagePositionsPermission && (in.getStartPosition() != null || (in.getEndPositions() != null && !in.getEndPositions()
                    .isEmpty()) || in.getBlockPosition() != null)) {
                return accessDeniedResponse("Access denied for setting start-/end-/blockpositions");
            }
            
            JControllerProxy proxy = null;
            JControllerState currentState = null;
            
            // DailyPlan Orders: orderIds.get(Boolean.FALSE), Adhoc Orders: orderIds.get(Boolean.TRUE)
            Map<Boolean, Set<String>> orderIds = in.getOrderIds().stream().collect(Collectors.groupingBy(id -> id.matches(".*#T[0-9]+-.*"), Collectors
                    .toSet()));
            orderIds.putIfAbsent(Boolean.FALSE, Collections.emptySet());
            orderIds.putIfAbsent(Boolean.TRUE, Collections.emptySet());

            CategoryType category = orderIds.get(Boolean.FALSE).isEmpty() ? CategoryType.CONTROLLER : CategoryType.DAILYPLAN;
            DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId(), category);

            List<DBItemDailyPlanOrder> dailyPlanOrderItems = null;
            if (!orderIds.get(Boolean.FALSE).isEmpty()) {
                dailyPlanOrderItems = getDailyPlanOrders(controllerId, DailyPlanUtils.getDistinctOrderIds(orderIds.get(Boolean.FALSE)));
            }
            if (dailyPlanOrderItems == null) {
                dailyPlanOrderItems = Collections.emptyList();
            }
            boolean someDailyPlanOrdersAreSubmitted = dailyPlanOrderItems.stream().anyMatch(DBItemDailyPlanOrder::getSubmitted);
            boolean onlyStarttimeModifications = hasOnlyStarttimeModifications(in);

            Stream<String> workflowNames = Stream.empty();
            if (!onlyStarttimeModifications) {
                workflowNames = dailyPlanOrderItems.stream().map(DBItemDailyPlanOrder::getWorkflowName).distinct();
            }

            // some dailyplan orders are already submitted then these must be in state SCHEDULED
            if (someDailyPlanOrdersAreSubmitted || !orderIds.get(Boolean.TRUE).isEmpty()) {
                if (proxy == null) {
                    proxy = Proxy.of(controllerId);
                }
                if (currentState == null) {
                    currentState = proxy.currentState();
                }

                OrdersHelper.getNotFreshOrders(in.getOrderIds(), currentState).ifPresent(s -> {
                    throw new JocBadRequestException("Some orders are not in the state SCHEDULED or PLANNED: " + s.toString());
                });

                if (!onlyStarttimeModifications) {
                    workflowNames = Stream.concat(workflowNames, OrdersHelper.getWorkflowNamesOfFreshOrders(in.getOrderIds(), currentState));
                }
            }

            Map<String, List<Object>> labelMap = Collections.emptyMap();
            Set<BlockPosition> blockPositions = Collections.emptySet();
            if (!onlyStarttimeModifications) { // check that all orders belong to one workflow
                Set<String> wNames = workflowNames.collect(Collectors.toSet());
                if (wNames.size() > 1) {
                    throw new JocBadRequestException(
                            "All orders must belong to the same workflow when variables or positions are modified. Involved workflows are " + wNames
                                    .toString());
                } else if (wNames.size() == 1) {
                    // JOC-1453
                    boolean withStartLabel = in.getStartPosition() != null && in.getStartPosition() instanceof String;
                    boolean withEndLabels = in.getEndPositions() != null && in.getEndPositions().stream().filter(Objects::nonNull).anyMatch(
                            ep -> ep instanceof String);
                    boolean withBlock = in.getBlockPosition() != null;
                    
                    if (withStartLabel || withEndLabels || withBlock) {
                        com.sos.inventory.model.workflow.Workflow workflow = getWorkflow(controllerId, wNames.iterator().next());
                        if (withStartLabel || withEndLabels) {
                            labelMap = getLabelMap(workflow);
                        }
                        if (withBlock) {
                            blockPositions = getBlockPositions(workflow);
                        }
                    }
                }
            }

            if (!orderIds.get(Boolean.TRUE).isEmpty()) {
                if (proxy == null) {
                    proxy = Proxy.of(controllerId);
                }
                if (currentState == null) {
                    currentState = proxy.currentState();
                }
            }
            setSettings();
            ZoneId zoneId = getZoneId();
            Either<List<Err419>, OrderIdMap> adhocCall = OrdersHelper.cancelAndAddFreshOrder(orderIds.get(Boolean.TRUE), in, accessToken,
                    getJocError(), auditlog.getId(), proxy, currentState, zoneId, labelMap, blockPositions, folderPermissions);
            OrderIdMap dailyPlanResult = null;

            if (!dailyPlanOrderItems.isEmpty()) {
                if (!onlyStarttimeModifications) {
                    dailyPlanResult = modifyOrderParameterisation(in, dailyPlanOrderItems, auditlog, zoneId, labelMap, blockPositions);
                } else {
                    dailyPlanResult = modifyStartTime(in, dailyPlanOrderItems, auditlog, zoneId);
                }
            } else {
                LOGGER.debug("0 dailyplan orders found");
            }

            // TODO is not only adhocCall.isLeft() dependent ...
            if (adhocCall.isLeft()) {
                return JOCDefaultResponse.responseStatus419(adhocCall.getLeft());
            } else {
                OrderIdMap200 entity = new OrderIdMap200();
                entity.setDeliveryDate(Date.from(Instant.now()));
                entity.setOrderIds(getResult(adhocCall.get(), dailyPlanResult));
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private OrderIdMap getResult(OrderIdMap adhocResult, OrderIdMap dailyPlanResult) {
        if (adhocResult == null && dailyPlanResult == null) {
            return new OrderIdMap();
        }
        if (adhocResult != null && dailyPlanResult == null) {
            return adhocResult;
        }
        if (adhocResult == null && dailyPlanResult != null) {
            return dailyPlanResult;
        }

        OrderIdMap result = new OrderIdMap();
        adhocResult.getAdditionalProperties().entrySet().stream().forEach(e -> {
            result.getAdditionalProperties().put(e.getKey(), e.getValue());
        });
        dailyPlanResult.getAdditionalProperties().entrySet().stream().forEach(e -> {
            result.getAdditionalProperties().put(e.getKey(), e.getValue());
        });
        return result;
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

    private boolean hasOnlyStarttimeModifications(DailyPlanModifyOrder in) {
        if (in.getVariables() != null && !in.getVariables().getAdditionalProperties().isEmpty()) {
            return false;
        }
        if (in.getRemoveVariables() != null && !in.getRemoveVariables().isEmpty()) {
            return false;
        }
        if (in.getStartPosition() != null) {
            return false;
        }
        if (in.getEndPositions() != null && !in.getEndPositions().isEmpty()) {
            return false;
        }
        if (in.getBlockPosition() != null) {
            return false;
        }
        if ((in.getScheduledFor() == null || in.getScheduledFor().isEmpty()) && in.getCycle() == null) {
            throw new JocMissingRequiredParameterException(
                    "At least one of the parameters 'scheduledFor', 'cycle', 'variables', 'removeVariables', 'startPosition' or 'endPositions' should be set.");
        }
        return true;
    }

    private boolean withNewStartPosition(DailyPlanModifyOrder in) {
        if (in.getStartPosition() != null) {
            return true;
        }
        return false;
    }

    private boolean withNewEndPositions(DailyPlanModifyOrder in) {
        if (in.getEndPositions() != null && !in.getEndPositions().isEmpty()) {
            return true;
        }
        return false;
    }
    
    private boolean withNewBlockPosition(DailyPlanModifyOrder in) {
        if (in.getBlockPosition() != null) {
            return true;
        }
        return false;
    }

    private boolean withNewPositions(DailyPlanModifyOrder in) {
        return withNewStartPosition(in) || withNewEndPositions(in) || withNewBlockPosition(in);
    }

    private boolean withAddOrUpdateVariables(DailyPlanModifyOrder in) {
        if (in.getVariables() != null && !in.getVariables().getAdditionalProperties().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean withRemoveVariables(DailyPlanModifyOrder in) {
        if (in.getRemoveVariables() != null && !in.getRemoveVariables().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean withNewVariables(DailyPlanModifyOrder in) {
        return withAddOrUpdateVariables(in) || withRemoveVariables(in);
    }

    private OrderIdMap modifyOrderParameterisation(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog,
            ZoneId zoneId, Map<String, List<Object>> labelMap, Set<BlockPosition> availableBlockPositions) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException, SOSHibernateException, IOException {

        OrderIdMap result = new OrderIdMap();
        boolean withNewPositions = withNewPositions(in);
        boolean withNewVariables = withNewVariables(in);

        if (!withNewPositions && !withNewVariables) {
            return result;
        }

        List<DBItemDailyPlanOrder> submitted = new ArrayList<>();
        Map<Long, DBItemDailyPlanVariable> submittedVariables = new HashMap<>();
        Map<Long, List<DBItemDailyPlanOrder>> submittedCyclic = new HashMap<>();
        Map<Long, String> submittedCyclicNewParts = new HashMap<>();
        Workflow workflow = null;

        SOSHibernateSession session = null;
        if (items != null && !items.isEmpty()) {

            try {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyVariables]");
                session.setAutoCommit(false);
                session.beginTransaction();

                if (withNewVariables) {
                    // TODO get OrderPreparation from workflow
                    DeployedConfigurationDBLayer dcDbLayer = new DeployedConfigurationDBLayer(session);
                    DeployedContent content = dcDbLayer.getDeployedInventory(in.getControllerId(), DeployType.WORKFLOW.intValue(), items.get(0)
                            .getWorkflowName());
                    if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                        workflow = Globals.objectMapper.readValue(content.getContent(), Workflow.class);
                    }
                }

                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                DBLayerOrderVariables ovDbLayer = new DBLayerOrderVariables(session);
                
                final List<Object> startPosition = OrdersHelper.getPosition(in.getStartPosition(), labelMap);
                final List<Object> endPositions = withNewEndPositions(in) ? in.getEndPositions().stream().map(pos -> OrdersHelper.getPosition(pos,
                        labelMap)).filter(Objects::nonNull).collect(Collectors.toList()) : null;
                final BlockPosition blockPosition = OrdersHelper.getBlockPosition(in.getBlockPosition(), items.get(0).getWorkflowName(),
                        availableBlockPositions);

                // modify planned and prepare submitted
                for (DBItemDailyPlanOrder item : items) {

                    if (withNewPositions) {
                        OrderParameterisation orderParameterisation = null;
                        if (item.getOrderParameterisation() != null) {
                            orderParameterisation = Globals.objectMapper.readValue(item.getOrderParameterisation(), OrderParameterisation.class);
                        }
                        if (orderParameterisation == null) {
                            orderParameterisation = new OrderParameterisation();
                        }
                        OrderPositions origOp = orderParameterisation.getPositions();
                        OrderPositions newOp = new OrderPositions();
                        if (withNewStartPosition(in)) {
                            newOp.setStartPosition(startPosition);
                        }
                        if (withNewEndPositions(in)) {
                            newOp.setEndPositions(endPositions);
                        }
                        if (withNewBlockPosition(in)) {
                            newOp.setBlockPosition(blockPosition == null ? null : blockPosition.getPosition());
                        }
                        if (newOp.equals(origOp)) {
                            withNewPositions = false;
                        } else {
                            orderParameterisation.setPositions(newOp);
                            item.setOrderParameterisation(Globals.objectMapper.writeValueAsString(orderParameterisation));
                        }
                    }

                    if (withNewVariables) {
                        DBItemDailyPlanVariable variables = ovDbLayer.getOrderVariable(in.getControllerId(), item.getOrderId(), item.isCyclic());

                        // skip when variable not exists and only should be removed
                        if (variables == null && !withNewPositions && !withAddOrUpdateVariables(in)) {
                            // not changed
                            result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());
                            continue;
                        }

                        if (item.getSubmitted()) {
                            // prepare to modify later
                            submittedVariables.put(item.getId(), variables);
                        } else {
                            // modify now
                            modifyVariables(in, variables, session, item.getOrderId(), (workflow != null) ? workflow.getOrderPreparation() : null);
                        }
                    }

                    // cyclic main order
                    if (item.isCyclic()) {
                        if (item.getSubmitted()) {
                            // calculate new OrderId
                            String newPart = OrdersHelper.getUniqueOrderId(zoneId);
                            submittedCyclicNewParts.put(item.getId(), newPart);
                            result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.getNewFromOldOrderId(item.getOrderId(), newPart));

                            // prepare to modify later
                            List<DBItemDailyPlanOrder> cyclic = dbLayer.getDailyPlanOrdersByCyclicMainPart(item.getControllerId(), OrdersHelper
                                    .getCyclicOrderIdMainPart(item.getOrderId()));
                            if (withNewPositions) {
                                cyclic = cyclic.stream().peek(c -> c.setOrderParameterisation(item.getOrderParameterisation())).collect(Collectors
                                        .toList());
                            }
                            submittedCyclic.put(item.getId(), cyclic);
                            submitted.addAll(cyclic);
                        } else {
                            // not changed for planned order
                            result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());

                            if (withNewPositions) {
                                // modify cyclic DBItemDailyPlanOrder items now
                                dbLayer.updateDailyPlanOrdersByCyclicMainPart(item.getControllerId(), OrdersHelper.getCyclicOrderIdMainPart(item
                                        .getOrderId()), item.getOrderParameterisation());
                            }
                        }
                    } else { // single start
                        if (item.getSubmitted()) {
                            // not check the plannedStatTime due to possible cyclic workflow ..
                            // calculate new OrderId
                            result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.generateNewFromOldOrderId(item.getOrderId(),
                                    zoneId));

                            // prepare to modify later
                            submitted.add(item);

                        } else {
                            // not changed for planned order
                            result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());

                            if (withNewPositions) {
                                // modify now
                                item.setModified(new Date());
                                session.update(item);
                            }
                        }

                    }
                }
                Globals.commit(session);
            } catch (Exception e) {
                Globals.rollback(session);
                throw e;
            } finally {
                Globals.disconnect(session);
            }
        }

        if (submitted.size() > 0) {// submitted - single and all cyclic
            final Requirements orderPreparation = (workflow != null) ? workflow.getOrderPreparation() : null;
            CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), submitted);
            c.thenAccept(either -> {
                SOSHibernateSession sessionNew = null;
                try {
                    List<DBItemDailyPlanOrder> toSubmit = new ArrayList<>();
                    Date now = new Date();
                    boolean isDebugEnabled = LOGGER.isDebugEnabled();

                    sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyVariables]");
                    sessionNew.setAutoCommit(false);
                    sessionNew.beginTransaction();

                    // single & main cyclic
                    for (DBItemDailyPlanOrder item : items) {
                        DBItemDailyPlanVariable variables = submittedVariables.get(item.getId());

                        // main cyclic
                        if (item.isCyclic()) {
                            String newPart = submittedCyclicNewParts.get(item.getId());
                            List<DBItemDailyPlanOrder> cyclic = submittedCyclic.get(item.getId());

                            // TODO how check if already executed ?..
                            // when a cyclic workflow ???
                            // delete executed ???
                            boolean hasCyclicExecuted = false;
                            List<DBItemDailyPlanOrder> cyclic2Submit = new ArrayList<>();
                            for (DBItemDailyPlanOrder cyclicItem : cyclic) {
                                if (now.getTime() > cyclicItem.getPlannedStart().getTime()) {
                                    hasCyclicExecuted = true;
                                    if (isDebugEnabled) {
                                        try {
                                            LOGGER.debug(String.format("[modifyVariables][%s][skip submit]now() > planned start(%s)", cyclicItem
                                                    .getControllerId(), SOSDate.getDateTimeAsString(now), SOSDate.getDateTimeAsString(cyclicItem
                                                            .getPlannedStart())));
                                        } catch (Throwable ee) {
                                        }
                                    }
                                } else {
                                    cyclic2Submit.add(cyclicItem);
                                }
                            }

                            String newOrderId = result.getAdditionalProperties().get(item.getOrderId());
                            if (hasCyclicExecuted) {
                                if (variables != null) {
                                    DBItemDailyPlanVariable variablesNew = new DBItemDailyPlanVariable();
                                    variablesNew.setControllerId(in.getControllerId());
                                    variablesNew.setOrderId(newOrderId);
                                    variablesNew.setVariableValue(variables.getVariableValue());
                                    variablesNew.setModified(new Date());
                                    variablesNew.setCreated(new Date());
                                    sessionNew.save(variablesNew);

                                    variables = variablesNew;
                                }
                            }

                            modifyVariables(in, variables, sessionNew, newOrderId, orderPreparation);
                            for (DBItemDailyPlanOrder cyclicItem : cyclic2Submit) {
                                cyclicItem.setSubmitted(false);
                                cyclicItem.setOrderId(OrdersHelper.getNewFromOldOrderId(cyclicItem.getOrderId(), newPart));
                                cyclicItem.setModified(new Date());
                                sessionNew.update(cyclicItem);

                                toSubmit.add(cyclicItem);
                            }
                        } else { // single start
                            String newOrderId = result.getAdditionalProperties().get(item.getOrderId());
                            modifyVariables(in, variables, sessionNew, newOrderId, orderPreparation);

                            item.setSubmitted(false);
                            item.setOrderId(newOrderId);
                            item.setModified(new Date());
                            sessionNew.update(item);

                            toSubmit.add(item);
                        }
                    }
                    sessionNew.commit();
                    sessionNew.close();
                    sessionNew = null;

                    submitOrdersToController(toSubmit, in.getForceJobAdmission());
                    notifyAndStoreAuditLogDetails(in, items, auditlog);
                } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                        | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                        | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                    Globals.rollback(sessionNew);
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
                } finally {
                    Globals.disconnect(sessionNew);
                }
            });
        } else {
            notifyAndStoreAuditLogDetails(in, items, auditlog);
        }
        return result;
    }

    private void modifyVariables(DailyPlanModifyOrder in, DBItemDailyPlanVariable variables, SOSHibernateSession session, String orderId,
            Requirements orderPreparation) throws SOSHibernateException, IOException {
        String current = variables == null ? null : variables.getVariableValue();
        String modified = updateVariables(current, in.getVariables(), in.getRemoveVariables(), orderPreparation);

        if (modified != null) {
            if (variables == null) {
                variables = new DBItemDailyPlanVariable();
                variables.setControllerId(in.getControllerId());
                variables.setOrderId(orderId);
                variables.setVariableValue(modified);
                variables.setModified(new Date());
                variables.setCreated(new Date());
                session.save(variables);
            } else {
                variables.setOrderId(orderId);
                variables.setVariableValue(modified);
                variables.setModified(new Date());
                session.update(variables);
            }
        }
    }

    private void notifyAndStoreAuditLogDetails(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog) {
        EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), in.getDailyPlanDate()));
        OrdersHelper.storeAuditLogDetails(items.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), in
                .getControllerId())).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                        either2, getAccessToken(), getJocError(), in.getControllerId()));
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

//        if (isCyclicOrders(mainItems)) {
//            if (in.getCycle() == null || in.getCycle().getRepeat() == null) {
//                throw new JocMissingRequiredParameterException("Modify Start Time: missing cyclic definition.");
//            }
//            DBItemDailyPlanSubmission submission = insertNewSubmission(in.getControllerId(), mainItems.get(0).getDailyPlanDate(getSettings()
//                    .getTimeZone()));
//            for (DBItemDailyPlanOrder mainItem : mainItems) {
//                String newOrderId = modifyStartTimeCycle(in, submission, mainItem, auditlog);
//                result.getAdditionalProperties().put(mainItem.getOrderId(), newOrderId == null ? "" : newOrderId);
//            }
//        } else {
//            if (in.getScheduledFor() == null) {
//                throw new JocMissingRequiredParameterException("Modify Start Time: missing start time.");
//            }
//            
//            in.initScheduledFor();
//            
//            // can have multiple items - of the same schedule or workflow
//            result = modifyStartTimeSingle(in, mainItems, auditlog, zoneId);
//        }
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
        timeFormatter.setTimeZone(TimeZone.getTimeZone(timeZone == null ? "Etc/UTC" : timeZone));
        return timeFormatter.format(date);
    }
    
    private static String getPeriodRepeat(Long repeat) {
        timeFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return timeFormatter.format(Date.from(Instant.ofEpochMilli(0).plusSeconds(repeat)));
    }
    
    private static String getDailyPlanDate(Instant plannedStart, String timeZone, long periodBeginSeconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        return format.format(Date.from(plannedStart.minusSeconds(periodBeginSeconds)));
    }
    
    private OrderIdMap modifyStartTimeSingle(ModifyOrdersHelper in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog,
            ZoneId zoneId) throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException,
            SOSInvalidDataException, SOSHibernateException {
        
        Set<Long> submissionIds = items.stream().map(DBItemDailyPlanOrder::getSubmissionHistoryId).collect(Collectors.toSet());
        Set<String> oldDailyPlanDates = items.stream().map(DBItemDailyPlanOrder::getOrderId).map(s -> s.substring(1, 11)).collect(Collectors.toSet());
        
        SOSHibernateSession session = null;
        boolean isBulkOperation = items.size() > 1;
        final String settingTimeZone = getSettings().getTimeZone();
        Instant now = Instant.now();
        Long settingPeriodBeginSecondsOpt = JobSchedulerDate.getSecondsOfHHmmss(getSettings().getPeriodBegin());
        final long settingPeriodBeginSeconds = settingPeriodBeginSecondsOpt != null ? settingPeriodBeginSecondsOpt.longValue() : 0;
        //Set<String> dailyPlanDates = new HashSet<>();
        Map<String, DBItemDailyPlanSubmission> dailyPlanSubmissions = new HashMap<>();
        OrderIdMap result = new OrderIdMap();
        Map<String, String> cycleOrderIdMap = new HashMap<>();
        //Map<String, TreeSet<DBItemDailyPlanOrder>> cyclicOrders = new HashMap<>();
        Set<DBItemDailyPlanOrder> allItems = new HashSet<>();
        Set<Long> toDelete = new HashSet<>();
        
        // true if cyclic
        Map<Boolean, List<DBItemDailyPlanOrder>> itemsMap = items.stream().collect(Collectors.groupingBy(DBItemDailyPlanOrder::isCyclic));
        
        try {
            //boolean hasCycle = itemsMap.containsKey(Boolean.TRUE); //  items.stream().anyMatch(DBItemDailyPlanOrder::isCyclic);
            //DBLayerDailyPlannedOrders dbLayer = null;
            //if (hasCycle) {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTime]");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            //}
            
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
                    
                    String dailyPlanDateOfFirst = firstOrderOfCycle.getDailyPlanDate("Etc/UTC", 0);
                    //dailyPlanDates.add(dailyPlanDateOfFirst);
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

                        String dailyPlanDateOfLast = lastOrderOfCycle.getDailyPlanDate(settingTimeZone, settingPeriodBeginSeconds);
                        //dailyPlanDates.add(dailyPlanDateOfLast);
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
                        
                        String dailyPanDateOfFirst = getDailyPlanDate(newPlannedStartOfFirst, settingTimeZone, settingPeriodBeginSeconds);
                        
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
                        
                        if (!dailyPlanSubmissions.containsKey(dailyPanDateOfFirst)) {
                            DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), dailyPanDateOfFirst);
                            session.save(submission);
                            dailyPlanSubmissions.put(dailyPanDateOfFirst, submission);
                        }

                        modifyStartTimeCycle(in, dailyPanDateOfFirst, cycle, item, cyclicOrdersOfItem, dailyPlanSubmissions.get(dailyPanDateOfFirst),
                                auditlog).ifPresent(newOrderId -> {
                                    result.getAdditionalProperties().put(item.getOrderId(), newOrderId);
                                });
                        
//                        dailyPlanDates.add(dailyPanDateOfFirst);
//                        
//                        for (DBItemDailyPlanOrder cItem : cyclicOrdersOfItem.descendingSet()) {
//                            Instant newPlannedStart = in.getNewPlannedStart(cItem.getPlannedStart());
//                            // TODO if (newPlannedStart.isBefore(now)) { some first of the cycle can be in the past, the rest should planned
//                            if (newPlannedStart.isBefore(now)) {
//                                //
//                            } else {
//                                if (item.getExpectedEnd() != null) {
//                                    long expectedDuration = cItem.getExpectedEnd().getTime() - cItem.getPlannedStart().getTime();
//                                    cItem.setExpectedEnd(Date.from(newPlannedStart.plusMillis(expectedDuration)));
//                                }
//                                cItem.setPlannedStart(Date.from(newPlannedStart));
//                                cItem.setIsLastOfCyclic(true);
//
//                                String newOrderId = OrdersHelper.generateNewFromOldOrderId(cItem.getOrderId(), dailyPanDateOfFirst, zoneId);
//                                result.getAdditionalProperties().put(item.getOrderId(), newOrderId);
//                                cycleOrderIdMap.put(cItem.getOrderId(), newOrderId);
//                            }
//                        }
//                        
//                        allItems.addAll(cyclicOrdersOfItem);
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
                String dailyPlanDate = item.getDailyPlanDate(settingTimeZone, settingPeriodBeginSeconds);
                //dailyPlanDates.add(dailyPlanDate);
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

        // OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), itemsMap.get(Boolean.FALSE)).thenAccept(either -> {
        OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), allItems).thenAccept(either -> {
            SOSHibernateSession sessionNew = null;
            try {
                
                //Map<String, Long> submissionHistoryIds = new HashMap<>();
                sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeSingle]");
                sessionNew.setAutoCommit(false);
                
//                for (String dailyPlanDate: dailyPlanDates) {
//                    DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), dailyPlanDate);
//                    sessionNew.beginTransaction();
//                    sessionNew.save(submission);
//                    sessionNew.commit();
//                    submissionHistoryIds.put(dailyPlanDate, submission.getId());
//                }
                
                sessionNew.beginTransaction();
                DBLayerOrderVariables ovDbLayer = new DBLayerOrderVariables(sessionNew);
                List<DBItemDailyPlanOrder> toSubmit = new ArrayList<>();
                for (DBItemDailyPlanOrder item : allItems) {
                    
                    if (item.isCyclic()) {
                        if (!item.isLastOfCyclic()) { // only one item of cycle will be update. All other will be deleted
                            sessionNew.delete(item);
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
                            ovDbLayer.update(item.getControllerId(), oldOrderId, newOrderId, true);
                        }
                    } else {
                        ovDbLayer.update(item.getControllerId(), oldOrderId, newOrderId);
                    }
                    
//                    if (submissionHistoryIds.size() == 1) {
//                        item.setSubmissionHistoryId(submissionHistoryIds.values().iterator().next());
//                    } else if (submissionHistoryIds.size() > 1) {
//                        Long submissionHistoryId = submissionHistoryIds.get(item.getDailyPlanDate());
//                        if (submissionHistoryId == null) {
//                            item.setSubmissionHistoryId(submissionHistoryIds.values().iterator().next());
//                        }
//                    }
                    if (dailyPlanSubmissions.size() == 1) {
                        item.setSubmissionHistoryId(dailyPlanSubmissions.values().iterator().next().getId());
                    } else if (dailyPlanSubmissions.size() > 1) {
                        DBItemDailyPlanSubmission submission = dailyPlanSubmissions.get(item.getDailyPlanDate());
                        if (submission == null) {
                            item.setSubmissionHistoryId(dailyPlanSubmissions.values().iterator().next().getId());
                        } else {
                            item.setSubmissionHistoryId(submission.getId());
                        }
                    }
                    
                    item.setModified(new Date());
                    item.setOrderId(newOrderId);
                    
                    if (toDelete.contains(item.getId())) {
                        item.setSubmitted(false); 
                    }

                    if (item.getSubmitted()) {
                        item.setSubmitted(false);

                        toSubmit.add(item);
                    }
                    sessionNew.update(item);
                }
                sessionNew.commit();
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(sessionNew);
                for (Long submissionId : submissionIds) {
                    deleteNotUsedSubmission(sessionNew, dbLayer, in.getControllerId(), submissionId);
                }
                sessionNew.close();
                sessionNew = null;
                
                if (toSubmit.size() > 0) {
                    submitOrdersToController(toSubmit, in.getForceJobAdmission());
                }
                
                dailyPlanSubmissions.keySet().forEach(dailyPlanDate -> EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), dailyPlanDate)));
                oldDailyPlanDates.forEach(dailyPlanDate -> EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), dailyPlanDate)));

                OrdersHelper.storeAuditLogDetails(allItems.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), in
                        .getControllerId())).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), in.getControllerId()));

            } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                    | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                    | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                Globals.rollback(sessionNew);
                ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
            } finally {
                Globals.disconnect(sessionNew);
            }
        });
        return result;
    }
    
    private Optional<String> modifyStartTimeCycle(ModifyOrdersHelper in, DBItemDailyPlanOrder mainItem,
            TreeSet<DBItemDailyPlanOrder> cyclicOrdersOfItem, DBItemJocAuditLog auditlog) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        return modifyStartTimeCycle(in, in.getScheduledFor(), in.getCycle(), mainItem, cyclicOrdersOfItem, null, auditlog);
    }
    
    private Optional<String> modifyStartTimeCycle(ModifyOrdersHelper in, String dailyplanDate, Cycle cycle, DBItemDailyPlanOrder mainItem,
            TreeSet<DBItemDailyPlanOrder> cyclicOrdersOfItem, DBItemDailyPlanSubmission submission, DBItemJocAuditLog auditlog)
            throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        Long oldSubmissionId = mainItem.getSubmissionHistoryId();
        String oldDailyPlanDate = mainItem.getOrderId().substring(1, 11);

        SOSHibernateSession session = null;
        Map<PlannedOrderKey, PlannedOrder> generatedOrders = Collections.emptyMap();
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeCycle][" + mainItem.getOrderId() + "]");
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
            
            // remove not submitted
            session.beginTransaction();
            dbLayer.deleteCascading(mainItem, false);
            session.commit();
            if (submission == null) {
                submission = insertNewSubmission(in.getControllerId(), dailyplanDate, session);
            }
            
            DailyPlanRunner runner = getDailyPlanRunner(mainItem.getSubmitted(), submission.getSubmissionForDate());
            OrderListSynchronizer synchronizer = calculateStartTimes(in, cycle, runner, submission, mainItem, variable);
            synchronizer.substituteOrderIds();
            generatedOrders = synchronizer.getPlannedOrders();
            
            // get submitted
            List<DBItemDailyPlanOrder> submitted = cyclicOrdersOfItem.stream().filter(DBItemDailyPlanOrder::getSubmitted).collect(Collectors.toList());

            if (submitted != null && submitted.size() > 0) {
                session.close();
                session = null;

                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), submitted);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), in.getControllerId());
                    if (either.isRight()) {
                        // remove submitted & old submission
                        SOSHibernateSession sessionNew = null;
                        try {
                            sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeCycle][removeSubmitted]");
                            sessionNew.setAutoCommit(false);
                            sessionNew.beginTransaction();
                            DBLayerDailyPlannedOrders dbLayerNew = new DBLayerDailyPlannedOrders(sessionNew);
                            dbLayerNew.deleteCascading(mainItem, true);
                            sessionNew.commit();
                            deleteNotUsedSubmission(sessionNew, dbLayerNew, in.getControllerId(), oldSubmissionId);
                        } catch (Exception e) {
                            Globals.rollback(sessionNew);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
                        } finally {
                            Globals.disconnect(sessionNew);
                        }
                        // can't returns result ...
                        //recreateCyclicOrder(in, newSubmission, mainItem, variable, auditlog);
                        try {
                            runner.addPlannedOrderToControllerAndDB(in.getControllerId(), dailyplanDate, mainItem.getSubmitted(), synchronizer);
                        } catch (Exception e) {
                            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
                        }
                        
                        EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), oldDailyPlanDate));
                        EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), dailyplanDate));
                    }
                });
            } else {
                // remove old submission
                deleteNotUsedSubmission(session, dbLayer, in.getControllerId(), oldSubmissionId);
                session.close();
                session = null;

                // generate orders
                //generatedOrders = recreateCyclicOrder(in, newSubmission, mainItem, variable, auditlog);
                runner.addPlannedOrderToControllerAndDB(in.getControllerId(), dailyplanDate, mainItem.getSubmitted(), synchronizer);
                
                EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), oldDailyPlanDate));
                EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), dailyplanDate));
                
            }
            
            Set<AuditLogDetail> auditLogDetails = new HashSet<>();
            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), mainItem
                        .getControllerId()));
            }
            
            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                    either2, getAccessToken(), getJocError(), mainItem.getControllerId()));
            
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), mainItem.getControllerId());
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

//    private boolean isCyclicOrders(List<DBItemDailyPlanOrder> items) throws Exception {
//        boolean hasSingle = false;
//        boolean hasCyclic = false;
//        for (DBItemDailyPlanOrder item : items) {
//            if (hasSingle && hasCyclic) {
//                break;
//            }
//            if (OrdersHelper.isCyclicOrderId(item.getOrderId())) {
//                hasCyclic = true;
//            } else {
//                hasSingle = true;
//            }
//        }
//        if (hasSingle && hasCyclic) {
//            throw new Exception("Modify Start Time operation is not allowed. Single and Cyclic orders detected.");
//        }
//        return hasCyclic;
//    }
    
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
    
    private OrderListSynchronizer calculateStartTimes(DailyPlanModifyOrder in, Cycle cycle, DailyPlanRunner runner,
            DBItemDailyPlanSubmission newSubmission, final DBItemDailyPlanOrder mainItem, DBItemDailyPlanVariable variable) {

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
            if (orderParameterisation.getVariables().getAdditionalProperties().size() > 0) {
                schedule.getOrderParameterisations().add(orderParameterisation);
            }

            schedule.setCalendars(new ArrayList<AssignedCalendars>());
            AssignedCalendars calendars = new AssignedCalendars();
            Calendar calendar = getCalendarById(mainItem.getCalendarId());
            calendars.setCalendarName(calendar.getName());
            calendars.setPeriods(new ArrayList<Period>());
            calendars.setTimeZone(in.getTimeZone() == null ? "Etc/UTC" : in.getTimeZone());
            Period period = new Period();
            period.setBegin(cycle.getBegin());
            period.setEnd(cycle.getEnd());
            period.setRepeat(cycle.getRepeat());
            calendars.getPeriods().add(period);
            schedule.getCalendars().add(calendars);

            DailyPlanScheduleWorkflow w = new DailyPlanScheduleWorkflow(mainItem.getWorkflowName(), mainItem.getWorkflowPath(), null);
            DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(w));

            return runner.calculateStartTimes(StartupMode.manual, in.getControllerId(), Arrays.asList(dailyPlanSchedule), SOSDate.getDateAsString(
                    newSubmission.getSubmissionForDate()), newSubmission, calendar.getId(), getJocError(), getAccessToken());

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
                | ExecutionException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
        }
        return runner.getEmptySynchronizer();
    }

//    private Map<PlannedOrderKey, PlannedOrder> recreateCyclicOrder(DailyPlanModifyOrder in, DBItemDailyPlanSubmission newSubmission,
//            final DBItemDailyPlanOrder mainItem, DBItemDailyPlanVariable variable, DBItemJocAuditLog auditlog) {
//        String controllerId = in.getControllerId();
////        String dDate = in.getDailyPlanDate();
////        if (dDate == null) {
////            dDate = OrdersHelper.getDateFromOrderId(mainItem.getOrderId());
////        }
//
//        LOGGER.debug("recreateCyclicOrder: main orderId=" + mainItem.getOrderId());
//
//        Map<PlannedOrderKey, PlannedOrder> generatedOrders = null;
//        try {
//            Schedule schedule = new Schedule();
//            schedule.setVersion("");
//            schedule.setPath(mainItem.getSchedulePath());
//            schedule.setWorkflowNames(Arrays.asList(mainItem.getWorkflowName()));
//            if (JocInventory.SCHEDULE_CONSIDER_WORKFLOW_NAME) {
//                schedule.setWorkflowName(mainItem.getWorkflowName());
//            }
//            schedule.setTitle("");
//            schedule.setDocumentationName("");
//            schedule.setSubmitOrderToControllerWhenPlanned(mainItem.getSubmitted());
//            schedule.setPlanOrderAutomatically(true);
//            schedule.setOrderParameterisations(new ArrayList<OrderParameterisation>());
//            OrderParameterisation orderParameterisation = new OrderParameterisation();
//            orderParameterisation.setOrderName(mainItem.getOrderName());
//            Variables variables = new Variables();
//            if (variable != null && variable.getVariableValue() != null) {
//                variables = Globals.objectMapper.readValue(variable.getVariableValue(), Variables.class);
//            }
//            // TODO order positions??
//            // orderParameterisation.setStartPosition(null);
//            // orderParameterisation.setEndPosition(null);
//            orderParameterisation.setVariables(variables);
//            if (orderParameterisation.getVariables().getAdditionalProperties().size() > 0) {
//                schedule.getOrderParameterisations().add(orderParameterisation);
//            }
//
//            schedule.setCalendars(new ArrayList<AssignedCalendars>());
//            AssignedCalendars calendars = new AssignedCalendars();
//            Calendar calendar = getCalendarById(mainItem.getCalendarId());
//            calendars.setCalendarName(calendar.getName());
//            calendars.setPeriods(new ArrayList<Period>());
//            calendars.setTimeZone(in.getTimeZone() == null ? "Etc/UTC" : in.getTimeZone());
//            Period period = new Period();
//            period.setBegin(in.getCycle().getBegin());
//            period.setEnd(in.getCycle().getEnd());
//            period.setRepeat(in.getCycle().getRepeat());
//            calendars.getPeriods().add(period);
//            schedule.getCalendars().add(calendars);
//
//            DailyPlanRunner runner = getDailyPlanRunner(mainItem.getSubmitted(), newSubmission.getSubmissionForDate());
//
//            DailyPlanScheduleWorkflow w = new DailyPlanScheduleWorkflow(mainItem.getWorkflowName(), mainItem.getWorkflowPath(), null);
//            DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(w));
//            
//            generatedOrders = runner.generateDailyPlan(StartupMode.manual, controllerId, Arrays.asList(dailyPlanSchedule), mainItem.getDailyPlanDate(
//                    getSettings().getTimeZone(), getSettings().getPeriodBegin()), newSubmission, mainItem.getSubmitted(), getJocError(), getAccessToken());
//
//            Set<AuditLogDetail> auditLogDetails = new HashSet<>();
//            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
//                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), controllerId));
//            }
//
//            EventBus.getInstance().post(new DailyPlanEvent(in.getControllerId(), SOSDate.getDateAsString(newSubmission.getSubmissionForDate())));
//
//            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
//                    either2, getAccessToken(), getJocError(), controllerId));
//        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
//                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
//                | ExecutionException e) {
//            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
//        }
//        return generatedOrders;
//    }

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

    private void submitOrdersToController(List<DBItemDailyPlanOrder> items, Boolean forceJobAdmission) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        LOGGER.debug("submitOrdersToController: size=" + items.size());

        if (items.size() > 0) {
            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            settings.setOverwrite(false);
            settings.setSubmit(true);
            settings.setTimeZone(getSettings().getTimeZone());
            settings.setPeriodBegin(getSettings().getPeriodBegin());

            DailyPlanRunner runner = new DailyPlanRunner(settings);
            runner.submitOrders(StartupMode.manual, items.get(0).getControllerId(), items, "", forceJobAdmission, getJocError(), getAccessToken());
        }
    }

    private DBItemDailyPlanSubmission insertNewSubmission(String controllerId, String dailyPlanDate, SOSHibernateSession session)
            throws SOSHibernateException, SOSInvalidDataException {
        DBItemDailyPlanSubmission item = newSubmission(controllerId, dailyPlanDate);
        boolean sessionIsNull = session == null;
        try {
            if (sessionIsNull) {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[insertNewSubmission][" + dailyPlanDate + "]");
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
        return item;
    }

    private String updateVariables(String current, Variables toUpdate, List<String> toRemove, Requirements orderPreparation) throws IOException {
        Variables vars = new Variables();
        if (!SOSString.isEmpty(current)) {
            try {
                vars = Globals.objectMapper.readValue(current, Variables.class);
            } catch (Throwable e) {
                LOGGER.warn("Illegal value " + current);
            }
        }

        Map<String, Object> map = vars.getAdditionalProperties();

        if (toUpdate != null) {
            map.putAll(toUpdate.getAdditionalProperties());
        }
        if (toRemove != null) {
            toRemove.forEach(k -> map.remove(k));
        }
        vars.setAdditionalProperties(map);
        vars = OrdersHelper.checkArguments(vars, orderPreparation);
        return Globals.objectMapper.writeValueAsString(vars);
    }
    
    private com.sos.inventory.model.workflow.Workflow getWorkflow(String controllerId, String workflowName) throws JsonParseException,
            JsonMappingException, IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            DeployedContent dbWorkflow = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowName);
            Globals.disconnect(connection);
            connection = null;
            if (dbWorkflow != null) {
                return JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
            }
        } finally {
            Globals.disconnect(connection);
        }
        return null;
    }

    private Map<String, List<Object>> getLabelMap(com.sos.inventory.model.workflow.Workflow workflow) throws JsonParseException, JsonMappingException,
            IOException {
        if (workflow != null) {
            return WorkflowsHelper.getLabelToPositionsMap(workflow);
            //workflowsWithBlockPositions.put(dbWorkflow.getName(), WorkflowsHelper.getWorkflowBlockPositions(w.getInstructions()));
        }
        return Collections.emptyMap();
    }
    
    private Set<BlockPosition> getBlockPositions(com.sos.inventory.model.workflow.Workflow workflow) throws JsonParseException,
            JsonMappingException, IOException {
        if (workflow != null) {
            return WorkflowsHelper.getWorkflowBlockPositions(workflow.getInstructions());
        }
        return Collections.emptySet();
    }

}
