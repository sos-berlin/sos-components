package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Every3months extends ReportFrequency {

    public void nextPeriod() {
        from = from.plusMonths(3);
        to = from.plusMonths(3).minusDays(1);;
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusMonths(3).minusDays(1);;
    }
}
