package com.sos.joc.classes;

import com.sos.commons.db.jobscheduler.DBItemInventoryAgentInstance;
import com.sos.commons.db.jobscheduler.DBItemInventoryInstance;
import com.sos.commons.db.jobscheduler.DBItemInventoryOperatingSystem;
import com.sos.commons.db.joc.DBItemAuditLog;
import com.sos.commons.db.joc.DBItemCalendar;
import com.sos.commons.db.joc.DBItemInventoryCalendarUsage;
import com.sos.commons.db.joc.DBItemJocConfiguration;
import com.sos.commons.db.yade.DBItemYadeFiles;
import com.sos.commons.db.yade.DBItemYadeProtocols;
import com.sos.commons.db.yade.DBItemYadeTransfers;
import com.sos.commons.hibernate.SOSClassList;

public abstract class SOSClassMapping {

    public static SOSClassList getInventoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemInventoryCalendarUsage.class);
        cl.add(DBItemJocConfiguration.class);
        return cl;
    }

    public static SOSClassList getReportingClassMapping() {
        SOSClassList cl = new SOSClassList();
//        cl.add(DBItemReportTask.class);
//        cl.add(DBItemReportTrigger.class);
//        cl.add(DBItemReportExecution.class);
//        cl.add(DBItemReportExecutionDate.class);
//        cl.add(DBItemReportVariable.class);
        cl.add(DBItemAuditLog.class);
//        cl.add(DailyPlanDBItem.class);
        cl.add(DBItemCalendar.class);
//        cl.add(SchedulerEventDBItem.class);
        return cl;
    }

    public static SOSClassList getSchedulerClassMapping() {
        SOSClassList cl = new SOSClassList();
//        cl.add(DBItemSchedulerOrderHistory.class);
//        cl.add(DBItemSchedulerOrderStepHistory.class);
        return cl;
    }

    public static SOSClassList getYadeClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemYadeFiles.class);
        cl.add(DBItemYadeProtocols.class);
        cl.add(DBItemYadeTransfers.class);
        return cl;
    }

}
