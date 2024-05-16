package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Every2weeks extends ReportFrequency {

    public void nextPeriod() {
        from = from.plusWeeks(2);
        to = from.plusWeeks(2).minusDays(1);;
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusWeeks(2).minusDays(1);;
    }
}
