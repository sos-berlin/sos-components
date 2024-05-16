package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Every6months extends ReportFrequency {

    public void nextPeriod() {
        from = from.plusMonths(6);
        to = from.plusMonths(6).minusDays(1);;
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusMonths(6).minusDays(1);;
    }
}
