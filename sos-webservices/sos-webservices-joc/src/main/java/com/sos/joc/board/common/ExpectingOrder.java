package com.sos.joc.board.common;

import js7.data.board.BoardPath;
import js7.data.board.NoticeId;
import js7.data_for_java.order.JOrder;


public class ExpectingOrder {
    
    private JOrder jOrder;
    private BoardPath boardPath;
    private NoticeId noticeId;
    
//    public ExpectingOrder(JOrder jOrder, BoardPath boardPath) {
//        this.jOrder = jOrder;
//        this.boardPath = boardPath;
//        //this.noticeId = ((Order.ExpectingNotice) jOrder.asScala().state()).noticeId().string();
//        this.noticeId = ((Order.ExpectingNotices) jOrder.asScala().state()).expected().find(e -> e.boardPath().equals(boardPath)).get().noticeId().string();
//        //TODO cannot import Expected in: List<Expected> expected = JavaConverters.asJava(((Order.ExpectingNotices) jOrder.asScala().state()).expected());
//        //this.noticeIds = JavaConverters.asJava(((Order.ExpectingNotices) jOrder.asScala().state()).expected().filterImpl(e -> e.boardPath().equals(boardPath), false).toList().map(e -> e.noticeId().string()));
//        this.noticeIds = JavaConverters.asJava(((Order.ExpectingNotices) jOrder.asScala().state()).expected()).stream().filter(e -> e.boardPath().equals(boardPath)).map(e -> e.noticeId().string()).collect(Collectors.toList());
//    }
    
    public ExpectingOrder(JOrder jOrder, BoardPath boardPath, NoticeId noticeId) {
        this.jOrder = jOrder;
        this.boardPath = boardPath;
        this.noticeId = noticeId;
    }
    
    public JOrder getJOrder() {
        return jOrder;
    }
    
    public String getBoardPath() {
        return boardPath.string();
    }
    
    public String getNoticeId() {
        return noticeId.string();
    }
    
}
