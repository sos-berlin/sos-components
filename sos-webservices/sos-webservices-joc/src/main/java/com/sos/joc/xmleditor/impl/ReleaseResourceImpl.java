package com.sos.joc.xmleditor.impl;

import java.util.Date;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Path(JocXmlEditor.APPLICATION_PATH)
public class ReleaseResourceImpl extends ACommonResourceImpl implements IReleaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseResourceImpl.class);

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReleaseConfiguration.class);
            ReleaseConfiguration in = Globals.objectMapper.readValue(filterBytes, ReleaseConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.MANAGE);
            if (response != null) {
                return response;
            }
            // step 1 - check for vulnerabilities and validate
            java.nio.file.Path schema = JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType());
            try {
                SOSXMLXSDValidator.validate(schema, in.getConfiguration());
            } catch (SOSXMLXSDValidatorException e) {
                LOGGER.error(String.format("[%s]%s", schema, e.toString()), e);
                return JOCDefaultResponse.responseStatus200(ValidateResourceImpl.getError(e));
            }

            // step 2 - update db
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in)));

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

    private ReadStandardConfigurationAnswer handleStandardConfiguration(ReleaseConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = createOrUpdate(in);

        ReadConfigurationHandler handler = new ReadConfigurationHandler(in.getObjectType());
        handler.readCurrent(item, in.getControllerId(), true);
        postEvent(in);
        return handler.getAnswer();
    }

    private void postEvent(ReleaseConfiguration in) {
        EventBus.getInstance().post(new NotificationConfigurationReleased(in.getControllerId()));
    }

    private DBItemXmlEditorConfiguration createOrUpdate(ReleaseConfiguration in) throws Exception {
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
                item.setAuditLogId(Long.valueOf(0));// TODO
                item.setAccount(getAccount());
                item.setCreated(new Date());
                item.setModified(item.getCreated());
                item.setReleased(item.getCreated());
                session.save(item);
            } else {
                item.setConfigurationDraft(null);
                item.setConfigurationDraftJson(null);
                item.setConfigurationReleased(in.getConfiguration());
                item.setConfigurationReleasedJson(Utils.serialize(in.getConfigurationJson()));
                item.setAuditLogId(Long.valueOf(0));// TODO
                item.setAccount(getAccount());
                item.setModified(new Date());
                item.setReleased(item.getModified());
                session.update(item);
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

    private void checkRequiredParameters(final ReleaseConfiguration in) throws Exception {
        if (in.getObjectType() != null && in.getObjectType().equals(ObjectType.OTHER)) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                    "[%s][%s]unsupported object type for release", in.getControllerId(), in.getObjectType().name())));
        }

        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        checkRequiredParameter("configuration", in.getConfiguration());
        checkRequiredParameter("configurationJson", in.getConfigurationJson());
    }

}
