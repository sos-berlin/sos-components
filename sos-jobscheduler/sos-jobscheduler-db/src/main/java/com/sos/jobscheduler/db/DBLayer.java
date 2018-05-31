package com.sos.jobscheduler.db;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSClassList;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayer.class);

    /** Table SCHEDULER_ORDER_HISTORY */
    public static final String TABLE_SCHEDULER_ORDER_HISTORY = "SCHEDULER_ORDER_HISTORY";
    public static final String TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE = "SCHEDULER_OH_ID_SEQ";
    public static final String DBITEM_SCHEDULER_ORDER_HISTORY = DBItemSchedulerOrderHistory.class.getSimpleName();

    /** Table SCHEDULER_ORDER_STEP_HISTORY */
    public static final String TABLE_SCHEDULER_ORDER_STEP_HISTORY = "SCHEDULER_ORDER_STEP_HISTORY";
    public static final String TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE = "SCHEDULER_OSH_ID_SEQ";
    public static final String DBITEM_SCHEDULER_ORDER_STEP_HISTORY = DBItemSchedulerOrderStepHistory.class.getSimpleName();

    /** Table SCHEDULER_LOGS */
    public static final String TABLE_SCHEDULER_LOGS = "SCHEDULER_LOGS";
    public static final String TABLE_SCHEDULER_LOGS_SEQUENCE = "SCHEDULER_L_ID_SEQ";
    public static final String DBITEM_SCHEDULER_LOGS = DBItemSchedulerOrderStepHistory.class.getSimpleName();

    /** Table SCHEDULER_VARIABLES */
    public static final String TABLE_SCHEDULER_SETTINGS = "SCHEDULER_SETTINGS";
    public static final String DBITEM_SCHEDULER_SETTINGS = DBItemSchedulerSettings.class.getSimpleName();

    public static final String DEFAULT_KEY = ".";

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemSchedulerSettings.class);
        cl.add(DBItemSchedulerOrderHistory.class);
        cl.add(DBItemSchedulerOrderStepHistory.class);
        cl.add(DBItemSchedulerLogs.class);
        return cl;
    }

}
