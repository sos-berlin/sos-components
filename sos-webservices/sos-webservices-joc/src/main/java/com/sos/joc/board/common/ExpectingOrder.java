package com.sos.joc.board.common;

import com.sos.joc.model.order.OrderV;

import js7.data.board.BoardPath;
import js7.data.order.Order;
//import js7.data.order.OrderEvent.OrderNoticesExpected$.Expected;
import js7.data_for_java.order.JOrder;


public class ExpectingOrder {
    
    private JOrder jOrder;
    private OrderV vOrder;
    private BoardPath boardPath;
    private String noticeId;
    
    public ExpectingOrder(JOrder jOrder, BoardPath boardPath) {
        this.jOrder = jOrder;
        this.boardPath = boardPath;
        //this.noticeId = ((Order.ExpectingNotice) jOrder.asScala().state()).noticeId().string();
        this.noticeId = ((Order.ExpectingNotices) jOrder.asScala().state()).expected().find(e -> e.boardPath().equals(boardPath)).get().noticeId().string();
        //TODO cannot import Expected in: List<Expected> expected = JavaConverters.asJava(((Order.ExpectingNotices) jOrder.asScala().state()).expected());
    }
    
    public ExpectingOrder(OrderV vOrder, String noticeId) {
        this.vOrder = vOrder;
        this.noticeId = noticeId;
    }
    
    public JOrder getJOrder() {
        return jOrder;
    }
    
    public OrderV getOrderV() {
        return vOrder;
    }
    
    public String getBoardPath() {
        return boardPath.string();
    }
    
    public String getNoticeId() {
        return noticeId;
    }
}
