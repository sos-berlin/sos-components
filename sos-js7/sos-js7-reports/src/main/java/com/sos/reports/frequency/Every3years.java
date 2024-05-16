package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Every3years extends ReportFrequency {

    public void nextPeriod() {
        from = from.plusYears(3);
        to = from.plusYears(3).minusDays(1);;
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusYears(3).minusDays(1);;
    }
}
