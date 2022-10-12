package com.sos.joc.notification.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.notification.StoreNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.notification.resource.IStoreNotification;
import com.sos.joc.xmleditor.impl.StoreResourceImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notification")
public class StoreNotificationImpl extends JOCResourceImpl implements IStoreNotification {

    private static final String API_CALL = "./notification/store";
    
    @Override
    public JOCDefaultResponse postStoreNotification(String xAccessToken, byte[] storeNotificationFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, storeNotificationFilter, xAccessToken);
            JsonValidator.validate(storeNotificationFilter, StoreNotificationFilter.class);
            StoreConfiguration in = Globals.objectMapper.readValue(storeNotificationFilter, StoreConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).getNotification().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog dbAuditlog = storeAuditLog(in.getAuditLog(), CategoryType.MONITORING);
            ObjectType notificationType = ObjectType.NOTIFICATION;
            in.setObjectType(notificationType);
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocXmlEditor.parseXml(in.getConfiguration());
            hibernateSession.setAutoCommit(false);
            Globals.beginTransaction(hibernateSession);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(hibernateSession);
            DBItemXmlEditorConfiguration item = null;
            String name = JocXmlEditor.getConfigurationName(notificationType);
            item = dbLayer.getObject(notificationType.name(), name);
            if (item == null) {
                item = StoreResourceImpl.create(hibernateSession, in, name, getAccount(), dbAuditlog.getId());
            } else {
                item = StoreResourceImpl.update(hibernateSession, in, item, name, getAccount(), dbAuditlog.getId());
            }
            Globals.commit(hibernateSession);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(StoreResourceImpl.getSuccess(notificationType, item)));
        } catch (JocException e) {
            Globals.rollback(hibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(hibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
}
