package com.sos.joc.board.common;

import com.sos.joc.model.order.OrderV;

import js7.data.board.BoardPath;
import js7.data.order.Order;
import js7.data_for_java.order.JOrder;


public class ExpectingOrder {
    
    private JOrder jOrder;
    private OrderV vOrder;
    private BoardPath boardPath;
    private String noticeId;
    
    public ExpectingOrder(JOrder jOrder, BoardPath boardPath) {
        this.jOrder = jOrder;
        this.boardPath = boardPath;
        this.noticeId = ((Order.ExpectingNotice) jOrder.asScala().state()).noticeId().string();
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
