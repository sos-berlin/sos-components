package com.sos.joc.classes;

import com.sos.commons.util.SOSClassList;
import com.sos.jobscheduler.db.audit.DBItemAuditLog;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.jobscheduler.db.calendar.DBItemInventoryCalendarUsage;
import com.sos.jobscheduler.db.configuration.DBItemJocConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentInstance;
import com.sos.jobscheduler.db.os.DBItemInventoryOperatingSystem;
import com.sos.yade.db.DBItemYadeFiles;
import com.sos.yade.db.DBItemYadeProtocols;
import com.sos.yade.db.DBItemYadeTransfers;

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
