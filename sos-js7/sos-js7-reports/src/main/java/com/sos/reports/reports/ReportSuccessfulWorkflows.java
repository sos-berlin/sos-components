package com.sos.reports.reports;

import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportRecord;

public class ReportSuccessfulWorkflows extends ReportStateWorkflows implements IReport {
 
  
    
    @Override
    public boolean getCondition(ReportRecord orderRecord) {
        return !orderRecord.getError();
    }

 
 
}
