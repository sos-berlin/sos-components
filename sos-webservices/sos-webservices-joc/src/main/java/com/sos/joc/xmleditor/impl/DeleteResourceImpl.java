package com.sos.joc.xmleditor.impl;

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.DeleteDraft;
import com.sos.joc.model.xmleditor.delete.DeleteOtherDraftAnswer;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.resource.IDeleteResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class DeleteResourceImpl extends ACommonResourceImpl implements IDeleteResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceImpl.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeleteDraft.class);
            DeleteDraft in = Globals.objectMapper.readValue(filterBytes, DeleteDraft.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    checkRequiredParameter("id", in.getId());

                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleMultipleConfigurations(in)));
                    break;
                default:
                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in)));
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

    private void checkRequiredParameters(final DeleteDraft in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
    }

    private ReadStandardConfigurationAnswer handleStandardConfiguration(DeleteDraft in) throws Exception {
        DBItemXmlEditorConfiguration item = updateStandardItem(in.getControllerId(), in.getObjectType().name(), JocXmlEditor.getConfigurationName(in
                .getObjectType()));

        ReadConfigurationHandler handler = new ReadConfigurationHandler(this, in.getObjectType());
        handler.readLive(item, in.getControllerId());
        return handler.getAnswer();
    }

    private DeleteOtherDraftAnswer handleMultipleConfigurations(DeleteDraft in) throws Exception {
        boolean deleted = delete(in.getObjectType(), in.getId());
        DeleteOtherDraftAnswer answer = new DeleteOtherDraftAnswer();
        if (deleted) {
            answer.setDeleted(new Date());
        } else {
            answer.setFound(false);
        }
        return answer;
    }

    private boolean delete(ObjectType type, Integer id) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            int deleted = dbLayer.delete(type, id.longValue());
            session.commit();
            if (isTraceEnabled) {
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

    private DBItemXmlEditorConfiguration updateStandardItem(String controllerId, String objectType, String name) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(controllerId, objectType, name);
            if (item == null) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s][%s]not found", controllerId, objectType, name));
                }
            } else {
                item.setConfigurationDraft(null);
                item.setAccount(getAccount());
                item.setModified(new Date());
                session.update(item);

                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s][%s]%s", controllerId, objectType, name, SOSString.toString(item, Arrays.asList(
                            "configuration"))));
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
