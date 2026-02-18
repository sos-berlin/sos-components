package com.sos.joc.plan.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.board.Board;
import com.sos.controller.model.board.BoardDeps;
import com.sos.controller.model.board.Notice;
import com.sos.controller.model.board.NoticeState;
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

    private final static int limitOrdersDefault = 10000;
    private final Map<BoardPath, ?> jBoards;
    private final Map<OrderId, OrderV> orders;
    private final boolean compact;
    private final JControllerState controllerState;
    private final boolean withSysncState;
    private final Integer limitOrders;
    private final boolean withExpectingOrderIds;
    
    private static final Map<NoticeStateText, Integer> severities = Collections.unmodifiableMap(new HashMap<NoticeStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(NoticeStateText.POSTED, 6);
            put(NoticeStateText.EXPECTED, 8);
            put(NoticeStateText.ANNOUNCED, 4);
        }
    });

    public PlannedBoards(Map<BoardPath, ?> jBoards, Map<OrderId, OrderV> orders, boolean compact, Integer limit, JControllerState controllerState) {
        this.jBoards = jBoards;
        this.orders = orders == null ? Collections.emptyMap() : orders;
        this.compact = compact;
        this.controllerState = controllerState;
        this.withSysncState = true;
        this.limitOrders = limit == null ? limitOrdersDefault : limit;
        this.withExpectingOrderIds = false;
    }
    
    public PlannedBoards(Map<BoardPath, ?> jBoards, Map<OrderId, OrderV> orders, boolean compact, Integer limit) {
        this.jBoards = jBoards;
        this.orders = orders == null ? Collections.emptyMap() : orders;
        this.compact = compact;
        this.controllerState = null;
        this.withSysncState = false;
        this.limitOrders = limit == null ? limitOrdersDefault : limit;
        this.withExpectingOrderIds = false;
    }
    
    public PlannedBoards(Map<BoardPath, ?> jBoards, boolean withExpectingOrderIds, boolean compact, Integer limit) {
        this.jBoards = jBoards;
        this.orders = Collections.emptyMap();
        this.compact = compact;
        this.controllerState = null;
        this.withSysncState = false;
        this.limitOrders = limit == null ? limitOrdersDefault : limit;
        this.withExpectingOrderIds = withExpectingOrderIds;
    }
    
    public PlannedBoards(BoardPath boardPath) {
        this.jBoards = Collections.singletonMap(boardPath, Collections.emptyList());
        this.orders = Collections.emptyMap();
        this.compact = true;
        this.controllerState = null;
        this.withSysncState = false;
        this.limitOrders = -1;
        this.withExpectingOrderIds = false;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Board> T getPlannedBoard(DeployedContent dc, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {

        Object pbs = jBoards.get(BoardPath.of(dc.getName()));
        if (pbs == null) {
            return getPlannedBoard(init(dc, clazz), Collections.emptyList());
        }
        if (pbs instanceof JPlannedBoard) {
            return getPlannedBoard(init(dc, clazz), Collections.singleton((JPlannedBoard) pbs));
        } else {
            return getPlannedBoard(init(dc, clazz), (Collection<JPlannedBoard>) pbs);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Board> T getPlannedBoard(T obj) {

        Object pbs = jBoards.values().iterator().next();
        if (pbs == null) {
            return getPlannedBoard(obj, Collections.emptyList());
        }
        if (pbs instanceof JPlannedBoard) {
            return getPlannedBoard(obj, Collections.singleton((JPlannedBoard) pbs));
        } else {
            return getPlannedBoard(obj, (Collection<JPlannedBoard>) pbs);
        }
    }
    
    public Board getPlannedBoard(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {
        return getPlannedBoard(dc, Board.class);
    }
    
    public BoardDeps getPlannedBoardDeps(DeployedContent dc) throws JsonParseException, JsonMappingException, IOException {
        return getPlannedBoard(dc, BoardDeps.class);
    }
    
    private <T extends Board> T getPlannedBoard(T item, Collection<JPlannedBoard> pbs) {

        int numOfExpectingOrders = 0;
        int numOfAnnouncements = 0;
        int numOfExpectedNotices = 0;
        int numOfPostedNotices = 0;
        List<Notice> notices = new ArrayList<>();
        for (JPlannedBoard pb : pbs) {
            Collection<JNoticePlace> noticePlaces = pb.toNoticePlace().values();
            numOfAnnouncements += noticePlaces.stream().mapToInt(this::getNumOfAnnouncements).sum();
            numOfPostedNotices += noticePlaces.stream().mapToInt(this::getNumOfPostedNotices).sum();
            numOfExpectedNotices += noticePlaces.stream().mapToInt(this::getNumOfExpectedNotices).sum();
            numOfExpectingOrders += noticePlaces.stream().mapToInt(this::getNumOfExpectingOrders).sum();
            if (!compact) {
                PlanId planid = pb.id().planId();
                notices.addAll(pb.toNoticePlace().entrySet().stream().flatMap(e -> getNotices(planid, e.getKey(), e.getValue()).stream()).collect(
                        Collectors.toList()));
            }
        }

        item.setNumOfExpectingOrders(numOfExpectingOrders);
        item.setNumOfAnnouncements(numOfAnnouncements);
        item.setNumOfExpectedNotices(numOfExpectedNotices);
        item.setNumOfPostedNotices(numOfPostedNotices);
        if (!compact) {
            item.setNotices(notices);
            item.setNumOfNotices(notices.size());
        } else {
            item.setNumOfNotices(numOfExpectedNotices + numOfAnnouncements + numOfPostedNotices);
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
        notice.setState(getState(NoticeStateText.POSTED));
        return notice;
    }

    private Notice createAnnouncedOrExpectedNotice(String noticeKeyShortString, JNoticePlace np) {
        Notice notice = new Notice();
        notice.setId(noticeKeyShortString);
        if (!np.expectingOrderIds().isEmpty()) {
            if (this.orders != null && !this.orders.isEmpty()) {
                Stream<OrderV> expectingOrders = np.expectingOrderIds().stream().map(this.orders::get).filter(Objects::nonNull);
                if (limitOrders > -1 && np.expectingOrderIds().size() > limitOrders) {
                    expectingOrders = expectingOrders.sorted(Comparator.comparingLong(OrderV::getScheduledFor).reversed()).limit(limitOrders
                            .longValue());
                }
                notice.setExpectingOrders(expectingOrders.collect(Collectors.toList()));
            } else if (withExpectingOrderIds) {
                Stream<String> expectingOrderIds = np.expectingOrderIds().stream().map(OrderId::string);
                if (limitOrders > -1 && np.expectingOrderIds().size() > limitOrders) {
                    expectingOrderIds = expectingOrderIds.limit(limitOrders.longValue());
                }
                notice.setExpectingOrderIds(expectingOrderIds.collect(Collectors.toSet()));
            }
            // TODO notice.setWorkflowTagsPerWorkflow();
        }
        notice.setState(getState(NoticeStateText.EXPECTED, np.isAnnounced()));
        return notice;
    }
    
    
    private int getNumOfAnnouncements(JNoticePlace np) {
        return np.isAnnounced() ? 1 : 0;
    }
    
    private int getNumOfExpectingOrders(JNoticePlace np) {
        //return np.expectingOrderIds().stream().filter(orders::containsKey).mapToInt(oId -> 1).sum();
        return np.expectingOrderIds().size();
    }
    
    private int getNumOfPostedNotices(JNoticePlace np) {
        return np.notice().isPresent() ? 1 : 0;
    }
    
    private int getNumOfExpectedNotices(JNoticePlace np) {
        //return np.expectingOrderIds().stream().filter(orders::containsKey).findAny().isEmpty() ? 0 : 1;
        return np.expectingOrderIds().isEmpty() ? 0 : 1;
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
    
    private <T extends Board> T init(DeployedContent dc, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        T item = Globals.objectMapper.readValue(dc.getContent(), clazz);
        item.setTYPE(null);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        item.setHasNote(dc.getHasNote());
        if (withSysncState) {
            item.setState(SyncStateHelper.getState(getSyncStateText(BoardPath.of(dc.getName()))));
        }
        return item;
    }
    
    private static NoticeState getState(NoticeStateText state) {
        NoticeState nState = new NoticeState();
        nState.set_text(state);
        nState.setSeverity(severities.get(state));
        return nState;
    }
    
    private static NoticeState getState(NoticeStateText state, boolean isAnnounced) {
        if (isAnnounced) {
            return getState(NoticeStateText.ANNOUNCED);
        } else {
            return getState(state);
        }
    }

}
