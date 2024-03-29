package com.sos.joc.xmleditor.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.xml.SOSXMLXSDValidator;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.release.ReleaseConfiguration;
import com.sos.joc.xmleditor.common.Utils;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.resource.IReleaseResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ReleaseResourceImpl extends ACommonResourceImpl implements IReleaseResource {
  
    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReleaseConfiguration.class);
            ReleaseConfiguration in = Globals.objectMapper.readValue(filterBytes, ReleaseConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response != null) {
                return response;
            }
            // step 1 - check for vulnerabilities and validate
            java.nio.file.Path schema = JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType());
            try {
                SOSXMLXSDValidator.validate(schema, in.getConfiguration());
            } catch (SOSXMLXSDValidatorException e) {
                return JOCDefaultResponse.responseStatus200(ValidateResourceImpl.getError(e));
            }

            // step 2 - update db
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in, getAccount(), 0L)));

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

    public static ReadStandardConfigurationAnswer handleStandardConfiguration(ReleaseConfiguration in, String account, Long auditLogId)
            throws Exception {
        DBItemXmlEditorConfiguration item = createOrUpdate(in, account, auditLogId);

        ReadConfigurationHandler handler = new ReadConfigurationHandler(in.getObjectType());
        handler.readCurrent(item, true);
        postEvent(in);
        return handler.getAnswer();
    }

    private static void postEvent(ReleaseConfiguration in) {
        EventBus.getInstance().post(new NotificationConfigurationReleased());
    }

    private static DBItemXmlEditorConfiguration createOrUpdate(ReleaseConfiguration in, String account, Long auditLogId) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            String name = JocXmlEditor.getConfigurationName(in.getObjectType());

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(in.getObjectType().name(), name);

            if (item == null) {
                item = new DBItemXmlEditorConfiguration();
                item.setType(in.getObjectType().name());
                item.setName(name);
                item.setConfigurationDraft(null);
                item.setConfigurationDraftJson(null);
                item.setConfigurationReleased(in.getConfiguration());
                item.setConfigurationReleasedJson(Utils.serialize(in.getConfigurationJson()));
                item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(in.getObjectType(), null));
                item.setAuditLogId(auditLogId);
                item.setAccount(account);
                item.setCreated(new Date());
                item.setModified(item.getCreated());
                item.setReleased(item.getCreated());
                session.save(item);
            } else {
                item.setConfigurationDraft(null);
                item.setConfigurationDraftJson(null);
                item.setConfigurationReleased(in.getConfiguration());
                item.setConfigurationReleasedJson(Utils.serialize(in.getConfigurationJson()));
                item.setAuditLogId(auditLogId);
                item.setAccount(account);
                item.setModified(new Date());
                item.setReleased(item.getModified());
                session.update(item);
            }

            Globals.commit(session);
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void checkRequiredParameters(final ReleaseConfiguration in) throws Exception {
        if (in.getObjectType() != null && in.getObjectType().equals(ObjectType.OTHER)) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                    "[%s]unsupported object type for release", in.getObjectType().name())));
        }

//        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
//        checkRequiredParameter("configuration", in.getConfiguration());
        //checkRequiredParameter("configurationJson", in.getConfigurationJson());
    }

}
