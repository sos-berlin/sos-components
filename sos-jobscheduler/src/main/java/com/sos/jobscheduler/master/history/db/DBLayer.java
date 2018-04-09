package com.sos.jobscheduler.master.history.db;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
