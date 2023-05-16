package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.Optional;

import com.sos.joc.history.controller.proxy.HistoryEventEntry;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.order.CycleState;
import js7.data.order.Order;
import js7.data.order.Order.State;
import scala.jdk.javaapi.OptionConverters;

// without outcome
public final class FatEventOrderCyclingPrepared extends AFatEventOrderBase {

    private String state;
    private Date next;
    private Date end;

    public FatEventOrderCyclingPrepared(Long eventId, Date eventDatetime, HistoryOrder order) throws Exception {
        super(eventId, eventDatetime, order.getOrderId(), order.getWorkflowInfo().getPosition());
        set(order);
    }

    private void set(HistoryOrder order) {
        State s = order.getOrderState();
        if (s != null && s instanceof Order.BetweenCycles) {
            Order.BetweenCycles b = (Order.BetweenCycles) s;
            state = b.getClass().getSimpleName();

            Optional<CycleState> ocs = OptionConverters.toJava(b.cycleState());
            if (ocs.isPresent()) {
                CycleState cs = ocs.get();
                next = HistoryEventEntry.getDate(cs.next());
                end = HistoryEventEntry.getDate(cs.end());
            }
        }
    }

    public String getState() {
        return state;
    }

    public Date getNext() {
        return next;
    }

    public Date getEnd() {
        return end;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderCyclingPrepared;
    }

}
