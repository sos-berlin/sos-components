package com.sos.joc.tags.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.DBItemInventoryTagging;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.model.tag.tagging.RequestFilter;
import com.sos.joc.tags.resource.ITagging;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import jakarta.ws.rs.Path;

@Path("inventory")
public class TaggingImpl extends JOCResourceImpl implements ITagging {


    @Override
    public JOCDefaultResponse postTagging(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            RequestFilter in =  initRequest(PATH_TAGS, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(in.getAuditLog(), CategoryType.INVENTORY);
            
            session = Globals.createSosHibernateStatelessConnection(PATH_TAGGING);
            session.setAutoCommit(false);
            session.beginTransaction();
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
            
            DBItemInventoryConfiguration config = getConfiguration(in.getPath(), new InventoryDBLayer(session));
            
            Set<String> tags = in.getTags() == null ? Collections.emptySet() : in.getTags();
            List<DBItemInventoryTag> dbTags = dbTagLayer.getTags(tags);
            Date date = Date.from(Instant.now());
            Set<DBItemInventoryTag> newDbTagItems = TagsModifyImpl.add(tags, date, dbTagLayer);
            boolean sendTagsEvent = !newDbTagItems.isEmpty();
            newDbTagItems.addAll(dbTags);
            Set<Long> newTagIds = newDbTagItems.stream().map(DBItemInventoryTag::getId).collect(Collectors.toSet());
            
            List<DBItemInventoryTagging> dbTaggings = dbTagLayer.getTaggings(config.getId());
            Set<Long> tagIdsInTaggings = dbTaggings.stream().map(DBItemInventoryTagging::getTagId).collect(Collectors.toSet());
            List<String> tagEvents = new ArrayList<>();
            
            //delete taggings that not inside request
            dbTaggings.stream().filter(i -> !newTagIds.contains(i.getTagId())).forEach(i -> {
                try {
                    dbTagLayer.getSession().delete(i);
                    tagEvents.add(i.getName());
                } catch (SOSHibernateException e) {
                    throw new DBInvalidDataException(e);
                }
            });
            
            //add new taggings
            newDbTagItems.stream().filter(i -> !tagIdsInTaggings.contains(i.getId())).map(i -> {
                DBItemInventoryTagging item = new DBItemInventoryTagging();
                item.setCid(config.getId());
                item.setId(null);
                item.setModified(date);
                item.setName(config.getName());
                item.setTagId(i.getId());
                item.setType(config.getType());
                return item;
            }).forEach(i -> {
                try {
                    dbTagLayer.getSession().save(i);
                    tagEvents.add(i.getName());
                } catch (SOSHibernateException e) {
                    throw new DBInvalidDataException(e);
                }
            });
            
            Globals.commit(session);
            
            if (sendTagsEvent) {
                JocInventory.postTagsEvent();
            }
            tagEvents.forEach(tag -> JocInventory.postTagEvent(tag));
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    @Override
    public JOCDefaultResponse postUsed(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            RequestFilter in =  initRequest(PATH_TAGS, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(PATH_TAGS);
            DBItemInventoryConfiguration config = getConfiguration(in.getPath(), new InventoryDBLayer(session));
            
            Tags entity = new Tags();
            entity.setTags(new InventoryTagDBLayer(session).getTags(config.getId()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private RequestFilter initRequest(String apiCall, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(apiCall, filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, RequestFilter.class);
        return Globals.objectMapper.readValue(filterBytes, RequestFilter.class);
    }
    
    private DBItemInventoryConfiguration getConfiguration(String path, InventoryDBLayer dbLayer) throws Exception {
        com.sos.joc.model.inventory.common.RequestFilter filter = new com.sos.joc.model.inventory.common.RequestFilter();
        filter.setObjectType(ConfigurationType.WORKFLOW);
        filter.setPath(path);
        return JocInventory.getConfiguration(dbLayer, filter, folderPermissions);
    }
    
}