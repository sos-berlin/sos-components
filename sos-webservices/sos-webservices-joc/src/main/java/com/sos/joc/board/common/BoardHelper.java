package com.sos.joc.board.common;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.board.NoticeState;
import com.sos.controller.model.board.NoticeStateText;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.order.OrderItem;
import com.sos.joc.Globals;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.board.BoardState;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.instructions.ExpectNotice;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import scala.collection.JavaConverters;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    public static Board getBoard(JControllerState controllerState, DeployedContent dc, Set<Folder> permittedFolders,
            List<JOrder> expectingOrders) throws Exception {
        SyncStateText stateText = SyncStateText.UNKNOWN;

        Board item = Globals.objectMapper.readValue(dc.getContent(), Board.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        
        JBoardState jBoardState = null;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            Either<Problem, JBoardState> boardV = controllerState.pathToBoardState(BoardPath.of(dc.getName()));
            if (boardV != null && boardV.isRight()) {
                stateText = SyncStateText.IN_SYNC;
                jBoardState = boardV.get();
            }
        }
        
        item.setState(SyncStateHelper.getState(stateText));
        
        ConcurrentMap<String, List<OrderV>> expectNotices = expectingOrders.parallelStream().map(o -> {
            try {
                OrderItem oItem = Globals.objectMapper.readValue(o.toJson(), OrderItem.class);
                String noticeId = oItem.getState().getNoticeId();
                if (noticeId == null) {
                    return null;
                }
                // TODO remove final Parameters
                return new ExpectingOrder(OrdersHelper.mapJOrderToOrderV(o, oItem, true, permittedFolders, null, null), noticeId);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.groupingByConcurrent(ExpectingOrder::getNoticeId, Collectors.mapping(ExpectingOrder::getOrderV,
                Collectors.toList())));
        
        List<Notice> notices = new ArrayList<>();
        
        expectNotices.forEach((noticeId, orderVs) -> {
            Notice notice = new Notice();
            notice.setId(noticeId);
            notice.setEndOfLife(null);
            notice.setExpectingOrders(orderVs);
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
                Set<OrderId> orderIds = JavaConverters.asJava(bs.expectingOrders(n.id()));
                notice.setExpectingOrders(controllerState.ordersBy(o -> orderIds.contains(o.id())).parallel().map(o -> {
                    try {
                        // TODO remove final Parameters
                        return OrdersHelper.mapJOrderToOrderV(o, true, permittedFolders, null, null);
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
                notices.add(notice);
            });
            item.setNotices(notices);
        }

        return item;
    }
    
    public static ConcurrentMap<String, List<JOrder>> getExpectingOrders(JControllerState controllerState, Integer limit) {
        if (controllerState == null) {
            return new ConcurrentHashMap<>();
        }
        return controllerState.ordersBy(JOrderPredicates.byOrderState(Order.ExpectingNotice.class)).limit(limit.longValue()).parallel().map(
                order -> new ExpectingOrder(order, ((ExpectNotice) controllerState.asScala().instruction(order.asScala().workflowPosition()))
                        .boardPath())).collect(Collectors.groupingByConcurrent(ExpectingOrder::getBoardPath, Collectors.mapping(
                                ExpectingOrder::getJOrder, Collectors.toList())));
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

}
