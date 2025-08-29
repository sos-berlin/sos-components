package com.sos.joc.classes.logs;

import java.util.Objects;

public class TaskLogBean {

    private final Long orderHistoryId;
    private final Long historyId;

    protected TaskLogBean(LogTaskContent content) {
        // check == null - does not seem necessary, but ...
        this.orderHistoryId = content.getOrderId() == null ? 0L : content.getOrderId();
        this.historyId = content.getHistoryId();
    }

    protected Long getOrderHistoryId() {
        return orderHistoryId;
    }

    protected Long getHistoryId() {
        return historyId;
    }

    @Override
    public String toString() {
        return "orderHistoryId=" + orderHistoryId + ", historyId=" + historyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskLogBean)) {
            return false;
        }
        TaskLogBean that = (TaskLogBean) o;
        return Objects.equals(orderHistoryId, that.orderHistoryId) && Objects.equals(historyId, that.historyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderHistoryId, historyId);
    }

}
