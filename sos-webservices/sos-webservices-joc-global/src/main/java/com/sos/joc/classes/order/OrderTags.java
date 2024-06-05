package com.sos.joc.classes.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.history.DBItemHistoryOrderTag;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

import io.vavr.control.Either;
import js7.data.order.OrderId;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;

public class OrderTags {

//    private static OrderTags instance;
//    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTags.class);

//    private OrderTags() {
////        EventBus.getInstance().register(this);
//    }
//
//    public static OrderTags getInstance() {
//        if (instance == null) {
//            instance = new OrderTags();
//        }
//        return instance;
//    }

//    @Subscribe({ OrderTagsEvent.class })
//    public void addTag(OrderTagsEvent evt) {
//        //orderTags.putIfAbsent(evt.getControllerId(), new ConcurrentHashMap<>());
//        SOSHibernateSession connection = null;
//        try {
//            Date now = Date.from(Instant.now());
//            connection = Globals.createSosHibernateStatelessConnection("storeOrderTags");
//            // TODO check if tags of order already exists
//            addTagsOfOrder(evt, connection, now);
//        } finally {
//            Globals.disconnect(connection);
//        }
//    }
    
    public static Either<Exception, Void> addTags(String controllerId, Map<String, Set<String>> oTags) {
        if (controllerId != null && oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                //orderTags.putIfAbsent(controllerId, new ConcurrentHashMap<>());
                Date now = Date.from(Instant.now());
                // delete/insert
                connection = Globals.createSosHibernateStatelessConnection("storeOrderTags");
                connection.setAutoCommit(false);
                Globals.beginTransaction(connection);
                deleteTags(controllerId, oTags.keySet().stream().collect(Collectors.toList()), connection);
                for (Map.Entry<String, Set<String>> oTag : oTags.entrySet()) {
                    addTagsOfOrder(controllerId, oTag.getKey(), oTag.getValue(), connection, now);
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
    
    private static void addTagsOfOrder(String controllerId, String orderId, Set<String> tags, SOSHibernateSession connection, Date now) {
        //if (!orderTags.get(controllerId).containsKey(orderId)) {
            //orderTags.get(controllerId).put(orderId, tags);
            int i = 0;
            for (String tag : tags) {
                try {
                    connection.save(new DBItemHistoryOrderTag(controllerId, orderId, tag, ++i, now));
                } catch (SOSHibernateInvalidSessionException ex) {
                    throw new DBConnectionRefusedException(ex);
                } catch (Exception ex) {
                    throw new DBInvalidDataException(ex);
                }
            }
        //}
    }
    
    public static void deleteTags(String controllerId, List<String> orderIds, SOSHibernateSession connection) throws SOSHibernateException {
        if (controllerId != null && orderIds != null && !orderIds.isEmpty()) {

            int size = orderIds.size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                for (int i = 0; i < orderIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    deleteTags(controllerId, SOSHibernate.getInClausePartition(i, orderIds), connection);
                }
            } else {

                StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
                hql.append(" where orderId in (:orderIds)");
                hql.append(" and controllerId=:controllerId");

                Query<DBItemDailyPlanVariable> query = connection.createQuery(hql);
                query.setParameterList("orderIds", orderIds.stream().map(OrdersHelper::getOrderIdMainPart).collect(Collectors.toSet()));
                query.setParameter("controllerId", controllerId);
                connection.executeUpdate(query);

                // if (!SOSString.isEmpty(controllerId) && orderTags.containsKey(controllerId)) {
                // orderIds.forEach(o -> orderTags.get(controllerId).remove(o));
                // }
            }
        }
    }
    
    public static void deleteTagsOfOrder(String controllerId, String orderId, SOSHibernateSession connection) {
        if (controllerId != null && orderId != null) {
            orderId = OrdersHelper.getOrderIdMainPart(orderId);
//            orderTags.putIfAbsent(controllerId, new ConcurrentHashMap<>());
//            orderTags.get(controllerId).remove(orderId);
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
                    updateTagsOfOrder(controllerId, oldNewOrderId.getKey().string(), oldNewOrderId.getValue().id().string(), connection);
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
    
    public static int updateTagsOfOrder(String controllerId, String oldOrderId, String newOrderId, SOSHibernateSession connection)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        hql.append(" set orderId=:newOrderId");
        hql.append(" where controllerId=:controllerId");
        hql.append(" and orderId=:oldOrderId");

        Query<Integer> query = connection.createQuery(hql);
        query.setParameter("newOrderId", OrdersHelper.getOrderIdMainPart(newOrderId));
        query.setParameter("oldOrderId", OrdersHelper.getOrderIdMainPart(oldOrderId));
        query.setParameter("controllerId", controllerId);
        return connection.executeUpdate(query);
    }
    
    public static void copyTagsOfOrder(String controllerId, String oldOrderId, String newOrderId, SOSHibernateSession connection)
            throws SOSHibernateException {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
            hql.append(" where controllerId=:controllerId");
            hql.append(" and orderId=:oldOrderId");

            Query<DBItemHistoryOrderTag> query = connection.createQuery(hql);
            query.setParameter("oldOrderId", OrdersHelper.getOrderIdMainPart(oldOrderId));
            query.setParameter("controllerId", controllerId);
            List<DBItemHistoryOrderTag> result = query.getResultList();
            if (result != null) {
                Date now = Date.from(Instant.now());
                for (DBItemHistoryOrderTag oldItem : result) {
                    DBItemHistoryOrderTag newItem = new DBItemHistoryOrderTag(oldItem.getControllerId(), newOrderId, oldItem.getTagName(), oldItem
                            .getOrdering(), now);
                    connection.save(newItem);
                }
            }
    }
    
    public static Stream<JOrder> filter(List<JOrder> jOrders, Map<String, Set<String>> orderTags, Set<String> tags) throws SOSHibernateException {
        if (orderTags == null || orderTags.isEmpty()) {
            return Stream.empty();
        }
        if (jOrders == null || jOrders.isEmpty()) {
            return Stream.empty();
        }
        return filter(jOrders.stream(), orderTags, tags);
    }
    
    public static Stream<JOrder> filter(Stream<JOrder> jOrders, Map<String, Set<String>> orderTags, Set<String> tags) throws SOSHibernateException {
        if (orderTags == null || orderTags.isEmpty()) {
            return Stream.empty();
        }
        return jOrders.filter(o -> orderTags.containsKey(OrdersHelper.getOrderIdMainPart(o.id().string()))).filter(o -> new HashSet<>(orderTags.get(
                OrdersHelper.getOrderIdMainPart(o.id().string()))).removeAll(tags));
    }
    
    public static Map<String, Set<String>> getTags(String controllerId, List<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (jOrders == null || jOrders.isEmpty()) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        return getTags(controllerId, jOrders.stream(), connection);
    }
    
    public static Map<String, Set<String>> getTags(String controllerId, Stream<JOrder> jOrders, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (jOrders == null) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        return getTagsByOrderIds(controllerId, jOrders.map(JOrder::id).map(OrderId::string), connection);
    }
    
    public static Map<String, Set<String>> getTagsByOrderIds(String controllerId, Stream<String> orderIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderIds == null) {
            return Collections.emptyMap();
        }
        if (controllerId == null) {
            return Collections.emptyMap();
        }
        return getTagsByOrderIds(controllerId, orderIds.map(OrdersHelper::getOrderIdMainPart).distinct().collect(Collectors.toList()), connection);
    }
    
    private static Map<String, Set<String>> getTagsByOrderIds(String controllerId, List<String> orderIds, SOSHibernateSession connection)
            throws SOSHibernateException {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        int size = orderIds.size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<String, Set<String>> r = new HashMap<>();
            for (int i = 0; i < orderIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.putAll(getTagsByOrderIds(controllerId, SOSHibernate.getInClausePartition(i, orderIds), connection));
            }
            return r;
        } else {
            if (controllerId == null) {
                return Collections.emptyMap();
            }
            connection = Globals.createSosHibernateStatelessConnection(OrderTags.class.getSimpleName());
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
            hql.append(" where controllerId=:controllerId");
            hql.append(" and orderId in (:orderIds)").append(" order by ordering");

            Query<DBItemHistoryOrderTag> query = connection.createQuery(hql);
            query.setParameter("controllerId", controllerId);
            query.setParameterList("orderIds", orderIds);
            List<DBItemHistoryOrderTag> result = connection.getResultList(query);
            if (result == null) {
                return Collections.emptyMap();
            }
            return result.stream().collect(Collectors.groupingBy(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getTagName,
                    Collectors.toSet())));
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
        query.setParameter("orderId", OrdersHelper.getOrderIdMainPart(orderId));
        List<String> result = connection.getResultList(query);
        if (result == null) {
            return Collections.emptySet();
        }
        return result.stream().collect(Collectors.toSet());
    }

    public static List<String> getMainOrderIdsByTags(String controllerId, Set<String> oTags) throws SOSHibernateException {
        if (oTags != null && !oTags.isEmpty()) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("getOrderTags");
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
            } finally {
                Globals.disconnect(connection);
            }
        }
        return Collections.emptyList();
    }

    public static List<ResponseBaseSearchItem> getTagSearch(String controllerId, String search, SOSHibernateSession session)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select tagName as name, min(ordering) as ordering from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_TAGS);
        List<String> whereClause = new ArrayList<>();
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
    
//    public void updateMap(String controllerId, String orderId, Set<String> tags) {
//        orderTags.putIfAbsent(controllerId, new ConcurrentHashMap<>());
//        orderTags.get(controllerId).put(orderId, tags);
//    }

//    public static void init() {
//        SOSHibernateSession connection = null;
//        try {
//            connection = Globals.createSosHibernateStatelessConnection(OrderTags.class.getSimpleName());
//            OrderTags.getInstance()._init(connection);
//        } finally {
//            Globals.disconnect(connection);
//        }
//    }
    
//    private boolean orderHasTags(String controllerId, String orderId) {
//        orderTags.putIfAbsent(controllerId, new ConcurrentHashMap<>());
//        return orderTags.get(controllerId).containsKey(orderId);
//    }

//    private void _init(SOSHibernateSession connection) {
//        LOGGER.info("... init order tags");
//        try {
//            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_TAGS).append(" order by ordering");
//            Query<DBItemHistoryOrderTag> query = connection.createQuery(hql.toString());
//            List<DBItemHistoryOrderTag> result = connection.getResultList(query);
//            if (result != null) {
//                orderTags = result.stream().collect(Collectors.groupingByConcurrent(DBItemHistoryOrderTag::getControllerId, Collectors
//                        .groupingByConcurrent(DBItemHistoryOrderTag::getOrderId, Collectors.mapping(DBItemHistoryOrderTag::getTagName, Collectors
//                                .toCollection(LinkedHashSet::new)))));
//            }
//        } catch (SOSHibernateException e) {
//            LOGGER.warn(e.toString());
//        }
//    }

}
