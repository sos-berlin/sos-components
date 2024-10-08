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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.inventory.JocInventory;
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
            LOGGER.trace("HistoryOrderStarted received: " + SOSString.toString(evt));
            addHistoryIdToTags(evt.getControllerId(), evt.getOrderId(), (HistoryOrderBean) evt.getPayload());
        }
    }

    @Subscribe({ AddOrderEvent.class })
    public void addTagsToOrderbyFileOrderSourceOrAddOrderInstruction(AddOrderEvent evt) {
        boolean isChildOrder = evt.getOrderId().contains("|");
        if (!isChildOrder) {
            LOGGER.trace("AddOrderEvent received: " + SOSString.toString(evt));
            addTagsToOrderbyFileOrderSourceOrAddOrderInstruction(evt.getControllerId(), evt.getOrderId(), evt.getWorkflowName());
        }
    }
    
    @Subscribe({ TerminateOrderEvent.class })
    public void deleteTagsFromAddOrderInstruction(TerminateOrderEvent evt) {
        boolean isChildOrder = evt.getOrderId().contains("|");
        if (!isChildOrder) {
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
                String orderIdPattern = orderId.substring(OrdersHelper.mainOrderIdLength, OrdersHelper.mainOrderIdLength + 19);
                connection = Globals.createSosHibernateStatelessConnection("deleteAddOrderTags");
                DBItemInventoryAddOrderTag dbItem = connection.get(DBItemInventoryAddOrderTag.class, Long.valueOf(orderIdPattern));
                if (dbItem != null) {
                    connection.delete(dbItem);
                }
            } catch (Exception e) {
                LOGGER.error("[deleteAddOrderTags][" + orderId + "]: " + e.toString(), e);
            } finally {
                Globals.disconnect(connection);
            }
        }
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

    private synchronized void addTagsToOrderbyAddOrderInstruction(String controllerId, String orderId) {
        SOSHibernateSession connection = null;
        try {
            String orderIdPattern = orderId.substring(OrdersHelper.mainOrderIdLength, OrdersHelper.mainOrderIdLength + 19);
            String addOrderIndex = orderId.substring(OrdersHelper.mainOrderIdLength - 3, OrdersHelper.mainOrderIdLength - 1);
            connection = Globals.createSosHibernateStatelessConnection("storeAddOrderTags");
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
        } catch (Exception e) {
            Globals.rollback(connection);
            LOGGER.error("[storeAddOrderTags][" + orderId + "]: " + e.toString(), e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    public static Either<Exception, Void> addAdhocOrderTags(String controllerId, Map<OrderV, Set<String>> oTags) {
        if (controllerId != null && oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                // delete/insert
                connection = Globals.createSosHibernateStatelessConnection("storeOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                deleteTags(controllerId, oTags.keySet().stream().map(OrderV::getOrderId).distinct().collect(Collectors.toList()), connection);
                for (Map.Entry<OrderV, Set<String>> oTag : oTags.entrySet()) {
                    addTagsOfOrder(controllerId, oTag.getKey().getOrderId(), oTag.getValue(), connection, Date.from(Instant.ofEpochMilli(oTag.getKey()
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
    
//    private void addTagsOfOrder(OrderTagsEvent evt, SOSHibernateSession connection, Date now) {
//        //orderTags.putIfAbsent(evt.getControllerId(), new ConcurrentHashMap<>());
//        for (Map.Entry<String, Object> entry : evt.getTags().entrySet()) {
//            //if (!orderTags.get(evt.getControllerId()).containsKey(entry.getKey())) {
//                @SuppressWarnings("unchecked")
//                Set<String> ts = (Set<String>) entry.getValue();
//                //orderTags.get(evt.getControllerId()).put(entry.getKey(), ts);
//                int i = 0;
//                for (String tag : ts) {
//                    try {
//                        connection.save(new DBItemHistoryOrderTag(evt.getControllerId(), entry.getKey(), tag, ++i, now));
//                    } catch (SOSHibernateInvalidSessionException ex) {
//                        throw new DBConnectionRefusedException(ex);
//                    } catch (Exception ex) {
//                        throw new DBInvalidDataException(ex);
//                    }
//                }
//            //}
//        }
//    }
    
    private static void addTagsOfOrder(String controllerId, String orderId, Set<String> tags, SOSHibernateSession connection, Date scheduledFor) {
        int i = 0;
        for (String tag : tags) {
            try {
                connection.save(new DBItemHistoryOrderTag(controllerId, orderId, tag, ++i, scheduledFor));
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
                        .getOrdering(), scheduledFor);
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

        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        List<String> clauses = new ArrayList<>(2);
        if (!controllerId.isBlank()) {
            clauses.add("controllerId=:controllerId");
        }
        String clause = IntStream.range(0, chunkedOrderIds.size()).mapToObj(i -> "orderId in (:orderIds" + i + ")").collect(Collectors.joining(
                " or "));
        if (chunkedOrderIds.size() > 1) {
            clause = "(" + clause + ")";
        }
        clauses.add(clause);
        hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", ""))).append(" order by ordering");

        Query<DBItemHistoryOrderTag> query = connection.createQuery(hql);
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
        return result.stream().collect(Collectors.groupingBy(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getTagName,
                Collectors.toCollection(LinkedHashSet::new))));
    }
    
    public static Map<String, Set<String>> getTagsByHistoryIds(String controllerId, List<Long> historyIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (historyIds == null || historyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Collection<List<Long>> chunkedHistoryIds = getChunkedCollection(historyIds);
        return chunkedHistoryIds.stream().map(chunk -> getTagsByChunkOfHistoryIds(controllerId, chunk, connection)).flatMap(List::stream).collect(
                Collectors.groupingBy(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getTagName, Collectors.toSet())));
    }
    
    private static List<DBItemHistoryOrderTag> getTagsByChunkOfHistoryIds(String controllerId, List<Long> historyIds,
            SOSHibernateSession connection) {
        if (historyIds == null || historyIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
            List<String> clauses = new ArrayList<>(3);
            if (controllerId != null && !controllerId.isBlank()) {
                clauses.add("controllerId=:controllerId");
            }
            clauses.add("historyId != 0");
            clauses.add("historyId in (:historyIds)");
            hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));

            Query<DBItemHistoryOrderTag> query = connection.createQuery(hql.toString());
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
        StringBuilder hql = new StringBuilder("select tagName from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        hql.append(" where controllerId=:controllerId");
        hql.append(" and orderId=:orderId").append(" order by ordering");

        Query<String> query = connection.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", orderId);
        List<String> result = connection.getResultList(query);
        if (result == null) {
            return Collections.emptySet();
        }
        return result.stream().collect(Collectors.toCollection(LinkedHashSet::new));
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

}
