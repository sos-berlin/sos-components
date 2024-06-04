package com.sos.reports.classes;

import java.time.LocalDate;

import com.sos.inventory.model.report.Frequency;

public abstract class ReportFrequency {

    protected LocalDate from;
    protected LocalDate to;
    protected Frequency frequency;

    public abstract void nextPeriod();

    public abstract void initPeriod(LocalDate monthFrom);

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public boolean endOfInterval(LocalDate orderDate) {
        return orderDate.isAfter(to);
    }

    public String currentFromTo() {
        return String.valueOf(from.getYear() + "-" + String.format("%02d", from.getMonthValue()) + "-" + String.format("%02d", from.getDayOfMonth())
                + "_" + to.getYear() + "-" + String.format("%02d", to.getMonthValue()) + "-" + String.format("%02d", to.getDayOfMonth()));
    }

    public String getFromMonth() {
        return String.valueOf(from.getYear() + "-" + String.format("%02d", from.getMonthValue()));
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public boolean isBefore(Interval interval) {
        int m;
        if (interval.getCurrentYear() > to.getYear()) {
            m = 13;
        } else {
            m = interval.getCurrentMonth();
        }
        return (to.getMonth().getValue() < m);
    }
}
