package com.sos.reports.classes;

import java.time.LocalDate;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.inventory.model.report.Frequency;
import com.sos.reports.frequency.Every2weeks;
import com.sos.reports.frequency.Every3months;
import com.sos.reports.frequency.Every3years;
import com.sos.reports.frequency.Every6months;
import com.sos.reports.frequency.Monthly;
import com.sos.reports.frequency.Weekly;
import com.sos.reports.frequency.Yearly;

public class ReportArguments {

    public String reportId;
    public String inputDirectory;
    public ReportFrequency reportFrequency;
    public String outputDirectory;
    public String controllerId;
    public LocalDate monthFrom;
    public LocalDate monthTo;
    public Integer hits = 10;
    public String logDir;

    public void setInputDirectory(String inputDirectory) {
        if (!inputDirectory.endsWith("/")) {
            inputDirectory = inputDirectory + "/";
        }
        this.inputDirectory = inputDirectory;

    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public void setMonthFrom(String inMonthFrom) {
        this.monthFrom = LocalDate.parse(inMonthFrom + "-01");
    }

    public void setMonthTo(String inMonthTo) {
        this.monthTo = LocalDate.parse(inMonthTo + "-01");
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public void setHits(String hits) {
        try {
            this.hits = Integer.valueOf(hits);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    public void setReqportFrequency(String frequencyValue) {

        frequencyValue = frequencyValue.toUpperCase();
        Frequency frequency = Frequency.valueOf(frequencyValue);

        try {
            switch (frequency) {
            case WEEKLY:
                this.reportFrequency = new Weekly();
            case TWO_WEEKS:
                this.reportFrequency = new Every2weeks();
            case MONTHLY:
                this.reportFrequency = new Monthly();
            case THREE_MONTHS:
                this.reportFrequency = new Every3months();
            case SIX_MONTHS:
                this.reportFrequency = new Every6months();
            case YEARLY:
                this.reportFrequency = new Yearly();
            case THREE_YEARS:
                this.reportFrequency = new Every3years();
            default:
                this.reportFrequency = new Monthly();

            }
            this.reportFrequency.initPeriod(monthFrom);
            this.reportFrequency.setFrequency(frequency);
        } catch (Exception e) {
            throw e;
        }
    }

    public void checkRequired() throws SOSRequiredArgumentMissingException {
        String msg = "";
        if (reportId == null) {
            msg += ("Missing parameter value for <-r --report>") + "\n";
        }
        if (inputDirectory == null) {
            msg += ("Missing parameter value for <-i --inputDirectory>") + "\n";
        }
        if (reportFrequency == null) {
            msg += ("Missing parameter value for <-p --frequencies>") + "\n";
        }
        if (outputDirectory == null) {
            msg += ("Missing parameter value for <-o --outputDirectory>") + "\n";
        }
        if (monthFrom == null) {
            msg += ("Missing parameter value for <-s --monthFrom>") + "\n";
        }
        if (!msg.isEmpty()) {
            throw new SOSRequiredArgumentMissingException(msg);
        }
     }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getOutputFilename() {
        return outputDirectory + "/report_" + reportFrequency.getFromMonth() + "-" + String.format("%02d", reportFrequency.getTo().getDayOfMonth())
                + "_" + reportFrequency.getFrequency().intValue() + ".json";
    }

}
