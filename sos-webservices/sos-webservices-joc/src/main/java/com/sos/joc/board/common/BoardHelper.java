package com.sos.joc.board.common;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.board.NoticeState;
import com.sos.controller.model.board.NoticeStateText;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.common.Folder;

import js7.data.board.BoardPath;
import js7.data.board.BoardState;
import js7.data.order.Order;
import js7.data.workflow.instructions.ExpectNotice;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    public static Board getCompactBoard(JControllerState controllerState, DeployedContent dc, ConcurrentMap<String, Integer> numOfExpectings)
            throws Exception {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        Board item = init(dc);

        JBoardState jBoardState = null;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            jBoardState = controllerState.pathToBoardState().get(BoardPath.of(dc.getName()));
            if (jBoardState != null) {
                stateText = SyncStateText.IN_SYNC;
            }
        }

        item.setState(SyncStateHelper.getState(stateText));
        item.setNumOfExpectingOrders(numOfExpectings.values().stream().mapToInt(Integer::intValue).sum());
        int numOfNotices = numOfExpectings.keySet().size();
        if (jBoardState != null) {
            numOfNotices += StreamSupport.stream(JavaConverters.asJava(jBoardState.asScala().notices()).spliterator(), false).mapToInt(e -> 1).sum();
        }
        item.setNumOfNotices(numOfNotices);
        item.setNotices(null);

        return item;
    }
    
    public static Board getBoard(JControllerState controllerState, DeployedContent dc, ConcurrentMap<String, List<JOrder>> expectings, Integer limit,
            long surveyDateMillis) throws Exception {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        Board item = init(dc);

        JBoardState jBoardState = null;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            jBoardState = controllerState.pathToBoardState().get(BoardPath.of(dc.getName()));
            if (jBoardState != null) {
                stateText = SyncStateText.IN_SYNC;
            }
        }

        item.setState(SyncStateHelper.getState(stateText));
        item.setNumOfExpectingOrders(expectings.values().stream().mapToInt(List::size).sum());

        ToLongFunction<JOrder> compareScheduleFor = OrdersHelper.getCompareScheduledFor(surveyDateMillis);
        List<Notice> notices = new ArrayList<>();

        expectings.forEach((noticeId, jOrders) -> {
            Notice notice = new Notice();
            notice.setId(noticeId);
            notice.setEndOfLife(null);
            if (limit > -1) {
                notice.setExpectingOrders(jOrders.stream().sorted(Comparator.comparingLong(compareScheduleFor).reversed()).limit(limit.longValue())
                        .map(o -> {
                            try {
                                return OrdersHelper.mapJOrderToOrderV(o, true, null, null);
                            } catch (Exception e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.toList()));
            } else {
                notice.setExpectingOrders(jOrders.stream().map(o -> {
                    try {
                        return OrdersHelper.mapJOrderToOrderV(o, true, null, null);
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            notice.setState(getState(NoticeStateText.EXPECTED));
            notices.add(notice);
        });

        if (jBoardState != null) {
            final BoardState bs = jBoardState.asScala();

            JavaConverters.asJava(bs.notices()).forEach(n -> {
                Notice notice = new Notice();
                notice.setId(n.id().string());
                if (n.endOfLife() != null) {
                    notice.setEndOfLife(Date.from(Instant.ofEpochMilli(n.endOfLife().toEpochMilli())));
                }
                notice.setState(getState(NoticeStateText.POSTED));
                notice.setExpectingOrders(null);
                // Set<OrderId> orderIds = JavaConverters.asJava(bs.expectingOrders(n.id()));
                // notice.setExpectingOrders(controllerState.ordersBy(o -> orderIds.contains(o.id())).parallel().map(o -> {
                // try {
                // // TODO remove final Parameters
                // return OrdersHelper.mapJOrderToOrderV(o, true, permittedFolders, null, null, null);
                // } catch (Exception e) {
                // return null;
                // }
                // }).filter(Objects::nonNull).collect(Collectors.toList()));
                notices.add(notice);
            });
        }

        item.setNotices(notices);
        item.setNumOfNotices(notices.size());
        return item;
    }
    
    public static ConcurrentMap<String, ConcurrentMap<String, List<JOrder>>> getExpectingOrders(JControllerState controllerState,
            Collection<String> boardPaths, Set<Folder> permittedFolders) {
        return getExpectingOrdersStream(controllerState, boardPaths, permittedFolders).collect(Collectors.groupingByConcurrent(
                ExpectingOrder::getBoardPath, Collectors.groupingByConcurrent(ExpectingOrder::getNoticeId, Collectors.mapping(
                        ExpectingOrder::getJOrder, Collectors.toList()))));
    }

    public static ConcurrentMap<String, ConcurrentMap<String, Integer>> getNumOfExpectingOrders(JControllerState controllerState,
            Collection<String> boardPaths, Set<Folder> permittedFolders) {
        return getExpectingOrdersStream(controllerState, boardPaths, permittedFolders).collect(Collectors.groupingByConcurrent(
                ExpectingOrder::getBoardPath, Collectors.groupingByConcurrent(ExpectingOrder::getNoticeId, Collectors.reducing(0, e -> 1,
                        Integer::sum))));
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
    
    private static Stream<ExpectingOrder> getExpectingOrdersStream(JControllerState controllerState, Collection<String> boardPaths,
            Set<Folder> permittedFolders) {
        if (controllerState == null || boardPaths == null || boardPaths.isEmpty()) {
            return Stream.empty();
        }
        Function<JOrder, ExpectingOrder> mapper = order -> {
            BoardPath boardPath = ((ExpectNotice) controllerState.asScala().instruction(order.asScala().workflowPosition())).boardPath();
            if (boardPaths.contains(boardPath.string())) {
                return new ExpectingOrder(order, boardPath);
            }
            return null;
        };
        if (permittedFolders == null || permittedFolders.isEmpty()) {
            return controllerState.ordersBy(JOrderPredicates.byOrderState(Order.ExpectingNotice.class)).parallel().map(mapper).filter(
                    Objects::nonNull);
        }
        ConcurrentMap<JWorkflowId, List<JOrder>> ordersPerWorkflow = controllerState.ordersBy(JOrderPredicates.byOrderState(
                Order.ExpectingNotice.class)).parallel().collect(Collectors.groupingByConcurrent(JOrder::workflowId));
        return ordersPerWorkflow.entrySet().parallelStream().filter(e -> JOCResourceImpl.canAdd(WorkflowPaths.getPath(e.getKey().path().string()),
                permittedFolders)).flatMap(e -> e.getValue().stream()).map(mapper).filter(Objects::nonNull);
    }
    
    private static Board init(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {
        Board item = Globals.objectMapper.readValue(dc.getContent(), Board.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        return item;
    }
    
    private static NoticeState getState(NoticeStateText state) {
        NoticeState nState = new NoticeState();
        nState.set_text(state);
        nState.setSeverity(severities.get(state));
        return nState;
    }
    
    private static final Map<NoticeStateText, Integer> severities = Collections.unmodifiableMap(new HashMap<NoticeStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(NoticeStateText.POSTED, 6);
            put(NoticeStateText.EXPECTED, 8);
        }
    });
    
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
