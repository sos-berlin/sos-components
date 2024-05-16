package com.sos.reports.frequency;

import java.time.LocalDate;

import com.sos.reports.classes.ReportFrequency;

public class Yearly extends ReportFrequency {

    @Override
    public void nextPeriod() {
        from = from.plusYears(1);
        to = from.plusYears(1).minusDays(1);;
    }

    public void initPeriod(LocalDate monthFrom) {
        from = monthFrom;
        to = from.plusYears(1).minusDays(1);;
    }

}
