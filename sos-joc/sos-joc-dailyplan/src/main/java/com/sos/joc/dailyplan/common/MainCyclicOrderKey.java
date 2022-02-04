package com.sos.joc.dailyplan.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;

public class MainCyclicOrderKey implements Comparable<MainCyclicOrderKey> {

    private final String controllerId;
    private final String workflowName;
    private final String scheduleName;
    private final String orderName;

    private final String repeat;
    private final String periodBegin;
    private final String periodEnd;

    public MainCyclicOrderKey(PlannedOrder order) {
        controllerId = order.getControllerId();
        workflowName = order.getWorkflowName();
        scheduleName = order.getScheduleName();
        orderName = order.getOrderName();

        repeat = order.getPeriod().getRepeat();
        periodBegin = order.getPeriod().getBegin();
        periodEnd = order.getPeriod().getEnd();
    }

    public MainCyclicOrderKey(DBItemDailyPlanOrder item) {
        controllerId = item.getControllerId();
        workflowName = item.getWorkflowName();
        scheduleName = item.getScheduleName();
        orderName = item.getOrderName();

        DateFormat format = new SimpleDateFormat("hh:mm:ss");
        repeat = String.valueOf(item.getRepeatInterval());
        periodBegin = format.format(item.getPeriodBegin());
        periodEnd = format.format(item.getPeriodEnd());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("controllerId=").append(controllerId);
        sb.append(",workflowName=").append(workflowName);
        sb.append(",scheduleName=").append(scheduleName);
        sb.append(",orderName=").append(orderName);
        sb.append(",repeat=").append(repeat);
        sb.append(",periodBegin=").append(periodBegin);
        sb.append(",periodEnd=").append(periodEnd);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(controllerId);
        b.append(workflowName);
        b.append(scheduleName);
        b.append(orderName);
        b.append(repeat);
        b.append(periodBegin);
        b.append(periodEnd);
        return b.toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        MainCyclicOrderKey o = (MainCyclicOrderKey) other;
        EqualsBuilder b = new EqualsBuilder();
        b.append(controllerId, o.controllerId);
        b.append(workflowName, o.workflowName);
        b.append(scheduleName, o.scheduleName);
        b.append(orderName, o.orderName);
        b.append(repeat, o.repeat);
        b.append(periodBegin, o.periodBegin);
        b.append(periodEnd, o.periodEnd);
        return b.isEquals();
    }

    @Override
    public int compareTo(MainCyclicOrderKey other) {
        Comparator<MainCyclicOrderKey> c = Comparator.comparing(MainCyclicOrderKey::getControllerId);
        c = c.thenComparing(MainCyclicOrderKey::getWorkflowName);
        c = c.thenComparing(MainCyclicOrderKey::getScheduleName);
        c = c.thenComparing(MainCyclicOrderKey::getOrderName);
        c = c.thenComparing(MainCyclicOrderKey::getRepeat);
        c = c.thenComparing(MainCyclicOrderKey::getPeriodBegin);
        c = c.thenComparing(MainCyclicOrderKey::getPeriodEnd);
        return c.compare(this, other);
    }

    public String getControllerId() {
        return controllerId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getOrderName() {
        return orderName;
    }

    public String getRepeat() {
        return repeat;
    }

    public String getPeriodBegin() {
        return periodBegin;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

}
