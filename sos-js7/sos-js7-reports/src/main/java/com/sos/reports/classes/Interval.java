package com.sos.reports.classes;

import java.time.LocalDate;

public class Interval {

    private Integer yearFrom = 0;
    private Integer monthFrom = 0;
    private Integer yearTo = 0;
    private Integer monthTo = 0;
    private Integer currentMonth = 0;
    private Integer currentYear = 0;

    public void setInterval(LocalDate from, LocalDate to) {

        yearFrom = from.getYear();
        yearTo = to.getYear();
        monthFrom = from.getMonthValue();
        monthTo = to.getMonthValue();
        currentMonth = monthFrom;
        currentYear = yearFrom;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public Integer getMonthFrom() {
        return monthFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public Integer getMonthTo() {
        return monthTo;
    }

    public void next() {
        currentMonth = currentMonth + 1;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear = currentYear + 1;
        }

    }

    public Boolean end() {
        return ((currentMonth > monthTo) && (currentYear == yearTo) || currentYear > yearTo);
    }

    public String currentInterval() {
        return String.valueOf(currentYear) + "-" + String.format("%02d", currentMonth);
    }
    

}
