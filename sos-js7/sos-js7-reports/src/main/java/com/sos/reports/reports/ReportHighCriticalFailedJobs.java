package com.sos.reports.reports;

import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportRecord;

public class ReportHighCriticalFailedJobs extends ReportStateJobs implements IReport {

    @Override
    public boolean getCondition(ReportRecord jobRecord) {
        return (jobRecord.getError() && jobRecord.getCriticality() ==  1);
    }

}
