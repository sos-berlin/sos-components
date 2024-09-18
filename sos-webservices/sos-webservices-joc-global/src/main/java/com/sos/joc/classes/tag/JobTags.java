package com.sos.joc.classes.tag;

import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.exceptions.DBInvalidDataException;

public class JobTags extends ATagsModifyImpl<DBItemInventoryJobTag> {
    
    public void update(Jobs jobs, DBItemInventoryConfiguration workflowDbItem, InventoryJobTagDBLayer dbTagLayer) throws SOSHibernateException {
        if (jobs == null || jobs.getAdditionalProperties() == null) {
            return;
        }
        List<InventoryTagEvent> tagEvents = new ArrayList<>();

        Set<String> allTagNames = jobs.getAdditionalProperties().values().stream().map(Job::getJobTags).filter(Objects::nonNull).flatMap(Set::stream)
                .collect(Collectors.toSet());
        List<DBItemInventoryJobTag> dbTags = allTagNames.isEmpty() ? Collections.emptyList() : dbTagLayer.getTags(allTagNames);
        Date date = Date.from(Instant.now());
        Set<DBItemInventoryJobTag> newDbTagItems = add(allTagNames, date, dbTagLayer);
        //tagEvents.addAll(newDbTagItems.stream().map(name -> new InventoryTagAddEvent(name.getName())).collect(Collectors.toList()));
        
        newDbTagItems.addAll(dbTags);
        Map<String, Long> newTags = newDbTagItems.stream().collect(Collectors.toMap(DBItemInventoryJobTag::getName, DBItemInventoryJobTag::getId));
        
        List<DBItemInventoryJobTagging> dbTaggings = dbTagLayer.getTaggings(workflowDbItem.getId());
        
        //delete obsolete taggings
        dbTaggings.stream().filter(i -> !newTags.values().contains(i.getTagId())).forEach(i -> {
            try {
                dbTagLayer.getSession().delete(i);
                tagEvents.add(new InventoryTagEvent(i.getWorkflowName()));
            } catch (SOSHibernateException e) {
                throw new DBInvalidDataException(e);
            }
        });
        
       //add new taggings
       jobs.getAdditionalProperties().entrySet().stream().filter(entry -> entry.getValue().getJobTags() != null).flatMap(entry -> entry.getValue()
               .getJobTags().stream().map(tagName -> {
                   DBItemInventoryJobTagging item = new DBItemInventoryJobTagging();
                   item.setCid(workflowDbItem.getId());
                   item.setId(null);
                   item.setModified(date);
                   item.setWorkflowName(workflowDbItem.getName());
                   item.setJobName(entry.getKey());
                   item.setTagId(newTags.get(tagName));
                   return item;
               })).filter(i -> !dbTaggings.contains(i)).forEach(i -> {
                   try {
                       dbTagLayer.getSession().save(i);
                       tagEvents.add(new InventoryTagEvent(i.getWorkflowName()));
                   } catch (SOSHibernateException e) {
                       throw new DBInvalidDataException(e);
                   }
               });
       
       tagEvents.forEach(evt -> EventBus.getInstance().post(evt));
    }

}
