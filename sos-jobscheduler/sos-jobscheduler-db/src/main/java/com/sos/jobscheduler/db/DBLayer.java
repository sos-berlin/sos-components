package com.sos.jobscheduler.db;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSClassList;
import com.sos.webservices.db.calendar.DBItemCalendar;
import com.sos.webservices.db.inventory.instance.DBItemInventoryInstance;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayer.class);

    public static final String TABLE_JOBSCHEDULER_ORDER_HISTORY = "SOS_JS_ORDER_HISTORY";
    public static final String TABLE_JOBSCHEDULER_ORDER_HISTORY_SEQUENCE = "SOS_JS_OH_SEQ";
    public static final String DBITEM_JOBSCHEDULER_ORDER_HISTORY = DBItemJobSchedulerOrderHistory.class.getSimpleName();

    public static final String TABLE_JOBSCHEDULER_ORDER_STEP_HISTORY = "SOS_JS_ORDER_STEP_HISTORY";
    public static final String TABLE_JOBSCHEDULER_ORDER_STEP_HISTORY_SEQUENCE = "SOS_JS_OSH_SEQ";
    public static final String DBITEM_JOBSCHEDULER_ORDER_STEP_HISTORY = DBItemJobSchedulerOrderStepHistory.class.getSimpleName();

    public static final String TABLE_JOBSCHEDULER_LOGS = "SOS_JS_LOGS";
    public static final String TABLE_JOBSCHEDULER_LOGS_SEQUENCE = "SOS_JS_L_SEQ";
    public static final String DBITEM_JOBSCHEDULER_LOGS = DBItemJobSchedulerOrderStepHistory.class.getSimpleName();

    public static final String TABLE_JOBSCHEDULER_SETTINGS = "SOS_JS_SETTINGS";
    public static final String DBITEM_JOBSCHEDULER_SETTINGS = DBItemJobSchedulerSettings.class.getSimpleName();

    public static final String DEFAULT_KEY = ".";

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemJobSchedulerSettings.class);
        cl.add(DBItemJobSchedulerOrderHistory.class);
        cl.add(DBItemJobSchedulerOrderStepHistory.class);
        cl.add(DBItemJobSchedulerLogs.class);
        return cl;
    }
    
    public static SOSClassList getOrderInitatorClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemCalendar.class);
        return cl;
    }
    

}
