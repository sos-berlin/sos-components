package com.sos.js7.converter.autosys.output.js7.helper.bean;

import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;

public class Condition2ConsumeNotice {

    public enum Condition2ConsumeNoticeType {
        CYCLIC, LOOKBACK, CYCLIC_LOOKBACK
    }

    private final Condition condition;
    private final Condition2ConsumeNoticeType type;

    public Condition2ConsumeNotice(Condition condition, Condition2ConsumeNoticeType type) {
        this.condition = condition;
        this.type = type;
    }

    public Condition getCondition() {
        return condition;
    }

    public Condition2ConsumeNoticeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Condition2ConsumeNotice) {
            Condition2ConsumeNotice other = (Condition2ConsumeNotice) o;
            return condition.equals(other.condition) && type.equals(other.type);
        }
        return false;
    }

}
