package com.sos.commons.db.history;

import com.sos.commons.db.DBItemConstants;

public class HistoryDBItemConstants extends DBItemConstants{

    /** Table SCHEDULER_ORDER_HISTORY */
    public static final String TABLE_SCHEDULER_ORDER_HISTORY = "SCHEDULER_ORDER_HISTORY";
    public static final String TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE = "SCHEDULER_OH_ID_SEQ";
    public static final String DBITEM_SCHEDULER_ORDER_HISTORY = DBItemSchedulerOrderHistory.class.getSimpleName();

    /** Table SCHEDULER_ORDER_STEP_HISTORY */
    public static final String TABLE_SCHEDULER_ORDER_STEP_HISTORY = "SCHEDULER_ORDER_STEP_HISTORY";
    public static final String TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE = "SCHEDULER_OSH_ID_SEQ";
    public static final String DBITEM_SCHEDULER_ORDER_STEP_HISTORY = DBItemSchedulerOrderStepHistory.class.getSimpleName();

}
