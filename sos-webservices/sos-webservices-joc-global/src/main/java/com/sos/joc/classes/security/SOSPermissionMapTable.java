package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SOSPermissionMapTable {

    private Map<String, List<String>> permMap;

    public SOSPermissionMapTable() {
        super();
    }

    private void addMapEntry(String key, String value) {
        if (permMap.get(key) == null) {
            permMap.put(key, new ArrayList<String>());
        }
        permMap.get(key).add(value);

    }

    private void initMap() {

        permMap = new HashMap<String, List<String>>();
        addMapEntry("sos:products:joc_cockpit:joc", "sos:products:joc");
        addMapEntry("sos:products:joc_cockpit", "sos:products:joc");

        addMapEntry("sos:products:joc_cockpit:joc:view:log", "sos:products:joc:get_log");
        
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:view", "sos:products:joc:adminstration:controllers:view");

        addMapEntry("sos:products:joc_cockpit:jobscheduler_master", "sos:products:joc:adminstration:controllers:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master", "sos:products:controller:restart");
        
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:view:status", "sos:products:joc:adminstration:controllers:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:view:parameter", "sos:products:joc:adminstration:controllers:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:view:mainlog", "sos:products:controller:get_log");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:restart:terminate", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:restart", "sos:products:controller:restart");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:restart:abort", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute", "sos:products:controller:restart");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:terminate", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:abort", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:execute:stop", "sos:products:controller:terminate");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:edit_permissions", "sos:products:joc:adminstration:accounts:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration", "sos:products:joc:adminstration");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:view:others", "sos:products:joc:others:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:view:inventory", "sos:products:joc:inventory:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:view:yade", "sos:products:joc:inventory:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:view:notification",
                "sos:products:joc:notification:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:edit", "sos:products:joc:others:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:edit", "sos:products:joc:inventory:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:edit", "sos:products:joc:notification:manage");

        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:others:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:inventory:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:notification:manage");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:others:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:inventory:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations", "sos:products:joc:notification:view");

        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:job", "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:job_chain",
                "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:lock", "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:monitor",
                "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:order", "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:process_class",
                "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:schedule",
                "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy:xml_editor",
                "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master:administration:configurations:deploy", "sos:products:controller:deployment");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_master_cluster:view:status", "sos:products:controller:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_universal_agent:view:status", "sos:products:controller:agents:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_universal_agent:view", "sos:products:controller:agents:view");
        addMapEntry("sos:products:joc_cockpit:jobscheduler_universal_agent", "sos:products:controller:agents");
        addMapEntry("sos:products:joc_cockpit:daily_plan:view:status", "sos:products:joc:dailyplan:view");
        addMapEntry("sos:products:joc_cockpit:daily_plan:view", "sos:products:joc:dailyplan:view");
        addMapEntry("sos:products:joc_cockpit:daily_plan", "sos:products:joc:dailyplan");
        addMapEntry("sos:products:joc_cockpit:history:view:status", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:history:view", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:history", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:documentation:view", "sos:products:joc:documentations:view");
        addMapEntry("sos:products:joc_cockpit:documentation", "sos:products:joc:documentations");
        addMapEntry("sos:products:joc_cockpit:documentation:import", "sos:products:joc:documentations:manage");
        addMapEntry("sos:products:joc_cockpit:documentation", "sos:products:joc:documentations:manage");
        addMapEntry("sos:products:joc_cockpit:documentation:export", "sos:products:joc:documentations:manage");
        addMapEntry("sos:products:joc_cockpit:documentation:delete", "sos:products:joc:documentations:manage");
        addMapEntry("sos:products:joc_cockpit:order:view:status", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:order:view", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:order", "sos:products:controller:orders");
        addMapEntry("sos:products:joc_cockpit:order:view:configuration", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:order:view:order_log", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:order:view:documentation", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:order:change:start_and_end_node", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change:time_for_adhoc_orders", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change:parameter", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change:run_time", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change:state", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change:hot_folder", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:change", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:start", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:update", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:suspend", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:resume", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:reset", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute:remove_setback", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:execute", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:delete:permanent", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:delete:temporary", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:order:delete", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:view:configuration", "sos:products:controller:workflows:view");
        addMapEntry("sos:products:joc_cockpit:job_chain:view:history", "sos:products:controller:orders:view");
        addMapEntry("sos:products:joc_cockpit:job_chain:view:status", "sos:products:controller:workflows:view");
        addMapEntry("sos:products:joc_cockpit:job_chain:view:documentation", "sos:products:controller:workflows:view");
        addMapEntry("sos:products:joc_cockpit:job_chain:view", "sos:products:controller:workflows:view");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:stop", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:unstop", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:add_order", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:skip_jobchain_node", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:process_jobchain_node", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:execute:stop_jobchain_node", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:change:hot_folder", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain:assign_documentation", "sos:products:controller:orders:modify");
        addMapEntry("sos:products:joc_cockpit:job_chain", "sos:products:controller:workflows");
        addMapEntry("sos:products:joc_cockpit:job:view:history", "sos:products:controller:orders:view");

    }

    public List<String> map(List<String> perms) {
        List<String> mappedPerms = new ArrayList<String>();
        if (permMap == null) {
            initMap();
        }
        boolean excluded;
        for (String perm : perms) {
            if (perm.startsWith("-")) {
                perm = perm.substring(1);
                excluded = true;
            } else {
                excluded = false;
            }
            if (permMap.get(perm) != null) {
                for (String entry : permMap.get(perm)) {
                    if (excluded) {
                        mappedPerms.add("-" + entry);
                    } else {
                        mappedPerms.add(entry);
                    }
                }
            } else {
                if (perm.startsWith("sos:products:joc:") || perm.length() < 17) {
                    mappedPerms.add(perm);
                }
            }
        }
        return mappedPerms;
    }

}
