package com.sos.joc.db.inventory.deprecated;

import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentClusterDeprecated;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentClusterMemberDeprecated;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentInstance;

public class InventoryDBItemConstants {

    public static final String DEFAULT_NAME = ".";
    public static final String DEFAULT_FOLDER = "/";
    public static final Long DEFAULT_ID = 0L;

    /** Table INVENTORY_AGENT_INSTANCES */
    public static final String DBITEM_INVENTORY_AGENT_INSTANCES = DBItemInventoryAgentInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_INSTANCES = "INVENTORY_AGENT_INSTANCES";
    public static final String TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE = "REPORTING_IAI_ID_SEQ";

    /** Table INVENTORY_AGENT_CLUSTER */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTER = DBItemInventoryAgentClusterDeprecated.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTER = "INVENTORY_AGENT_CLUSTERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE = "REPORTING_IAC_ID_SEQ";

    /** Table INVENTORY_AGENT_CLUSTER_MEMBERS */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS = DBItemInventoryAgentClusterMemberDeprecated.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS = "INVENTORY_AGENT_CLUSTERMEMBERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE = "REPORTING_IACM_ID_SEQ";

}
