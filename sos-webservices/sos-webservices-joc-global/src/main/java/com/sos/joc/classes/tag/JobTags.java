package com.sos.joc.classes.tag;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.DBItemInventoryJobTagging;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.inventory.InventoryTagEvent;

public class JobTags extends ATagsModifyImpl<DBItemInventoryJobTag> {
    
    public void update(Jobs jobs, DBItemInventoryConfiguration workflowDbItem, InventoryJobTagDBLayer dbTagLayer) throws SOSHibernateException {
        if (jobs == null || jobs.getAdditionalProperties() == null) {
            return;
        }
        Set<String> allTagNames = jobs.getAdditionalProperties().values().stream().map(Job::getJobTags).filter(Objects::nonNull).flatMap(Set::stream)
                .collect(Collectors.toSet());
        List<DBItemInventoryJobTag> oldDBTags = allTagNames.isEmpty() ? Collections.emptyList() : dbTagLayer.getTags(allTagNames);
        Date date = Date.from(Instant.now());
        Set<DBItemInventoryJobTag> newDbTagItems = add(allTagNames, date, dbTagLayer);
        
        newDbTagItems.addAll(oldDBTags);
        Map<String, Long> newTags = newDbTagItems.stream().collect(Collectors.toMap(DBItemInventoryJobTag::getName, DBItemInventoryJobTag::getId));
        
        Set<DBItemInventoryJobTagging> dbTaggings = dbTagLayer.getTaggings(workflowDbItem.getId());
        
        Set<DBItemInventoryJobTagging> jsonTaggings = jobs.getAdditionalProperties().entrySet().stream().filter(entry -> entry.getValue()
                .getJobTags() != null).flatMap(entry -> entry.getValue().getJobTags().stream().map(tagName -> {
                    DBItemInventoryJobTagging item = new DBItemInventoryJobTagging();
                    item.setCid(workflowDbItem.getId());
                    item.setId(null);
                    item.setModified(date);
                    item.setWorkflowName(workflowDbItem.getName());
                    item.setJobName(entry.getKey());
                    item.setTagId(newTags.get(tagName));
                    return item;
                })).collect(Collectors.toSet());
        
        boolean isChanged = false;
        
        //delete obsolete taggings
        if (!dbTaggings.isEmpty()) {
            for (DBItemInventoryJobTagging dbTagging : dbTaggings) {
                if (!jsonTaggings.contains(dbTagging)) {
                    dbTagLayer.getSession().delete(dbTagging);
                    isChanged = true;
                }
            }
        }
        
        //add new taggings
        if (!jsonTaggings.isEmpty()) {
            for (DBItemInventoryJobTagging jsonTagging : jsonTaggings) {
                if (!dbTaggings.contains(jsonTagging)) {
                    dbTagLayer.getSession().save(jsonTagging);
                    isChanged = true;
                }
            }
        }
        
        if (isChanged) {
            EventBus.getInstance().post(new InventoryTagEvent(workflowDbItem.getName()));
        }
    }

}
