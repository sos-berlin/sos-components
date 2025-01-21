package com.sos.joc.classes.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.CaseWhen;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.When;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.history.DBItemHistoryOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryAddOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderStarted;
import com.sos.joc.event.bean.order.AddOrderEvent;
import com.sos.joc.event.bean.order.TerminateOrderEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;
import com.sos.joc.model.order.OrderV;

import io.vavr.control.Either;
import jakarta.persistence.TemporalType;
import js7.data.order.OrderId;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;

public class OrderTags {

    private static OrderTags instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTags.class);
    private static final TypeReference<Map<String,Set<String>>> typeRefAddOrderTags = new TypeReference<Map<String,Set<String>>>() {};

    private OrderTags() {
        EventBus.getInstance().register(this);
    }

    public static OrderTags getInstance() {
        if (instance == null) {
            instance = new OrderTags();
        }
        return instance;
    }
    
    @Subscribe({ HistoryOrderStarted.class })
    public void addHistoryIdToTags(HistoryOrderStarted evt) {
        if (!evt.getOrderId().contains("|")) { // not child order
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HistoryOrderStarted received: " + SOSString.toString(evt));
            }
            addHistoryIdToTags(evt.getControllerId(), evt.getOrderId(), (HistoryOrderBean) evt.getPayload());
        }
    }

    @Subscribe({ AddOrderEvent.class })
    public void addTagsToOrderbyFileOrderSourceOrAddOrderInstruction(AddOrderEvent evt) {
        boolean isChildOrder = evt.getOrderId().contains("|");
        if (!isChildOrder) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("AddOrderEvent received: " + SOSString.toString(evt));
            }
            addTagsToOrderbyFileOrderSourceOrAddOrderInstruction(evt.getControllerId(), evt.getOrderId(), evt.getWorkflowName());
        }
    }
    
    @Subscribe({ TerminateOrderEvent.class })
    public void deleteTagsFromAddOrderInstruction(TerminateOrderEvent evt) {
        boolean isChildOrder = evt.getOrderId().contains("|");
        if (!isChildOrder) {
            //LOGGER.trace("TerminateOrderEvent received: " + SOSString.toString(evt));
            deleteTagsFromAddOrderInstruction(evt.getControllerId(), evt.getOrderId());
        }
    }
    
    // public for test
    public void addTagsToOrderbyFileOrderSourceOrAddOrderInstruction(String controllerId, String orderId, String workflowName) {
        String orderIdModifier = orderId.substring(12, 13);
        if (orderIdModifier.equals("D")) {
            addTagsToOrderbyAddOrderInstruction(controllerId, orderId);
        } else if (orderIdModifier.equals("F")) {
            addTagsToOrderbyFileOrderSource(controllerId, orderId);
        }
        String addOrderTags = WorkflowRefs.getAddOrderTags(controllerId, workflowName);
        if (addOrderTags != null && !addOrderTags.isEmpty()) { // workflow has addOrder instructions
            storeAddOrderTags(orderId, addOrderTags);
        }
    }
    
    private synchronized void deleteTagsFromAddOrderInstruction(String controllerId, String orderId) {
        String orderIdModifier = orderId.substring(12, 13);
        if (orderIdModifier.equals("D")) {
            SOSHibernateSession connection = null;
            try {
                String orderIdPattern = getOrderIdPattern(orderId);
                boolean allAddOrderOrdersAreTerminated = Proxy.of(controllerId).currentState().ordersBy(o -> o.id().string().contains(orderIdPattern
                        + "!")).count() == 0L;
                //LOGGER.info("deleteAddOrderTags -> orderIdPattern: " + orderIdPattern + " are terminated: " + allAddOrderOrdersAreTerminated);
                if (allAddOrderOrdersAreTerminated) {
                    connection = Globals.createSosHibernateStatelessConnection("deleteAddOrderTags");
                    DBItemInventoryAddOrderTag dbItem = connection.get(DBItemInventoryAddOrderTag.class, Long.valueOf(orderIdPattern));
                    if (dbItem != null) {
                        connection.delete(dbItem);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[deleteAddOrderTags][" + orderId + "]: " + e.toString(), e);
            } finally {
                Globals.disconnect(connection);
            }
        }
    }
    
    private static String getOrderIdPattern(String orderId) {
        return orderId.substring(OrdersHelper.mainOrderIdLength).replaceFirst("(\\d+-)?(\\d+)!.*$", "$2");
    }
    
    private void addHistoryIdToTags(String controllerId, String orderId, HistoryOrderBean payload) {
        if (payload != null) {
            updateHistoryIdOfOrder(controllerId, orderId, payload.getHistoryId(), payload.getStartTime());
        }
    }

    private synchronized void storeAddOrderTags(String orderId, String addOrderTags) {
        //#2024-05-07#D08028140100-2024050708748127900!-test1
        /*
        String idPattern = "'#' ++ now(format='yyyy-MM-dd', timezone='%s') ++ '#D' ++ " + OrdersHelper.mainOrderIdControllerPattern + " ++ '"
        + sAddOrderIndex + "-' ++ replaceAll(replaceAll($js7OrderId, '^(" + datetimePattern
        + ")-.*$', '$1'), '\\D', \"\") ++ replaceAll(replaceAll($js7OrderId, '^" + datetimePattern
        + "-([^!]+!-)?(.*)$', '$2'), '^([^|]+).*', '!-$1')";
         */
        
        SOSHibernateSession connection = null;
        try {
            DBItemInventoryAddOrderTag dbAddOrderTagsItem = new DBItemInventoryAddOrderTag();
            Long orderIdPattern = Long.valueOf(OrdersHelper.getOrderIdMainPart(orderId).replaceAll("\\D", ""));
            dbAddOrderTagsItem.setOrderIdPattern(orderIdPattern);
            dbAddOrderTagsItem.setOrderTags(addOrderTags);
            //LOGGER.info("storeAddOrderTags: " + orderIdPattern + ", " + addOrderTags);
            
            connection = Globals.createSosHibernateStatelessConnection("storeAddOrderTags");
            DBItemInventoryAddOrderTag dbItem = connection.get(DBItemInventoryAddOrderTag.class, Long.valueOf(orderIdPattern));
            if (dbItem == null) { // else already exists
                connection.save(dbAddOrderTagsItem);
            }
            
        } catch (Exception e) {
            LOGGER.error("[storeAddOrderTags][" + orderId + "]: " + e.toString(), e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private synchronized void addTagsToOrderbyFileOrderSource(String controllerId, String orderId) {
        String orderWatchName = orderId.substring(OrdersHelper.mainOrderIdLength).replaceFirst("([^:]+):.*", "$1");
        // TODO it would be nice to know if fileOrderSource has tags before further processing
        SOSHibernateSession connection = null;
        try {
            DeployedContent fos = null;
            Optional<DeployedContent> fosOpt = WorkflowRefs.getFileOrderSources(controllerId).parallelStream().filter(f -> orderWatchName.equals(f
                    .getName())).findAny();
            if (fosOpt.isPresent()) {
                fos = fosOpt.get();
            }
            if (fos == null) {
                connection = Globals.createSosHibernateStatelessConnection("storeFileOrderTags");
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);

                fos = dbLayer.getDeployedInventory(controllerId, DeployType.FILEORDERSOURCE.intValue(), orderWatchName);
            }
            if (fos != null && fos.getContent().contains("\"tags\"")) {
                FileOrderSource fos2 = JocInventory.convertFileOrderSource(fos.getContent(), FileOrderSource.class);
                if (fos2.getTags() != null && !fos2.getTags().isEmpty()) {
                    if (connection == null) {
                        connection = Globals.createSosHibernateStatelessConnection("storeFileOrderTags");
                    }
                    connection.setAutoCommit(false);
                    Globals.beginTransaction(connection);
                    deleteTagsOfOrder(controllerId, orderId, connection); // if eventbus.post comes twice
                    addTagsOfOrder(controllerId, orderId, fos2.getTags(), connection, Date.from(Instant.now()));
                    Globals.commit(connection);
                }
            }

        } catch (Exception e) {
            Globals.rollback(connection);
            LOGGER.error("[storeFileOrderTags][" + orderId + "]: " + e.toString(), e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    private void addTagsToOrderbyAddOrderInstruction(String controllerId, String orderId) {
        SOSHibernateSession connection = null;
        try {
            String orderIdPattern = getOrderIdPattern(orderId);
            String addOrderIndex = orderId.substring(OrdersHelper.mainOrderIdLength - 3, OrdersHelper.mainOrderIdLength - 1);
            connection = Globals.createSosHibernateStatelessConnection("storeAddOrderTags");
            DBItemInventoryAddOrderTag dbItem = addTagsToOrderbyAddOrderInstruction(connection, orderIdPattern, addOrderIndex, controllerId, orderIdPattern);
            //JOC-1933 sometimes addOrder event of parent order comes around 10ms after the addAddOrder of the generated order event 
            //if addOrder instruction is first instruction
            int attempt = 0;
            while (attempt < 5 && dbItem == null) {
                attempt++;
                TimeUnit.MILLISECONDS.sleep(20);
                dbItem = addTagsToOrderbyAddOrderInstruction(connection, orderIdPattern, addOrderIndex, controllerId, orderIdPattern);
            }
        } catch (Exception e) {
            Globals.rollback(connection);
            LOGGER.error("[storeAddOrderTags][" + orderId + "]: " + e.toString(), e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private synchronized DBItemInventoryAddOrderTag addTagsToOrderbyAddOrderInstruction(SOSHibernateSession connection, String orderIdPattern,
            String addOrderIndex, String controllerId, String orderId) throws NumberFormatException, SOSHibernateException, JsonMappingException,
            JsonProcessingException {
        DBItemInventoryAddOrderTag dbItem = connection.get(DBItemInventoryAddOrderTag.class, Long.valueOf(orderIdPattern));
        if (dbItem != null) {
            Map<String, Set<String>> allTags = Globals.objectMapper.readValue(dbItem.getOrderTags(), typeRefAddOrderTags);
            Set<String> orderTags = allTags.getOrDefault(addOrderIndex, Collections.emptySet());
            if (!orderTags.isEmpty()) {
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                deleteTagsOfOrder(controllerId, orderId, connection); // if eventbus.post comes twice
                addTagsOfOrder(controllerId, orderId, orderTags, connection, Date.from(Instant.now()));
                Globals.commit(connection);
            }
        }
        return dbItem;
    }

    public static Either<Exception, Void> addAdhocOrderTags(String controllerId, Map<OrderV, Set<GroupedTag>> oTags) {
        if (controllerId != null && oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                // delete/insert
                connection = Globals.createSosHibernateStatelessConnection("storeOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                deleteTags(controllerId, oTags.keySet().stream().map(OrderV::getOrderId).distinct().collect(Collectors.toList()), connection);
                for (Map.Entry<OrderV, Set<GroupedTag>> oTag : oTags.entrySet()) {
                    Set<String> tags = oTag.getValue().stream().map(GroupedTag::getTag).collect(Collectors.toSet());
                    // TODO consider groups
                    addTagsOfOrder(controllerId, oTag.getKey().getOrderId(), tags, connection, Date.from(Instant.ofEpochMilli(oTag.getKey()
                            .getScheduledFor())));
                }
                Globals.commit(connection);
                return Either.right(null);
            } catch (Exception e) {
                Globals.rollback(connection);
                return Either.left(e);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Either.right(null);
    }
    
    public static Either<Exception, Void> addDailyPlanOrderTags(String controllerId, Map<DBItemDailyPlanOrder, Set<String>> oTags) {
        if (controllerId != null && oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                // delete/insert
                connection = Globals.createSosHibernateStatelessConnection("storeOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                deleteTags(controllerId, oTags.keySet().stream().map(DBItemDailyPlanOrder::getOrderId).distinct().collect(Collectors.toList()),
                        connection);

                // avoid duplicate entry cause of cyclic orders
                for (Map.Entry<String, Map.Entry<DBItemDailyPlanOrder, Set<String>>> oTag : oTags.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().getOrderId(), Function.identity(), (k, v) -> v)).entrySet()) {
                    addTagsOfOrder(controllerId, oTag.getKey(), oTag.getValue().getValue(), connection, oTag.getValue().getKey().getPlannedStart());
                }
                Globals.commit(connection);
                return Either.right(null);
            } catch (Exception e) {
                Globals.rollback(connection);
                return Either.left(e);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Either.right(null);
    }
    
    private static void addTagsOfOrder(String controllerId, String orderId, Set<String> tags, SOSHibernateSession connection, Date scheduledFor) {
        int i = 0;
        Map<String, Long> gts = Collections.emptyMap();
        try {
            gts = new InventoryOrderTagDBLayer(connection).getTags(tags).stream().distinct().collect(Collectors.toMap(
                    DBItemInventoryOrderTag::getName, DBItemInventoryOrderTag::getGroupId));
        } catch (Exception e) {
            // TODO log error
        }
        for (String tag : tags) {
            try {
                connection.save(new DBItemHistoryOrderTag(controllerId, orderId, tag, gts.getOrDefault(tag, 0L), ++i, scheduledFor));
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public static void deleteTags(String controllerId, List<String> orderIds, SOSHibernateSession connection) throws SOSHibernateException {
        if (controllerId != null && orderIds != null && !orderIds.isEmpty()) {

            int size = orderIds.size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    deleteTags(controllerId, SOSHibernate.getInClausePartition(i, orderIds), connection);
                }
            } else {

                StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                hql.append(" where orderId in (:orderIds)");
                hql.append(" and controllerId=:controllerId");

                Query<DBItemDailyPlanVariable> query = connection.createQuery(hql);
                query.setParameterList("orderIds", orderIds);
                query.setParameter("controllerId", controllerId);
                connection.executeUpdate(query);
            }
        }
    }
    
    public static void deleteTagsOfOrder(String controllerId, String orderId, SOSHibernateSession connection) {
        if (controllerId != null && orderId != null) {
            try {
                StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                hql.append(" where controllerId=:controllerId");
                hql.append(" and orderId=:orderId");

                Query<DBItemDailyPlanVariable> query = connection.createQuery(hql);
                query.setParameter("controllerId", controllerId);
                query.setParameter("orderId", orderId);
                connection.executeUpdate(query);
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public static Either<Exception, Void> updateTagsOfOrders(String controllerId, Map<OrderId, JFreshOrder> oldNewOrderIds) {

        if (controllerId != null && oldNewOrderIds != null && !oldNewOrderIds.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("updateOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                for (Map.Entry<OrderId, JFreshOrder> oldNewOrderId : oldNewOrderIds.entrySet()) {
                    updateOrderIdOfOrder(controllerId, oldNewOrderId.getKey().string(), oldNewOrderId.getValue().id().string(), connection);
                }
                Globals.commit(connection);
                return Either.right(null);
            } catch (Exception e) {
                Globals.rollback(connection);
                return Either.left(e);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Either.right(null);
    }
    
    public static int updateOrderIdOfOrder(String controllerId, String oldOrderId, String newOrderId, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        hql.append(" set orderId=:newOrderId");
        hql.append(" where controllerId=:controllerId");
        hql.append(" and orderId=:oldOrderId");

        Query<Integer> query = connection.createQuery(hql);
        query.setParameter("newOrderId", newOrderId);
        query.setParameter("oldOrderId", oldOrderId);
        query.setParameter("controllerId", controllerId);
        return connection.executeUpdate(query);
    }
    
    public static Either<Exception, Void> updateHistoryIdOfOrder(String controllerId, String orderId, Long historyId, Date startTime) {
        if (historyId != null && historyId > 0L && controllerId != null && orderId != null) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("updateOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                updateHistoryIdOfOrder(controllerId, orderId, historyId, startTime, connection);
                Globals.commit(connection);
                return Either.right(null);
            } catch (Exception e) {
                Globals.rollback(connection);
                return Either.left(e);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Either.right(null);
    }
    
    private static int updateHistoryIdOfOrder(String controllerId, String orderId, Long historyId, Date startTime, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        hql.append(" set historyId=:historyId");
        if (startTime != null) {
            hql.append(", startTime=:startTime"); 
        }
        hql.append(" where controllerId=:controllerId");
        hql.append(" and orderId=:orderId");

        Query<Integer> query = connection.createQuery(hql);
        query.setParameter("historyId", historyId);
        if (startTime != null) {
            query.setParameter("startTime", startTime);
        }
        query.setParameter("orderId", orderId);
        query.setParameter("controllerId", controllerId);
        return connection.executeUpdate(query);
    }
    
    public static void copyTagsOfOrder(String controllerId, String oldOrderId, String newOrderId, Date scheduledFor, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        hql.append(" where controllerId=:controllerId");
        hql.append(" and orderId=:oldOrderId");

        Query<DBItemHistoryOrderTag> query = connection.createQuery(hql);
        query.setParameter("oldOrderId", oldOrderId);
        query.setParameter("controllerId", controllerId);
        List<DBItemHistoryOrderTag> result = query.getResultList();
        if (result != null) {
            for (DBItemHistoryOrderTag oldItem : result) {
                DBItemHistoryOrderTag newItem = new DBItemHistoryOrderTag(oldItem.getControllerId(), newOrderId, oldItem.getTagName(), oldItem
                        .getGroupId(), oldItem.getOrdering(), scheduledFor);
                connection.save(newItem);
            }
        }
    }
    
    public static Stream<JOrder> filter(List<JOrder> jOrders, Map<String, Set<String>> orderTags, Set<String> requestedOrdertags)
            throws SOSHibernateException {
        if (orderTags == null || orderTags.isEmpty()) {
            return Stream.empty();
        }
        if (jOrders == null || jOrders.isEmpty()) {
            return Stream.empty();
        }
        return filter(jOrders.stream(), orderTags, requestedOrdertags);
    }

    private static Stream<JOrder> filter(Stream<JOrder> jOrders, Map<String, Set<String>> orderTags, Set<String> requestedOrdertags)
            throws SOSHibernateException {
        return jOrders.filter(o -> orderTags.containsKey(OrdersHelper.getParentOrderId(o.id().string()))).filter(o -> new HashSet<>(orderTags.get(
                OrdersHelper.getParentOrderId(o.id().string()))).removeAll(requestedOrdertags)); // removeAll return true if set is changed
    }
    
    
    public static Map<String, Set<String>> getTags(String controllerId, List<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        return getTags(false, controllerId, jOrders, connection);
    }
    
    // this function provides that returned Map<String, Set<String>> is final depends on withOrderTags
    public static Map<String, Set<String>> getTags(boolean forced, String controllerId, List<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (jOrders == null || jOrders.isEmpty()) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        return getTags(forced, controllerId, jOrders.stream(), connection);
    }
    
    public static Map<String, Set<String>> getTags(String controllerId, Stream<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        return getTags(false, controllerId, jOrders, connection);
    }
    
    public static Map<String, Set<String>> getTags(boolean forced, String controllerId, Stream<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (jOrders == null) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        return getTagsByOrderIds(forced, controllerId, jOrders.map(JOrder::id).map(OrderId::string), connection);
    }
    
    public static Set<String> getTagsByOrderId(String controllerId, String orderId, SOSHibernateSession connection)
            throws SOSHibernateException {
        return getTagsByOrderId(false, controllerId, orderId, connection);
    }
    
    public static Map<String, Set<String>> getTagsByOrderIds(String controllerId, Stream<String> orderIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        return getTagsByOrderIds(false, controllerId, orderIds, connection);
    }
    
    public static Set<String> getTagsByOrderId(boolean forced, String controllerId, String orderId, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderId == null) {
            return Collections.emptySet();
        }
        if (controllerId == null) {
            return Collections.emptySet();
        }
        if (!forced && !withTagsDisplayedAsOrderId()) {
            return Collections.emptySet();
        }
        return getTagsByOrderIds(controllerId, Arrays.asList(orderId), connection).getOrDefault(orderId, Collections.emptySet());
    }
    
    public static Map<String, Set<String>> getTagsByOrderIds(boolean forced, String controllerId, Stream<String> orderIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderIds == null) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        if (!forced && !withTagsDisplayedAsOrderId()) {
            return Collections.emptyMap();
        }
        return getTagsByOrderIds(controllerId, orderIds.distinct().collect(Collectors.toList()), connection);
    }
    
    private static Map<String, Set<String>> getTagsByOrderIds(String controllerId, List<String> orderIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        if (controllerId == null) {
            return Collections.emptyMap();
        }
        
        Collection<List<String>> chunkedOrderIds = getChunkedCollection(orderIds);

        StringBuilder hql = new StringBuilder("select t.orderId as orderId, t.tagName as tagName, g.name as groupName from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_TAGS).append(" t left join ").append(DBLayer.DBITEM_INV_TAG_GROUPS);
        hql.append(" g on t.groupId = g.id");
        
        List<String> clauses = new ArrayList<>(2);
        if (!controllerId.isBlank()) {
            clauses.add("t.controllerId=:controllerId");
        }
        String clause = IntStream.range(0, chunkedOrderIds.size()).mapToObj(i -> "t.orderId in (:orderIds" + i + ")").collect(Collectors.joining(
                " or "));
        if (chunkedOrderIds.size() > 1) {
            clause = "(" + clause + ")";
        }
        clauses.add(clause);
        hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", ""))).append(" order by t.ordering");

        Query<DBItemHistoryOrderTag> query = connection.createQuery(hql.toString(), DBItemHistoryOrderTag.class);
        if (!controllerId.isBlank()) {
            query.setParameter("controllerId", controllerId);
        }
        AtomicInteger counter = new AtomicInteger();
        for (List<String> chunk : chunkedOrderIds) {
            query.setParameterList("orderIds" + counter.getAndIncrement(), chunk);
        }
        List<DBItemHistoryOrderTag> result = connection.getResultList(query);
        if (result == null) {
            return Collections.emptyMap();
        }
        return result.stream().collect(Collectors.groupingBy(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getGroupedTag,
                Collectors.toCollection(LinkedHashSet::new))));
    }
    
    public static List<DBItemHistoryOrderTag> getTagsByTagNames(Collection<String> tagNames, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<List<String>> chunkedTagNames = getChunkedCollection(tagNames);

        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        
        String clause = IntStream.range(0, chunkedTagNames.size()).mapToObj(i -> "tagName in (:tagNames" + i + ")").collect(Collectors.joining(
                " or "));
        hql.append(" where " + clause);

        Query<DBItemHistoryOrderTag> query = connection.createQuery(hql.toString(), DBItemHistoryOrderTag.class);
        AtomicInteger counter = new AtomicInteger();
        for (List<String> chunk : chunkedTagNames) {
            query.setParameterList("tagNames" + counter.getAndIncrement(), chunk);
        }
        List<DBItemHistoryOrderTag> result = connection.getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    public static Map<String, Set<String>> getTagsByHistoryIds(String controllerId, List<Long> historyIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (historyIds == null || historyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Collection<List<Long>> chunkedHistoryIds = getChunkedCollection(historyIds);
        return chunkedHistoryIds.stream().map(chunk -> getTagsByChunkOfHistoryIds(controllerId, chunk, connection)).flatMap(List::stream).collect(
                Collectors.groupingBy(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getGroupedTag, Collectors
                        .toSet())));
    }
    
    private static List<DBItemHistoryOrderTag> getTagsByChunkOfHistoryIds(String controllerId, List<Long> historyIds,
            SOSHibernateSession connection) {
        if (historyIds == null || historyIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            StringBuilder hql = new StringBuilder("select t.orderId as orderId, t.tagName as tagName, g.name as groupName from ");
            hql.append(DBLayer.DBITEM_HISTORY_ORDER_TAGS).append(" t left join ").append(DBLayer.DBITEM_INV_TAG_GROUPS);
            hql.append(" g on t.groupId = g.id");
            List<String> clauses = new ArrayList<>(3);
            if (controllerId != null && !controllerId.isBlank()) {
                clauses.add("t.controllerId=:controllerId");
            }
            clauses.add("t.historyId != 0");
            clauses.add("t.historyId in (:historyIds)");
            hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));

            Query<DBItemHistoryOrderTag> query = connection.createQuery(hql.toString(), DBItemHistoryOrderTag.class);
            if (controllerId != null && !controllerId.isBlank()) {
                query.setParameter("controllerId", controllerId);
            }
            query.setParameterList("historyIds", historyIds);
            List<DBItemHistoryOrderTag> result = connection.getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public static Set<String> getTagsOfOrderId(String controllerId, String orderId, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderId == null) {
            return Collections.emptySet();
        }
        if (controllerId == null) {
            return Collections.emptySet();
        }
        
        connection = Globals.createSosHibernateStatelessConnection(OrderTags.class.getSimpleName());
        
        StringBuilder hql = new StringBuilder("select new ").append(GroupedTag.class.getName());
        hql.append("(g.name, t.tagName) from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS).append(" t left join ");
        hql.append(DBLayer.DBITEM_INV_TAG_GROUPS).append(" g on t.groupId = g.id");
        hql.append(" where t.controllerId=:controllerId");
        hql.append(" and t.orderId=:orderId").append(" order by t.ordering");

        Query<GroupedTag> query = connection.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", orderId);
        List<GroupedTag> result = connection.getResultList(query);
        if (result == null) {
            return Collections.emptySet();
        }
        return result.stream().map(GroupedTag::toString).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static List<String> getMainOrderIdsByTags(String controllerId, Set<String> oTags) {
        if (oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("getOrderTags");
                return getMainOrderIdsByTags(controllerId, oTags, connection);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Collections.emptyList();
    }
    
    public static List<Long> getHistoryIdsByTags(String controllerId, Set<String> oTags, Integer limit, Date startFrom, Date startTo) {
        if (oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("getOrderTags");
                return getHistoryIdsByTags(controllerId, oTags, limit, startFrom, startTo, connection);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Collections.emptyList();
    }
    
    public static List<String> getMainOrderIdsByTags(String controllerId, List<String> oTags) {
        if (oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("getOrderTags");
                return getMainOrderIdsByTags(controllerId, oTags, connection);
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Collections.emptyList();
    }
    
    public static List<String> getMainOrderIdsByTags(String controllerId, Set<String> oTags, SOSHibernateSession connection) {
        if (connection == null) {
            return getMainOrderIdsByTags(controllerId, oTags);
        } else {
            List<String> _oTags = oTags == null ? null : oTags.stream().collect(Collectors.toList());
            return getMainOrderIdsByTags(controllerId, _oTags, connection);
        }
    }
    
    public static List<String> getMainOrderIdsByTags(String controllerId, List<String> oTags, SOSHibernateSession connection) {
        if (connection == null) {
            return getMainOrderIdsByTags(controllerId, oTags);
        }
        if (oTags != null && !oTags.isEmpty()) {
            int size = oTags.size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                List<String> r = new ArrayList<>();
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    r.addAll(getMainOrderIdsByTags(controllerId, SOSHibernate.getInClausePartition(i, oTags), connection));
                }
                return r;
            } else {
                try {
                    StringBuilder hql = new StringBuilder("select orderId from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                    List<String> cause = new ArrayList<>(2);
                    if (controllerId != null && !controllerId.isBlank()) {
                        cause.add("controllerId=:controllerId");
                    }
                    cause.add("tagName in (:tagNames)");
                    hql.append(cause.stream().collect(Collectors.joining(" and ", " where ", "")));
                    hql.append(" group by orderId");

                    Query<String> query = connection.createQuery(hql.toString());
                    if (controllerId != null && !controllerId.isBlank()) {
                        query.setParameter("controllerId", controllerId);
                    }
                    query.setParameterList("tagNames", oTags);
                    List<String> result = connection.getResultList(query);
                    if (result == null) {
                        return Collections.emptyList();
                    }
                    return result;
                } catch (SOSHibernateInvalidSessionException ex) {
                    throw new DBConnectionRefusedException(ex);
                } catch (Exception ex) {
                    throw new DBInvalidDataException(ex);
                }
            }
        }
        return Collections.emptyList();
    }
    
    public static List<Long> getHistoryIdsByTags(String controllerId, Set<String> oTags, Integer limit, Date startFrom, Date startTo,
            SOSHibernateSession connection) {
        if (connection == null) {
            return getHistoryIdsByTags(controllerId, oTags, limit, startFrom, startTo);
        }
        
        if (limit == null || limit > WebserviceConstants.HISTORY_RESULTSET_LIMIT) {
            limit = WebserviceConstants.HISTORY_RESULTSET_LIMIT;
        }

        if (oTags != null && !oTags.isEmpty()) {
            
            if (oTags.size() > 2000) { // TODO 2000? maybe a different number? 
                /* MS SQL Server: 8003 The incoming request has too many parameters. The server supports a maximum of 2100 parameters. 
                 * Reduce the number of parameters and resend the request.
                 */
                try {
                    StringBuilder hql = new StringBuilder("select historyId, tagName from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                    List<String> clauses = new ArrayList<>(4);
                    if (controllerId != null && !controllerId.isBlank()) {
                        clauses.add("controllerId=:controllerId");
                    }
                    clauses.add("historyId != 0");
                    if (startFrom != null) {
                        clauses.add("startTime >= :startTimeFrom");
                    }
                    if (startTo != null) {
                        clauses.add("startTime < :startTimeTo");
                    }
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                    
                    Query<Object[]> query = connection.createQuery(hql.toString());
                    if (controllerId != null && !controllerId.isBlank()) {
                        query.setParameter("controllerId", controllerId);
                    }
                    if (startFrom != null) {
                        query.setParameter("startTimeFrom", startFrom, TemporalType.TIMESTAMP);
                    }
                    if (startTo != null) {
                        query.setParameter("startTimeTo", startTo, TemporalType.TIMESTAMP);
                    }
                    List<Object[]> result = connection.getResultList(query);
                    if (result == null) {
                        return Collections.emptyList();
                    }
                    Map<String, List<Long>> m = result.stream().collect(Collectors.groupingBy(item -> (String) item[1], Collectors.mapping(
                            item -> (Long) item[0], Collectors.toList())));
                    m.keySet().retainAll(oTags);
                    return m.values().stream().flatMap(List::stream).distinct().sorted(Comparator.reverseOrder()).limit(limit).collect(Collectors
                            .toList());
                } catch (SOSHibernateInvalidSessionException ex) {
                    throw new DBConnectionRefusedException(ex);
                } catch (Exception ex) {
                    throw new DBInvalidDataException(ex);
                }
                
            } else {
                Collection<List<String>> chunkedOTags = getChunkedCollection(oTags);

                try {
                    StringBuilder hql = new StringBuilder("select historyId from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                    List<String> clauses = new ArrayList<>(5);
                    if (controllerId != null && !controllerId.isBlank()) {
                        clauses.add("controllerId=:controllerId");
                    }
                    clauses.add("historyId != 0");
                    if (startFrom != null) {
                        clauses.add("startTime >= :startTimeFrom");
                    }
                    if (startTo != null) {
                        clauses.add("startTime < :startTimeTo");
                    }
                    String clause = IntStream.range(0, chunkedOTags.size()).mapToObj(i -> "tagName in (:tagNames" + i + ")").collect(Collectors
                            .joining(" or "));
                    if (chunkedOTags.size() > 1) {
                        clause = "(" + clause + ")";
                    }
                    clauses.add(clause);
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                    hql.append(" group by historyId order by historyId desc");

                    Query<Long> query = connection.createQuery(hql.toString());
                    query.setMaxResults(limit);
                    if (controllerId != null && !controllerId.isBlank()) {
                        query.setParameter("controllerId", controllerId);
                    }
                    if (startFrom != null) {
                        query.setParameter("startTimeFrom", startFrom, TemporalType.TIMESTAMP);
                    }
                    if (startTo != null) {
                        query.setParameter("startTimeTo", startTo, TemporalType.TIMESTAMP);
                    }
                    AtomicInteger counter = new AtomicInteger();
                    for (List<String> chunk : chunkedOTags) {
                        query.setParameterList("tagNames" + counter.getAndIncrement(), chunk);
                    }
                    List<Long> result = connection.getResultList(query);
                    if (result == null) {
                        return Collections.emptyList();
                    }
                    return result;
                } catch (SOSHibernateInvalidSessionException ex) {
                    throw new DBConnectionRefusedException(ex);
                } catch (Exception ex) {
                    throw new DBInvalidDataException(ex);
                }
            }
        }
        return Collections.emptyList();
    }

    public static List<ResponseBaseSearchItem> getTagSearch(String controllerId, String search, SOSHibernateSession session)
            throws SOSHibernateException {
        //StringBuilder hql = new StringBuilder("select tagName as name, min(ordering) as ordering from ");
        StringBuilder hql = new StringBuilder("select tagName as name from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        List<String> whereClause = new ArrayList<>(2);
        if (SOSString.isEmpty(controllerId)) {
            controllerId = null;
        } else {
            whereClause.add("controllerId=:controllerId");
        }
        if (SOSString.isEmpty(search) || search.equals("*")) {
            search = null;
//            whereClause.add("tagName is not null");
        } else {
            whereClause.add("lower(tagName) like :search");
        }
        if (!whereClause.isEmpty()) {
            hql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
        }
        hql.append(" group by tagName");

        Query<ResponseBaseSearchItem> query = session.createQuery(hql.toString(), ResponseBaseSearchItem.class);
        if (search != null) {
            // (only) on the right hand side always %
            query.setParameter("search", SearchStringHelper.globToSqlPattern(search.toLowerCase() + "%").replaceAll("%%+", "%"));
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }
        List<ResponseBaseSearchItem> result = session.getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    private static Integer getNumOfTagsDisplayedAsOrderId() {
        ConfigurationGlobalsJoc jocSettings = Globals.getConfigurationGlobalsJoc();
        return jocSettings.getNumOfTagsDisplayedAsOrderId();
    }
    
    public static boolean withTagsDisplayedAsOrderId() {
        return getNumOfTagsDisplayedAsOrderId() != 0;
    }
    
    private static <T> Collection<List<T>> getChunkedCollection(Collection<T> coll) {
        if (coll != null) {
            AtomicInteger counter = new AtomicInteger();
            return coll.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / SOSHibernate.LIMIT_IN_CLAUSE)).values();
        }
        return null;
    }
    
    public static Workflow addGroupsToInstructions(Workflow workflow, SOSHibernateSession session) throws JsonProcessingException {
        Set<String> tags = new HashSet<>();
        List<Instruction> insts = workflow.getInstructions();
        getOrderTags(tags, insts, new InventoryOrderTagDBLayer(session));
        if (hasGroup(tags)) {
            workflow.setInstructions(insts);
        }
        return workflow;
    }

    public static void updateTagsFromInstructions(Workflow workflow, DBItemInventoryConfiguration item) throws JsonProcessingException {
        Set<String> tags = new HashSet<>();
        List<Instruction> insts = workflow.getInstructions();
        getOrderTags(tags, insts, (InventoryOrderTagDBLayer) null);
        try {
            Validator.testJavaNameRulesAtTags("", tags);
            update(tags.stream());
        } catch (Exception e) {
            //
        }
        if (hasGroup(tags)) {
            workflow.setInstructions(insts);
            item.setContent(JocInventory.toString(workflow));
        }
    }

    private static void getOrderTags(Set<String> tags, List<Instruction> insts, InventoryOrderTagDBLayer dbOrderTagLayer) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            if (b.getWorkflow() != null) {
                                getOrderTags(tags, b.getWorkflow().getInstructions(), dbOrderTagLayer);
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        getOrderTags(tags, fl.getWorkflow().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        getOrderTags(tags, ie.getThen().getInstructions(), dbOrderTagLayer);
                    }
                    if (ie.getElse() != null) {
                        getOrderTags(tags, ie.getElse().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case CASE_WHEN:
                    CaseWhen cw = inst.cast();
                    if (cw.getCases() != null) {
                        for (When when : cw.getCases()) {
                            if (when.getThen() != null) {
                                getOrderTags(tags, when.getThen().getInstructions(), dbOrderTagLayer);
                            }
                        }
                    }
                    if (cw.getElse() != null) {
                        getOrderTags(tags, cw.getElse().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        getOrderTags(tags, tc.getTry().getInstructions(), dbOrderTagLayer);
                    }
                    if (tc.getCatch() != null) {
                        getOrderTags(tags, tc.getCatch().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        getOrderTags(tags, l.getLockedWorkflow().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        getOrderTags(tags, c.getCycleWorkflow().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    if (cns.getSubworkflow() != null) {
                        getOrderTags(tags, cns.getSubworkflow().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        getOrderTags(tags, ss.getSubworkflow().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        getOrderTags(tags, opts.getBlock().getInstructions(), dbOrderTagLayer);
                    }
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    if (ao.getTags() != null) {
                        if (dbOrderTagLayer == null) {
                            tags.addAll(ao.getTags());
                            if (hasGroup(ao.getTags())) {
                                ao.setTags(deleteGroupFromTags(ao.getTags()));
                            }
                        } else {
                            try {
                                Map<String, String> gt = dbOrderTagLayer.getGroupedTags(ao.getTags(), true).stream().distinct().collect(Collectors
                                        .toMap(GroupedTag::getTag, GroupedTag::toString));
                                ao.setTags(ao.getTags().stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));
                                tags.addAll(ao.getTags());
                            } catch (Exception e) {
                                //
                            }
                        }
                    }
                default:
                    break;
                }
            }
        }
    }
    
    public static Workflow addGroupsToInstructions(Workflow workflow, Map<String, String> gt) throws JsonProcessingException {
        Set<String> tags = new HashSet<>();
        List<Instruction> insts = workflow.getInstructions();
        if (gt == null) {
            gt = Collections.emptyMap(); 
         }
        getOrderTags(tags, insts, gt);
        if (hasGroup(tags)) {
            workflow.setInstructions(insts);
        }
        return workflow;
    }
    
    private static void getOrderTags(Set<String> tags, List<Instruction> insts, Map<String, String> gt) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            if (b.getWorkflow() != null) {
                                getOrderTags(tags, b.getWorkflow().getInstructions(), gt);
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        getOrderTags(tags, fl.getWorkflow().getInstructions(), gt);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        getOrderTags(tags, ie.getThen().getInstructions(), gt);
                    }
                    if (ie.getElse() != null) {
                        getOrderTags(tags, ie.getElse().getInstructions(), gt);
                    }
                    break;
                case CASE_WHEN:
                    CaseWhen cw = inst.cast();
                    if (cw.getCases() != null) {
                        for (When when : cw.getCases()) {
                            if (when.getThen() != null) {
                                getOrderTags(tags, when.getThen().getInstructions(), gt);
                            }
                        }
                    }
                    if (cw.getElse() != null) {
                        getOrderTags(tags, cw.getElse().getInstructions(), gt);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        getOrderTags(tags, tc.getTry().getInstructions(), gt);
                    }
                    if (tc.getCatch() != null) {
                        getOrderTags(tags, tc.getCatch().getInstructions(), gt);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        getOrderTags(tags, l.getLockedWorkflow().getInstructions(), gt);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        getOrderTags(tags, c.getCycleWorkflow().getInstructions(), gt);
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    if (cns.getSubworkflow() != null) {
                        getOrderTags(tags, cns.getSubworkflow().getInstructions(), gt);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        getOrderTags(tags, ss.getSubworkflow().getInstructions(), gt);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        getOrderTags(tags, opts.getBlock().getInstructions(), gt);
                    }
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    if (ao.getTags() != null) {
                        try {
                            ao.setTags(ao.getTags().stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));
                            tags.addAll(ao.getTags());
                        } catch (Exception e) {
                            //
                        }
                    }
                default:
                    break;
                }
            }
        }
    }
    
    public static Schedule addGroupsToOrderPreparation(Schedule schedule, SOSHibernateSession session) {
        List<OrderParameterisation> orderParameterisations = schedule.getOrderParameterisations();
        if (orderParameterisations != null) {
            Set<String> tags = orderParameterisations.stream().map(OrderParameterisation::getTags).filter(Objects::nonNull).flatMap(Set::stream)
                    .collect(Collectors.toSet());
            Map<String, String> gt =  new InventoryOrderTagDBLayer(session).getGroupedTags(tags, true).stream().distinct().collect(Collectors
                    .toMap(GroupedTag::getTag, GroupedTag::toString));
            if (!gt.isEmpty()) {
                for (OrderParameterisation op : orderParameterisations) {
                    if (op.getTags() != null) {
                        op.setTags(op.getTags().stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));
                    }
                }
            }
        }
        return schedule;
    }
    
    public static Schedule addGroupsToOrderPreparation(Schedule schedule, Map<String, String> gt) {
        List<OrderParameterisation> orderParameterisations = schedule.getOrderParameterisations();
        if (orderParameterisations != null && !gt.isEmpty()) {
            for (OrderParameterisation op : orderParameterisations) {
                if (op.getTags() != null) {
                    op.setTags(op.getTags().stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));
                }
            }
        }
        return schedule;
    }

    public static void updateTagsFromOrderPreparation(Schedule schedule, DBItemInventoryConfiguration item) throws JsonProcessingException {
        boolean hasGroup = false;
        List<OrderParameterisation> orderParameterisations = schedule.getOrderParameterisations();
        if (orderParameterisations != null) {
            Set<String> tags = orderParameterisations.stream().map(OrderParameterisation::getTags).filter(Objects::nonNull).flatMap(Set::stream)
                    .collect(Collectors.toSet());
            try {
                Validator.testJavaNameRulesAtTags("", tags);
                update(tags.stream());
            } catch (Exception e) {
                //
            }
            for (OrderParameterisation op : orderParameterisations) {
                if (op.getTags() != null) {
                    if (hasGroup(op.getTags())) {
                        hasGroup = true;
                        op.setTags(deleteGroupFromTags(op.getTags()));
                    }
                }
            }
            if (hasGroup) {
                schedule.setOrderParameterisations(orderParameterisations);
                item.setContent(JocInventory.toString(schedule));
            }
        }
    }
    
    public static FileOrderSource addGroupsToFileOrderSource(FileOrderSource fos, SOSHibernateSession session) throws JsonProcessingException {
        Set<String> tags = fos.getTags();
        if (tags != null) {
            try {
                Map<String, String> gt =  new InventoryOrderTagDBLayer(session).getGroupedTags(tags, true).stream().distinct().collect(Collectors
                        .toMap(GroupedTag::getTag, GroupedTag::toString));
                if (!gt.isEmpty()) {
                    fos.setTags(tags.stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));  
                }
            } catch (Exception e) {
                //
            }
        }
        return fos;
    }
    
    public static FileOrderSource addGroupsToFileOrderSource(FileOrderSource fos, Map<String, String> gt) throws JsonProcessingException {
        Set<String> tags = fos.getTags();
        if (tags != null && !gt.isEmpty()) {
            fos.setTags(tags.stream().map(tag -> gt.getOrDefault(tag, tag)).collect(Collectors.toSet()));
        }
        return fos;
    }

    public static void updateTagsFromFileOrderSource(FileOrderSource fos, DBItemInventoryConfiguration item) throws JsonProcessingException {
        Set<String> tags = fos.getTags();
        if (tags != null) {
            try {
                Validator.testJavaNameRulesAtTags("", tags);
                update(tags.stream());
            } catch (Exception e) {
                //
            }
            if (hasGroup(tags)) {
                fos.setTags(deleteGroupFromTags(tags));
                item.setContent(JocInventory.toString(fos));
            }
        }
    }

    public static void update(Stream<String> tags) {
        SOSHibernateSession session = null;
        try {
            Map<String, GroupedTag> groupedTags = tags.map(GroupedTag::new).distinct().collect(Collectors.toMap(GroupedTag::getTag, Function
                    .identity()));
            if (!groupedTags.isEmpty()) {

                session = Globals.createSosHibernateStatelessConnection("updateOrderTags");
                session.setAutoCommit(false);
                Globals.beginTransaction(session);
                
                InventoryOrderTagDBLayer dbTagLayer = new InventoryOrderTagDBLayer(session);
                List<DBItemInventoryOrderTag> dbTags = groupedTags.isEmpty() ? Collections.emptyList() : dbTagLayer.getTags(groupedTags.keySet());

                ATagsModifyImpl.checkAndAssignGroupModerate(groupedTags, new InventoryTagDBLayer(session), "workflow");
                ATagsModifyImpl.checkAndAssignGroupModerate(groupedTags, new InventoryJobTagDBLayer(session), "job");
                // TODO same with historyOrderTags??

                Set<GroupedTag> alreadyExistingTagsInDB = dbTags.stream().map(DBItemInventoryOrderTag::getName).map(GroupedTag::new).collect(
                        Collectors.toSet());
                groupedTags.values().removeAll(alreadyExistingTagsInDB); // groupedTags contains only new tags

                if (!groupedTags.isEmpty()) {

                    Set<String> groups = groupedTags.values().stream().map(GroupedTag::getGroup).filter(Optional::isPresent).map(Optional::get)
                            .collect(Collectors.toSet());
                    List<DBItemInventoryTagGroup> dbGroups = groups.isEmpty() ? Collections.emptyList() : dbTagLayer.getGroups(groups);
                    Map<String, Long> dbGroupsMap = dbGroups.stream().collect(Collectors.toMap(DBItemInventoryTagGroup::getName,
                            DBItemInventoryTagGroup::getId));

                    groups.removeAll(dbGroupsMap.keySet()); // groups contains only new groups
                    Date date = Date.from(Instant.now());

                    if (!groups.isEmpty()) {
                        int maxGroupsOrdering = dbTagLayer.getMaxGroupsOrdering();
                        for (String group : groups) {
                            DBItemInventoryTagGroup item = new DBItemInventoryTagGroup();
                            item.setName(group);
                            item.setModified(date);
                            item.setOrdering(++maxGroupsOrdering);
                            dbTagLayer.getSession().save(item);
                            dbGroupsMap.put(group, item.getId());
                            // TODO events
                        }
                    }

                    int maxOrdering = dbTagLayer.getMaxOrdering();
                    for (GroupedTag groupedTag : groupedTags.values()) {
                        DBItemInventoryOrderTag item = new DBItemInventoryOrderTag();
                        item.setId(null);
                        item.setModified(date);
                        item.setName(groupedTag.getTag());
                        item.setOrdering(++maxOrdering);
                        if (groupedTag.getGroup().isPresent()) {
                            item.setGroupId(dbGroupsMap.getOrDefault(groupedTag.getGroup().get(), 0L));
                        } else {
                            item.setGroupId(0L);
                        }
                        dbTagLayer.getSession().save(item);
                    }
                }
                
                Globals.commit(session);
            }
        } catch (Exception e) {
            Globals.rollback(session);
            LOGGER.warn("", e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private static Set<String> deleteGroupFromTags(Set<String> tagsWithGroups) {
        return tagsWithGroups.stream().map(GroupedTag::new).map(GroupedTag::getTag).collect(Collectors.toSet());
    }
    
    private static boolean hasGroup(Set<String> tagsWithGroups) {
        return tagsWithGroups.stream().anyMatch(s -> s.contains(":"));
    }

}
