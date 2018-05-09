package com.sos.commons.db.yade;

import com.sos.commons.db.DBItemConstants;

public class YadeDBItemConstants extends DBItemConstants {

    /** Table INVENTORY_OPERATING_SYSTEM */
    public static final String DBITEM_YADE_TRANSFERS = DBItemYadeTransfers.class.getSimpleName();
    public static final String TABLE_YADE_TRANSFERS = "YADE_TRANSFERS";
    public static final String TABLE_YADE_TRANSFERS_SEQUENCE = "YADE_TR_ID_SEQ";
    
    /** Table INVENTORY_INSTANCES */
    public static final String DBITEM_YADE_PROTOCOLS = DBItemYadeProtocols.class.getSimpleName();
    public static final String TABLE_YADE_PROTOCOLS = "YADE_PROTOCOLS";
    public static final String TABLE_YADE_PROTOCOLS_SEQUENCE = "YADE_PR_ID_SEQ";

    /** Table INVENTORY_AGENT_INSTANCES */
    public static final String DBITEM_YADE_FILES = DBItemYadeFiles.class.getSimpleName();
    public static final String TABLE_YADE_FILES = "YADE_FILES";
    public static final String TABLE_YADE_FILES_SEQUENCE = "YADE_FI_ID_SEQ";
    
}
