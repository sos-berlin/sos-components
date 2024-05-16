package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Monthly extends ReportFrequency {

    public void nextPeriod() {
        from = from.plusMonths(1);
        to = from.plusMonths(1).minusDays(1);
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusMonths(1).minusDays(1);
    }
}
