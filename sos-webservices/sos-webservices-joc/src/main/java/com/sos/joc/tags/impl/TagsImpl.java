package com.sos.joc.tags.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.model.tag.TagsUsedBy;
import com.sos.joc.model.tag.common.RequestFolder;
import com.sos.joc.tags.resource.ITags;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("tags")
public class TagsImpl extends JOCResourceImpl implements ITags {

    private static final String API_CALL = "./tags";
    private static final String API_USEDBY_CALL = "./tags/used";

    @Override
    public JOCDefaultResponse postTags(String accessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            Tags entity = new Tags();
            entity.setTags(new InventoryTagDBLayer(session).getAllTagNames());
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
    
    @Override
    public JOCDefaultResponse postUsedBy(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_USEDBY_CALL, filterBytes, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            JsonValidator.validateFailFast(filterBytes, RequestFolder.class);
            RequestFolder in =  Globals.objectMapper.readValue(filterBytes, RequestFolder.class);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_USEDBY_CALL);
            
            TagsUsedBy entity = new TagsUsedBy();
            //a[0] = workflowPath, a[1] = tagname
            entity.setAdditionalProperties(getUsedBy(in.getFolders(), folderPermissions.getListOfFolders(), new InventoryTagDBLayer(session)));
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

    private static Map<String, List<String>> getUsedBy(Collection<Folder> folders, Set<Folder> permittedFolders, InventoryTagDBLayer dbLayer)
            throws SOSHibernateException {
        return dbLayer.getTagsByFolders(folders, true).stream().filter(i -> folderIsPermitted(i.getFolder(), permittedFolders)).collect(Collectors
                .groupingBy(InventoryTagItem::getNullableName, Collectors.mapping(InventoryTagItem::getPath, Collectors.toList())));
    }
    
}