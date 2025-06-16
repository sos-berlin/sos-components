package com.sos.joc.dailyplan.common;

import com.sos.inventory.model.calendar.Period;

public class AbsoluteMainPeriod {

    private final String schedulePath;
    private final Period period;

    public AbsoluteMainPeriod(String schedulePath, Period period) {
        this.schedulePath = schedulePath;
        this.period = period;
    }

    public AbsoluteMainPeriod(MainCyclicOrderKey cyclicKey, Period period) {
        this(cyclicKey.getScheduleName(), period);
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public Period getPeriod() {
        return period;
    }

}
