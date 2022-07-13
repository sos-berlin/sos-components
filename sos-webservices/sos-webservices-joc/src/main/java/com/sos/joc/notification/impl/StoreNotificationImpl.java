package com.sos.joc.notification.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.notification.StoreNotificationFilter;
import com.sos.joc.model.notification.StoreNotificationResponse;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.notification.resource.IStoreNotification;
import com.sos.joc.xmleditor.common.Utils;
import com.sos.schema.JsonValidator;

@Path("notification")
public class StoreNotificationImpl extends JOCResourceImpl implements IStoreNotification {

    private static final String API_CALL = "./notification/store";
    
    @Override
    public JOCDefaultResponse postStoreNotification(String xAccessToken, byte[] storeNotificationFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, storeNotificationFilter, xAccessToken);
            JsonValidator.validate(storeNotificationFilter, StoreNotificationFilter.class);
            StoreNotificationFilter filter = Globals.objectMapper.readValue(storeNotificationFilter, StoreNotificationFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(filter.getControllerId(), 
                    getJocPermissions(xAccessToken).getNotification().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbAuditlog = storeAuditLog(filter.getAuditLog(), CategoryType.DEPLOYMENT);
            JocXmlEditor.parseXml(filter.getConfiguration());
            hibernateSession.beginTransaction();
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(hibernateSession);
            DBItemXmlEditorConfiguration item = null;
            String name = JocXmlEditor.getConfigurationName(ObjectType.NOTIFICATION);
            item = getStandardObject(dbLayer, filter);
            if (item == null) {
                item = create(hibernateSession, filter, name, dbAuditlog.getId());
            } else {
                item = update(hibernateSession, filter, item, name, dbAuditlog.getId());
            }
            hibernateSession.commit();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getSuccess(item.getName(), item.getModified(),
                    item.getReleased())));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private DBItemXmlEditorConfiguration getStandardObject(XmlEditorDbLayer dbLayer, StoreNotificationFilter filter) throws Exception {
        return dbLayer.getObject(ObjectType.NOTIFICATION.name(), JocXmlEditor.getConfigurationName(ObjectType.NOTIFICATION, filter.getName()));
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, StoreNotificationFilter filter, String name, Long auditLogId)
            throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setType(ObjectType.NOTIFICATION.name());
        item.setName(name.trim());
        item.setConfigurationDraft(filter.getConfiguration());
        if (filter.getConfigurationJson() != null) {
            item.setConfigurationDraftJson(Utils.serialize(filter.getConfigurationJson()));
        }
        item.setSchemaLocation(JocXmlEditor.SCHEMA_FILENAME_NOTIFICATION);
        item.setAuditLogId(auditLogId);
        item.setAccount(getAccount());
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        session.save(item);
        return item;
    }

    private DBItemXmlEditorConfiguration update(SOSHibernateSession session, StoreNotificationFilter filter, DBItemXmlEditorConfiguration item,
            String name, Long auditLogId) throws Exception {
        item.setName(name.trim());
        item.setConfigurationDraft(SOSString.isEmpty(filter.getConfiguration()) ? null : filter.getConfiguration());
        if (filter.getConfigurationJson() != null) {
            item.setConfigurationDraftJson(Utils.serialize(filter.getConfigurationJson()));
        }
        item.setSchemaLocation(JocXmlEditor.SCHEMA_FILENAME_NOTIFICATION);
        item.setAuditLogId(auditLogId);
        item.setAccount(getAccount());
        item.setModified(new Date());
        session.update(item);
        return item;
    }

    private StoreNotificationResponse getSuccess(String name, Date modified, Date deployed) {
        StoreNotificationResponse response = new StoreNotificationResponse();
        response.setName(name);
        response.setObjectType(ObjectType.NOTIFICATION);
        response.setModified(modified);
        response.setReleased(false);
        if (deployed == null) {
            response.setHasReleases(true);
            response.setState(ItemStateEnum.RELEASE_NOT_EXIST);
        } else {
            response.setHasReleases(false);
            response.setState(ItemStateEnum.DRAFT_IS_NEWER);
        }
        return response;
    }
    
}
