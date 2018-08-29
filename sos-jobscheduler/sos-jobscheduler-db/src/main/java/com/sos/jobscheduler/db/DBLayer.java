package com.sos.jobscheduler.db;

import java.io.Serializable;

import com.sos.commons.util.SOSClassList;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.jobscheduler.db.general.DBItemSetting;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String GENERAL_TABLE_SETTINGS = "SOS_JS_SETTINGS";
    public static final String GENERAL_DBITEM_SETTING = DBItemSetting.class.getSimpleName();

    public static final String HISTORY_TABLE_ORDERS = "SOS_JS_HISTORY_ORDERS";
    public static final String HISTORY_TABLE_ORDERS_SEQUENCE = "SOS_JS_HO_SEQ";
    public static final String HISTORY_DBITEM_ORDER = DBItemOrder.class.getSimpleName();

    public static final String HISTORY_TABLE_ORDER_STEPS = "SOS_JS_HISTORY_ORDER_STEPS";
    public static final String HISTORY_TABLE_ORDER_STEPS_SEQUENCE = "SOS_JS_HOS_SEQ";
    public static final String HISTORY_DBITEM_ORDER_STEP = DBItemOrderStep.class.getSimpleName();

    public static final String HISTORY_TABLE_LOGS = "SOS_JS_HISTORY_LOGS";
    public static final String HISTORY_TABLE_LOGS_SEQUENCE = "SOS_JS_HL_SEQ";
    public static final String HISTORY_DBITEM_LOG = DBItemLog.class.getSimpleName();

    public static final String HISTORY_TABLE_MASTERS = "SOS_JS_HISTORY_MASTERS";
    public static final String HISTORY_TABLE_MASTERS_SEQUENCE = "SOS_JS_HM_SEQ";
    public static final String HISTORY_DBITEM_MASTER = DBItemMaster.class.getSimpleName();

    public static final String HISTORY_TABLE_AGENTS = "SOS_JS_HISTORY_AGENTS";
    public static final String HISTORY_TABLE_AGENTS_SEQUENCE = "SOS_JS_HA_SEQ";
    public static final String HISTORY_DBITEM_AGENT = DBItemAgent.class.getSimpleName();

    public static final String DEFAULT_KEY = ".";

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemSetting.class);
        cl.add(DBItemOrder.class);
        cl.add(DBItemOrderStep.class);
        cl.add(DBItemLog.class);
        cl.add(DBItemMaster.class);
        cl.add(DBItemAgent.class);
        return cl;
    }

    public static SOSClassList getOrderInitatorClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemCalendar.class);
        return cl;
    }

}
