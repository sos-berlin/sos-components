package com.sos.reports.reports;

import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportRecord;

public class ReportFailedJobs extends ReportStateJobs implements IReport {

    @Override
    public boolean getCondition(ReportRecord jobRecord) {
        return jobRecord.getError();
    }

}
