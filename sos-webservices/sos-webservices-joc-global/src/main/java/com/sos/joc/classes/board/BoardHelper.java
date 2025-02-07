package com.sos.joc.classes.board;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.board.NoticeState;
import com.sos.controller.model.board.NoticeStateText;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.plan.PlanSchemaId;

import js7.data.board.BoardPath;
import js7.data.board.BoardState;
import js7.data.board.NoticeId;
import js7.data.board.NoticeKey;
import js7.data.board.PlannedNoticeKey;
import js7.data.order.Order;
import js7.data.plan.PlanId;
import js7.data.plan.PlanKey;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.board.JNotice;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);
    private static final String NoticeIdSeparator = "╱";
    private static final String EmptyNoticeKey = "—";

    public static Board getCompactBoard(JControllerState controllerState, DeployedContent dc, ConcurrentMap<NoticeId, Integer> numOfExpectings)
            throws Exception {

        final JBoardState jBoardState = getJBoardState(controllerState, dc.getName());

        Board item = init(dc);
        item.setState(SyncStateHelper.getState(getSyncStateText(controllerState, jBoardState)));
        item.setNumOfExpectingOrders(numOfExpectings.values().stream().mapToInt(Integer::intValue).sum());
        int numOfNotices = numOfExpectings.keySet().size();
        if (jBoardState != null) {
            numOfNotices += StreamSupport.stream(JavaConverters.asJava(jBoardState.asScala().notices()).spliterator(), false).mapToInt(e -> 1).sum();
        }
        item.setNumOfNotices(numOfNotices);
        item.setNotices(null);

        return item;
    }
    
    public static Board getBoard(JControllerState controllerState, DeployedContent dc, ConcurrentMap<NoticeId, List<JOrder>> expectings,
            Map<String, Set<String>> orderTags, Integer limit, ZoneId zoneId, long surveyDateMillis, SOSHibernateSession session) throws Exception {

        final JBoardState jBoardState = getJBoardState(controllerState, dc.getName());

        Board item = init(dc);
        item.setState(SyncStateHelper.getState(getSyncStateText(controllerState, jBoardState)));
        item.setNumOfExpectingOrders(expectings.values().stream().mapToInt(List::size).sum());

        ToLongFunction<JOrder> compareScheduleFor = OrdersHelper.getCompareScheduledFor(zoneId, surveyDateMillis);
        List<Notice> notices = new ArrayList<>();
        boolean withWorkflowTagsDisplayed = WorkflowsHelper.withWorkflowTagsDisplayed();
        
        Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
            try {
                return OrdersHelper.mapJOrderToOrderV(o, controllerState, true, orderTags, null, null, zoneId);
            } catch (Exception e) {
                return null;
            }
        };
        
        if (jBoardState != null) {
            final BoardState bs = jBoardState.asScala();
            
            //LOGGER.info(JavaConverters.asJava(bs.toNoticePlace()).toString());
            JavaConverters.asJava(bs.toNoticePlace()).forEach((pnk, nplace) -> {
                JNoticePlace np = JNoticePlace.apply(nplace);
                boolean isNotExpected = np.expectingOrderIds().isEmpty();
                if (isNotExpected) {
                    Notice notice = new Notice();
                    //notice.setKey(pnk.noticeKey().string());
                    notice.setId(getNoticeKeyShortString(pnk));
                    np.notice().flatMap(JNotice::endOfLife).map(Date::from).ifPresent(d -> notice.setEndOfLife(d));
                    if (np.isAnnounced()) {
                        notice.setState(getState(NoticeStateText.ANNOUNCED));
                    } else {
                        notice.setState(getState(NoticeStateText.POSTED));
                    }
                    notice.setExpectingOrders(null);
                    notices.add(notice);
                } else {
                    NoticeId noticeId = NoticeId.of(pnk.planId(), jBoardState.path(), pnk.noticeKey());
                    Notice notice = getExpectingOrder(noticeId, expectings.get(noticeId), limit, compareScheduleFor, mapJOrderToOrderV,
                            withWorkflowTagsDisplayed, session);
                    if (np.isAnnounced()) {
                        notice.setState(getState(NoticeStateText.ANNOUNCED));
                    }
                    notices.add(notice);
                }
            });
        }
        
//        expectings.forEach((noticeId, jOrders) -> {
//            Notice notice = getExpectingOrder(noticeId, jOrders, limit, compareScheduleFor, mapJOrderToOrderV, withWorkflowTagsDisplayed, session);
//            notices.add(notice);
//        });

        item.setNotices(notices);
        item.setNumOfNotices(notices.size());
        return item;
    }
    
    private static Notice getExpectingOrder(NoticeId noticeId, List<JOrder> jOrders, Integer limit, ToLongFunction<JOrder> compareScheduleFor,
            Function<JOrder, OrderV> mapJOrderToOrderV, boolean withWorkflowTagsDisplayed, SOSHibernateSession session) {
        Notice notice = new Notice();
        notice.setId(BoardHelper.getNoticeKeyShortString(noticeId));
        //notice.setKey(noticeId.noticeKey().string());
        notice.setEndOfLife(null);
        if (limit > -1) {
            notice.setExpectingOrders(jOrders.stream().sorted(Comparator.comparingLong(compareScheduleFor).reversed()).limit(limit.longValue())
                    .map(mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toList()));
        } else {
            notice.setExpectingOrders(jOrders.stream().map(mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        notice.setState(getState(NoticeStateText.EXPECTED));
        if (withWorkflowTagsDisplayed) {
            notice.setWorkflowTagsPerWorkflow(WorkflowsHelper.getTagsPerWorkflow(session, jOrders.stream().map(JOrder::workflowId).map(
                    JWorkflowId::path).map(WorkflowPath::string)));
        }
        return notice;
    }
    
    private static JBoardState getJBoardState(JControllerState controllerState, String boardName) {
        if (controllerState != null) {
            return controllerState.pathToBoardState().get(BoardPath.of(boardName));
        }
        return null;
    }
    
    private static SyncStateText getSyncStateText(JControllerState controllerState, JBoardState jBoardState) {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            if (jBoardState != null) {
                stateText = SyncStateText.IN_SYNC;
            }
        }
        return stateText;
    }
    
    public static ConcurrentMap<String, ConcurrentMap<NoticeId, List<JOrder>>> getExpectingOrders(JControllerState controllerState,
            Set<String> boardPaths, Set<Folder> permittedFolders) {
        return getExpectingOrders(getExpectingOrdersStream(controllerState, boardPaths, permittedFolders));
    }
    
    public static ConcurrentMap<String, ConcurrentMap<NoticeId, List<JOrder>>> getExpectingOrders(Stream<ExpectingOrder> expectingOrders) {
        return expectingOrders.collect(Collectors.groupingByConcurrent(
                ExpectingOrder::getBoardPath, Collectors.groupingByConcurrent(ExpectingOrder::getNoticeId, Collectors.mapping(
                        ExpectingOrder::getJOrder, Collectors.toList()))));
    }

    public static ConcurrentMap<String, ConcurrentMap<NoticeId, Integer>> getNumOfExpectingOrders(JControllerState controllerState,
            Set<String> boardPaths, Set<Folder> permittedFolders) {
        return getNumOfExpectingOrders(getExpectingOrdersStream(controllerState, boardPaths, permittedFolders));
    }
    
    public static ConcurrentMap<String, ConcurrentMap<NoticeId, Integer>> getNumOfExpectingOrders(Stream<ExpectingOrder> expectingOrders) {
        return expectingOrders.collect(Collectors.groupingByConcurrent(ExpectingOrder::getBoardPath, Collectors.groupingByConcurrent(
                ExpectingOrder::getNoticeId, Collectors.reducing(0, e -> 1, Integer::sum))));
    }
    
    public static JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }
    
    public static Stream<ExpectingOrder> getExpectingOrdersStream(JControllerState controllerState, Set<String> boardPaths,
            Set<Folder> permittedFolders) {
        if (controllerState == null || boardPaths == null || boardPaths.isEmpty()) {
            return Stream.empty();
        }
        Function<JOrder, Stream<ExpectingOrder>> mapper = order -> controllerState.orderToStillExpectedNotices(order.id()).stream().filter(
                e -> boardPaths.contains(e.boardPath().string())).map(e -> new ExpectingOrder(order, e));
        
        if (permittedFolders == null || permittedFolders.isEmpty()) {
            return controllerState.ordersBy(JOrderPredicates.byOrderState(Order.ExpectingNotices.class)).parallel().flatMap(mapper).filter(
                    Objects::nonNull);
        }
        ConcurrentMap<JWorkflowId, List<JOrder>> ordersPerWorkflow = controllerState.ordersBy(JOrderPredicates.byOrderState(
                Order.ExpectingNotices.class)).parallel().collect(Collectors.groupingByConcurrent(JOrder::workflowId));
        return ordersPerWorkflow.entrySet().parallelStream().filter(e -> JOCResourceImpl.canAdd(WorkflowPaths.getPath(e.getKey().path().string()),
                permittedFolders)).map(Map.Entry::getValue).flatMap(List::stream).flatMap(mapper).filter(Objects::nonNull);
    }
    
    public static Board init(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {
        Board item = Globals.objectMapper.readValue(dc.getContent(), Board.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        return item;
    }
    
    public static NoticeState getState(NoticeStateText state) {
        NoticeState nState = new NoticeState();
        nState.set_text(state);
        nState.setSeverity(severities.get(state));
        return nState;
    }
    
    public static NoticeState getState(NoticeStateText state, boolean isAnnounced) {
        NoticeState nState = new NoticeState();
        if (isAnnounced) {
            state = NoticeStateText.ANNOUNCED;
        }
        nState.set_text(state);
        nState.setSeverity(severities.get(state));
        return nState;
    }
    
    private static final Map<NoticeStateText, Integer> severities = Collections.unmodifiableMap(new HashMap<NoticeStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(NoticeStateText.POSTED, 6);
            put(NoticeStateText.EXPECTED, 8);
            put(NoticeStateText.ANNOUNCED, 4);
        }
    });
    
    public static NoticeId getNoticeId(String noticeIdStr, String boardPath) {
        
        noticeIdStr = noticeIdStr.replace(NoticeIdSeparator, "/").replace(EmptyNoticeKey, "-").replaceAll("//+", "/");
        BoardPath bPath = BoardPath.of(JocInventory.pathToName(boardPath));
        
        if (!noticeIdStr.contains("/")) { // old noticeId for global boards
            return NoticeId.of(PlanId.Global(), bPath, NoticeKey.of(noticeIdStr));
        } else {
            String[] noticeIdParts = noticeIdStr.split("/");
            PlanSchemaId pSchemaId = PlanSchemaId.fromValue(noticeIdParts[0]);
            PlanId planId = PlanId.Global();
            NoticeKey noticeKey = NoticeKey.empty();
            if (pSchemaId.equals(PlanSchemaId.Global)) {
                // expect noticeIdParts.lenght = 2
                // nothing to do -> planId = PlanId.Global()
                if (noticeIdParts.length > 1 && !noticeIdParts[1].isEmpty() && !noticeIdParts[1].equals("-")) {
                    noticeKey = NoticeKey.of(noticeIdParts[1]);
                }
            } else {
                // expect noticeIdParts.lenght == 3 (or 2 if NoticeKey is empty)
                planId = PlanId.apply(js7.data.plan.PlanSchemaId.of(pSchemaId.value()), PlanKey.of(noticeIdParts[1]));
                if (noticeIdParts.length > 2 && !noticeIdParts[2].isEmpty() && !noticeIdParts[2].equals("-")) {
                    noticeKey = NoticeKey.of(noticeIdParts[2]);
                }
            }
            return NoticeId.of(planId, bPath, noticeKey);
        }
    }
    
    public static String getNoticeKeyShortString(NoticeId nId) {
        return getNoticeKeyShortString(nId.plannedNoticeKey());
    }
    
    public static String getNoticeKeyShortString(PlannedNoticeKey pnk) {
        return pnk.toShortString().replace(NoticeIdSeparator, "/").replace(EmptyNoticeKey, "-").replaceFirst("^Global/", "");
    }

//    private static <T> Collector<T, ?, List<T>> limitingList(int limit) {
//        return Collector.of(ArrayList::new, (l, e) -> {
//            if (l.size() < limit)
//                l.add(e);
//        }, (l1, l2) -> {
//            l1.addAll(l2.subList(0, Math.min(l2.size(), Math.max(0, limit - l1.size()))));
//            return l1;
//        });
//    }

}
