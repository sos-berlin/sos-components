package com.sos.joc.plan.common;

import java.io.IOException;
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
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.order.OrderV;

import js7.data.board.BoardPath;
import js7.data.board.NoticeKey;
import js7.data.board.PlannedNoticeKey;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JNotice;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.board.JPlannedBoard;

public class PlannedBoards {
    
    private final PlanId planId;
    private final Map<BoardPath, JPlannedBoard> jBoards;
    private final Map<String, OrderV> orders;
    
    public PlannedBoards(PlanId planId, Map<BoardPath, JPlannedBoard> jBoards, Map<String, OrderV> orders) {
        this.planId = planId;
        this.jBoards = jBoards;
        this.orders = orders;
    }
    
    public Board getPlannedBoard(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {

        Map<NoticeKey, JNoticePlace> noticePlace = jBoards.get(BoardPath.of(dc.getName())).toNoticePlace();
        
        Board item = BoardHelper.init(dc);
        item.setNotices(noticePlace.entrySet().stream().map(e -> getNotice(e.getKey(), e.getValue())).collect(Collectors.toList()));
        item.setNumOfNotices(item.getNotices().size());
        item.setNumOfExpectingOrders(item.getNotices().stream().map(Notice::getExpectingOrders).filter(Objects::nonNull).mapToInt(List::size).sum());
        return item;
    }
    
    private Notice getNotice(NoticeKey nk, JNoticePlace np) {
        return getNotice(PlannedNoticeKey.apply(planId, nk), np);
    }
    
    private Notice getNotice(PlannedNoticeKey pnk, JNoticePlace np) {
        Notice notice = new Notice();
        boolean isAnnounced = np.isAnnounced();
        //notice.setKey(pnk.noticeKey().string());
        notice.setId(BoardHelper.getNoticeKeyShortString(pnk));
        notice.setExpectingOrders(np.expectingOrderIds().stream().map(oId -> orders.get(oId.string())).filter(Objects::nonNull).collect(Collectors
                .toList()));
        notice.setWorkflowTagsPerWorkflow(null);
        if (!notice.getExpectingOrders().isEmpty()) {
            // TODO
            // notice.setWorkflowTagsPerWorkflow();
            notice.setState(BoardHelper.getState(NoticeStateText.EXPECTED, isAnnounced));
            notice.setEndOfLife(null);
        } else {
            notice.setExpectingOrders(null);
            np.notice().flatMap(JNotice::endOfLife).map(Date::from).ifPresent(d -> notice.setEndOfLife(d));
            notice.setState(BoardHelper.getState(NoticeStateText.POSTED, isAnnounced));
        }

        return notice;
    }

}
