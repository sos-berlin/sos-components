package com.sos.joc.notification.impl;

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.notification.DeleteNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.notification.resource.IDeleteNotification;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.schema.JsonValidator;

@Path("notification")
public class DeleteNotificationImpl extends JOCResourceImpl implements IDeleteNotification {
    
    private static final String API_CALL = "./notification/delete";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteNotificationImpl.class);
    
    @Override
    public JOCDefaultResponse postDeleteNotification(String xAccessToken, byte[] deleteNotificationFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, deleteNotificationFilter, xAccessToken);
            JsonValidator.validate(deleteNotificationFilter, DeleteNotificationFilter.class);
            DeleteNotificationFilter filter = Globals.objectMapper.readValue(deleteNotificationFilter, DeleteNotificationFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(),
                    getJocPermissions(xAccessToken).getNotification().getManage());
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private ReadStandardConfigurationAnswer handleStandardConfiguration(DeleteNotificationFilter filter) throws Exception {
        DBItemXmlEditorConfiguration item = updateStandardItem(filter.getControllerId(), JocXmlEditor.getConfigurationName(ObjectType.NOTIFICATION), 
                filter.getRelease() == null ? false : filter.getRelease().booleanValue());
        ReadConfigurationHandler handler = new ReadConfigurationHandler(ObjectType.NOTIFICATION);
        handler.readCurrent(item, filter.getControllerId(), true);
        return handler.getAnswer();
    }

    private DBItemXmlEditorConfiguration updateStandardItem(String controllerId, String name, boolean release) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);
            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(ObjectType.NOTIFICATION.name(), name);
            if (item == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[%s][%s][%s]not found", controllerId, ObjectType.NOTIFICATION.name(), name));
                }
            } else {
                if (release) {
                    item.setConfigurationReleased(null);
                    item.setConfigurationReleasedJson(null);
                    item.setReleased(null);
                } else {
                    item.setConfigurationDraft(null);
                    item.setConfigurationDraftJson(null);
                }
                item.setAccount(getAccount());
                item.setModified(new Date());
                session.update(item);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[%s][%s][%s]%s", controllerId, ObjectType.NOTIFICATION.name(), name, 
                            SOSString.toString(item, Arrays.asList("configuration"))));
                }
            }
            session.commit();
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
