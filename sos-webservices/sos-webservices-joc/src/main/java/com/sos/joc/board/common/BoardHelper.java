package com.sos.joc.board.common;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.common.Folder;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.board.BoardState;
import js7.data.order.OrderId;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.controller.JControllerState;
import scala.collection.JavaConverters;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    public static Board getBoard(JControllerState controllerState, DeployedContent dc, Set<Folder> permittedFolders) throws Exception {
        SyncStateText stateText = SyncStateText.UNKNOWN;

        Board item = Globals.objectMapper.readValue(dc.getContent(), Board.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        
        String boardname = JocInventory.pathToName(dc.getPath());
        
        JBoardState jBoardState = null;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            Either<Problem, JBoardState> boardV = controllerState.pathToBoardState(BoardPath.of(boardname));
            if (boardV != null && boardV.isRight()) {
                stateText = SyncStateText.IN_SYNC;
                jBoardState = boardV.get();
            }
        }
        
        item.setState(SyncStateHelper.getState(stateText));

        if (jBoardState != null) {
            final BoardState bs = jBoardState.asScala();
            List<Notice> notices = new ArrayList<>();
            
            JavaConverters.asJava(bs.notices()).forEach(n -> {
                Notice notice = new Notice();
                notice.setId(n.id().string());
                notice.setEndOfLife(Date.from(Instant.ofEpochMilli(n.endOfLife().toEpochMilli())));
                Set<OrderId> orderIds = JavaConverters.asJava(bs.expectingOrders(n.id()));
                notice.setExpectingOrders(controllerState.ordersBy(o -> orderIds.contains(o.id())).map(o -> {
                    try {
                        return OrdersHelper.mapJOrderToOrderV(o, true, permittedFolders, null);
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
    
    public static JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

}
