package com.sos.joc.classes.tag;

import java.util.Set;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobTagging;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;

public class JobTags {
    
    public static void update(Jobs jobs, DBItemInventoryConfiguration workflowDbItem, InventoryJobTagDBLayer dbTagLayer) throws SOSHibernateException {
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
                    dbTagLayer.getSession().delete(jobTagging);
                }
            }

//            if (isChanged) {
//                EventBus.getInstance().post(new InventoryTagEvent(workflowDbItem.getName()));
//            }
        }
    }

}
