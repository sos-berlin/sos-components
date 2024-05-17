package com.sos.reports.classes;

import com.sos.joc.model.reporting.result.ReportResult;
import com.sos.reports.classes.ReportHelper.ReportTypes;

public interface IReport {

    public void count(ReportRecord orderRecord);
    public void reset();
    public String getTitle();
    public ReportTypes getType();
    public ReportResult putHits();
    public void setReportArguments(ReportArguments reportArguments);

}
 