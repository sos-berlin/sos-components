package com.sos.jobscheduler.db.inventory;

import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentCluster;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentClusterMember;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentInstance;

public class InventoryDBItemConstants {

    public static final String DEFAULT_NAME = ".";
    public static final String DEFAULT_FOLDER = "/";
    public static final Long DEFAULT_ID = 0L;

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
