package com.sos.joc.plan.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.board.NoticeStateText;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.order.OrderV;

import js7.data.board.BoardPath;
import js7.data.board.NoticeKey;
import js7.data.board.PlannedNoticeKey;
import js7.data.order.OrderId;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JNotice;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerState;

public class PlannedBoards {

    private final Map<BoardPath, ?> jBoards;
    private final Map<String, OrderV> orders;
    private final boolean compact;
    private final JControllerState controllerState;
    private final boolean withSysncState;

    public PlannedBoards(Map<BoardPath, ?> jBoards, Map<String, OrderV> orders, boolean compact, JControllerState controllerState) {
        this.jBoards = jBoards;
        this.orders = orders;
        this.compact = compact;
        this.controllerState = controllerState;
        this.withSysncState = true;
    }
    
    public PlannedBoards(Map<BoardPath, ?> jBoards, Map<String, OrderV> orders, boolean compact) {
        this.jBoards = jBoards;
        this.orders = orders;
        this.compact = compact;
        this.controllerState = null;
        this.withSysncState = false;
    }
    
    @SuppressWarnings("unchecked")
    public Board getPlannedBoard(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {

        Object pbs = jBoards.get(BoardPath.of(dc.getName()));
        if (pbs == null) {
            return getPlannedBoard(init(dc), Collections.emptyList());
        }
        if (pbs instanceof JPlannedBoard) {
            return getPlannedBoard(init(dc), Collections.singleton((JPlannedBoard) pbs));
        } else {
            return getPlannedBoard(init(dc), (Collection<JPlannedBoard>) pbs);
        }
    }
    
    private Board getPlannedBoard(Board item, Collection<JPlannedBoard> pbs) throws JsonParseException, JsonMappingException, IOException {

        int numOfExpectingOrders = 0;
        int numOfNotices = 0;
        List<Notice> notices = new ArrayList<>();
        for (JPlannedBoard pb : pbs) {
            PlanId planid = pb.id().planId();
            numOfExpectingOrders += pb.toNoticePlace().values().stream().mapToInt(this::getNumOfExpectingOrders).sum();
            if (compact) {
                numOfNotices += pb.toNoticePlace().values().stream().mapToInt(this::getNumOfNotices).sum();
            } else {
                notices.addAll(pb.toNoticePlace().entrySet().stream().flatMap(e -> getNotices(planid, e.getKey(), e.getValue()).stream()).collect(
                        Collectors.toList()));
            }
        }

        item.setNumOfExpectingOrders(numOfExpectingOrders);
        if (!compact) {
            item.setNotices(notices);
            item.setNumOfNotices(notices.size());
        } else {
            item.setNumOfNotices(numOfNotices);
        }
        return item;
    }

    private List<Notice> getNotices(PlanId pId, NoticeKey nk, JNoticePlace np) {
        return getNotices(PlannedNoticeKey.apply(pId, nk), np);
    }

    private List<Notice> getNotices(PlannedNoticeKey pnk, JNoticePlace np) {
        String noticeKeyShortString = BoardHelper.getNoticeKeyShortString(pnk);
        List<Notice> notices = new ArrayList<>(2);
        if (np.isAnnounced() || !np.expectingOrderIds().isEmpty()) {
            notices.add(createAnnouncedOrExpectedNotice(noticeKeyShortString, np));
        }
        if (np.notice().isPresent()) {
            notices.add(createPostedNotice(noticeKeyShortString, np.notice().get()));
        }
        return notices;
    }

    private Notice createPostedNotice(String noticeKeyShortString, JNotice jNotice) {
        Notice notice = new Notice();
        notice.setId(noticeKeyShortString);
        jNotice.endOfLife().map(Date::from).ifPresent(notice::setEndOfLife);
        notice.setState(BoardHelper.getState(NoticeStateText.POSTED));
        return notice;
    }

    private Notice createAnnouncedOrExpectedNotice(String noticeKeyShortString, JNoticePlace np) {
        Notice notice = new Notice();
        notice.setId(noticeKeyShortString);
        if (!np.expectingOrderIds().isEmpty()) {
            notice.setExpectingOrders(np.expectingOrderIds().stream().map(OrderId::string).map(this.orders::get).filter(Objects::nonNull).collect(
                    Collectors.toList()));
            // TODO notice.setWorkflowTagsPerWorkflow();
        }
        notice.setState(BoardHelper.getState(NoticeStateText.EXPECTED, np.isAnnounced()));
        return notice;
    }
    
    private int getNumOfNotices(JNoticePlace np) {
        int count = np.isAnnounced() ? 1 : 0;
        if (np.notice().isPresent()) {
            count++; 
        }
        return count;
    }
    
    private int getNumOfExpectingOrders(JNoticePlace np) {
        return np.expectingOrderIds().size();
    }
    
    private SyncStateText getSyncStateText(BoardPath boardpath) {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            if (controllerState.pathToBoardState().containsKey(boardpath)) {
                stateText = SyncStateText.IN_SYNC;
            }
        }
        return stateText;
    }
    
    private Board init(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {
        Board item = Globals.objectMapper.readValue(dc.getContent(), Board.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        if (withSysncState) {
            item.setState(SyncStateHelper.getState(getSyncStateText(BoardPath.of(dc.getName()))));
        }
        return item;
    }

}
