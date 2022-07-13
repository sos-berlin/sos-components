package com.sos.joc.notification.impl;

import java.util.Arrays;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXMLXSDValidator;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.notification.ReadNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.validate.ValidateConfigurationAnswer;
import com.sos.joc.notification.resource.IReadNotification;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.impl.ValidateResourceImpl;
import com.sos.schema.JsonValidator;

@Path("notification")
public class ReadNotificationImpl extends JOCResourceImpl implements IReadNotification {

    private static final String API_CALL = "./notification";
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreNotificationImpl.class);
    
    @Override
    public JOCDefaultResponse postReadNotification(String xAccessToken, byte[] readNotificationFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, readNotificationFilter, xAccessToken);
            JsonValidator.validate(readNotificationFilter, ReadNotificationFilter.class);
            ReadNotificationFilter filter = Globals.objectMapper.readValue(readNotificationFilter, ReadNotificationFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(filter.getControllerId(),
                    getJocPermissions(xAccessToken).getNotification().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            JocXmlEditor.setRealPath();
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
    
    private ReadStandardConfigurationAnswer handleStandardConfiguration(ReadNotificationFilter filter) throws Exception {
        DBItemXmlEditorConfiguration item = getItem(JocXmlEditor.getConfigurationName(ObjectType.NOTIFICATION));
        ReadConfigurationHandler handler = new ReadConfigurationHandler(ObjectType.NOTIFICATION);
        handler.readCurrent(item, filter.getControllerId(), (filter.getForceRelease() != null && filter.getForceRelease()));
        ReadStandardConfigurationAnswer answer = handler.getAnswer();
        if (answer.getConfiguration() != null) {
            answer.setValidation(getValidation(JocXmlEditor.getStandardAbsoluteSchemaLocation(ObjectType.NOTIFICATION), answer.getConfiguration()));
        }
        return answer;
    }
    
    private DBItemXmlEditorConfiguration getItem(String name) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);
            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(ObjectType.NOTIFICATION.name(), name);
            session.commit();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][%s]%s", ObjectType.NOTIFICATION.name(), name,
                        SOSString.toString(item, Arrays.asList("configuration"))));
            }
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private ValidateConfigurationAnswer getValidation(java.nio.file.Path schema, String content) throws Exception {
        ValidateConfigurationAnswer validation = null;
        try {
            SOSXMLXSDValidator.validate(schema, content);
            validation = ValidateResourceImpl.getSuccess();
        } catch (SOSXMLXSDValidatorException e) {
            validation = ValidateResourceImpl.getError(e);
        }
        return validation;
    }
    
}
