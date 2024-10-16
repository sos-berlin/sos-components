package com.sos.joc.classes.tag;

import java.util.Set;

import com.sos.inventory.model.workflow.Jobs;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobTagging;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;

public class JobTags {
    
    public static void update(Jobs jobs, DBItemInventoryConfiguration workflowDbItem, InventoryJobTagDBLayer dbTagLayer) {
        // delete corpses
        
        Set<DBItemInventoryJobTagging> jobTaggings = dbTagLayer.getTaggings(workflowDbItem.getId());
        if (!jobTaggings.isEmpty()) {
            
            if (jobs == null || jobs.getAdditionalProperties() == null) {
                jobs = new Jobs();
            }
            //boolean isChanged = false;
            Set<String> jobNames = jobs.getAdditionalProperties().keySet();
            for (DBItemInventoryJobTagging jobTagging : jobTaggings) {
                if (!jobNames.contains(jobTagging.getJobName())) {
                    //isChanged = true;
                    try {
                        dbTagLayer.getSession().delete(jobTagging);
                    } catch (Exception e) {
                        //
                    }
                }
            }

//            if (isChanged) {
//                EventBus.getInstance().post(new InventoryTagEvent(workflowDbItem.getName()));
//            }
        }
    }

}
