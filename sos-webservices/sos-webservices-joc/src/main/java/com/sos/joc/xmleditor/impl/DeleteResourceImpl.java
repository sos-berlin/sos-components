package com.sos.joc.xmleditor.impl;

import java.util.Arrays;
import java.util.Date;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.DeleteConfiguration;
import com.sos.joc.model.xmleditor.delete.DeleteOtherDraftAnswer;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.resource.IDeleteResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class DeleteResourceImpl extends ACommonResourceImpl implements IDeleteResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceImpl.class);

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeleteConfiguration.class);
            DeleteConfiguration in = Globals.objectMapper.readValue(filterBytes, DeleteConfiguration.class);

            // checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    checkRequiredParameter("id", in.getId());

                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleMultipleConfigurations(in)));
                    break;
                default:
                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in,
                            getAccount(), 0L)));
                    break;
                }
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    // private static void checkRequiredParameters(final DeleteConfiguration in) throws Exception {
    // made by schema JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
    // }

    public static ReadStandardConfigurationAnswer handleStandardConfiguration(DeleteConfiguration in, String account, Long auditLogId)
            throws Exception {
        DBItemXmlEditorConfiguration item = updateStandardItem(in.getObjectType().name(), JocXmlEditor.getConfigurationName(in.getObjectType()), in
                .getRelease() == null ? false : in.getRelease().booleanValue(), account, auditLogId);

        ReadConfigurationHandler handler = new ReadConfigurationHandler(in.getObjectType());
        handler.readCurrent(item, true);
        return handler.getAnswer();
    }

    private static DeleteOtherDraftAnswer handleMultipleConfigurations(DeleteConfiguration in) throws Exception {
        boolean deleted = deleteMultiple(in.getObjectType(), in.getId());
        DeleteOtherDraftAnswer answer = new DeleteOtherDraftAnswer();
        if (deleted) {
            answer.setDeleted(new Date());
        } else {
            answer.setFound(false);
        }
        return answer;
    }

    private static boolean deleteMultiple(ObjectType type, Long id) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            int deleted = dbLayer.deleteMultiple(type, id);
            session.commit();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[id=%s]deleted=%s", id, deleted));
            }
            return Math.abs(deleted) > 0;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static DBItemXmlEditorConfiguration updateStandardItem(String objectType, String name, boolean release, String account, Long auditLogId)
            throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(objectType, name);
            if (item == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[%s][%s]not found", objectType, name));
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
                item.setAuditLogId(auditLogId);
                item.setAccount(account);
                item.setModified(new Date());
                session.update(item);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[%s][%s]%s", objectType, name, SOSString.toString(item, Arrays.asList("configuration"))));
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
