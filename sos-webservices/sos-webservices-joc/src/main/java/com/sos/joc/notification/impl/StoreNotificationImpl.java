package com.sos.joc.notification.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.notification.StoreNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.notification.resource.IStoreNotification;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
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
            storeNotificationFilter = initLogging(API_CALL, storeNotificationFilter, xAccessToken, CategoryType.MONITORING);
            JsonValidator.validate(storeNotificationFilter, StoreNotificationFilter.class);
            StoreConfiguration in = Globals.objectMapper.readValue(storeNotificationFilter, StoreConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getNotification().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            DBItemJocAuditLog dbAuditlog = storeAuditLog(in.getAuditLog());
            ObjectType notificationType = ObjectType.NOTIFICATION;
            in.setObjectType(notificationType);

            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocXmlEditor.parseXml(in.getConfiguration());
            hibernateSession.setAutoCommit(false);
            Globals.beginTransaction(hibernateSession);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(hibernateSession);
            DBItemXmlEditorConfiguration item = null;
            String name = StandardSchemaHandler.getDefaultConfigurationName(notificationType);
            item = dbLayer.getObject(notificationType.name(), name);
            boolean isChanged = true;
            if (item == null) {
                item = StoreResourceImpl.create(hibernateSession, in, name, getAccount(), dbAuditlog.getId());
            } else {
                String currentConfiguration = SOSString.isEmpty(in.getConfiguration()) ? null : in.getConfiguration();
                isChanged = JocXmlEditor.isChanged(item, currentConfiguration);
                if (isChanged) {
                    item = StoreResourceImpl.update(hibernateSession, in, item, name, getAccount(), dbAuditlog.getId());
                }
            }
            Globals.commit(hibernateSession);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(StoreResourceImpl.getSuccess(notificationType, item, isChanged)));
        } catch (Exception e) {
            Globals.rollback(hibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
