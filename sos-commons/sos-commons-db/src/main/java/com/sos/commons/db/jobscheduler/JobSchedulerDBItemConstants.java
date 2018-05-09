package com.sos.commons.db.jobscheduler;

import com.sos.commons.db.DBItemConstants;

public class JobSchedulerDBItemConstants extends DBItemConstants {

    /** Table INVENTORY_OPERATING_SYSTEM */
    public static final String DBITEM_INVENTORY_OPERATING_SYSTEMS = DBItemInventoryOperatingSystem.class.getSimpleName();
    public static final String TABLE_INVENTORY_OPERATING_SYSTEMS = "INVENTORY_OPERATING_SYSTEMS";
    public static final String TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE = "REPORTING_IOS_ID_SEQ";
    
    /** Table INVENTORY_INSTANCES */
    public static final String DBITEM_INVENTORY_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_INSTANCES = "INVENTORY_INSTANCES";
    public static final String TABLE_INVENTORY_INSTANCES_SEQUENCE = "REPORTING_II_ID_SEQ";

    /** Table INVENTORY_AGENT_INSTANCES */
    public static final String DBITEM_INVENTORY_AGENT_INSTANCES = DBItemInventoryAgentInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_INSTANCES = "INVENTORY_AGENT_INSTANCES";
    public static final String TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE = "REPORTING_IAI_ID_SEQ";
    
    /** Table INVENTORY_AGENT_CLUSTER */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTER = DBItemInventoryAgentCluster.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTER = "INVENTORY_AGENT_CLUSTERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE = "REPORTING_IAC_ID_SEQ";
    
    /** Table INVENTORY_AGENT_CLUSTER_MEMBERS */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS = DBItemInventoryAgentClusterMember.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS = "INVENTORY_AGENT_CLUSTERMEMBERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE = "REPORTING_IACM_ID_SEQ";
    
}
