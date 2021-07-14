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
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationRemoved;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.DeleteConfiguration;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.remove.RemoveConfiguration;
import com.sos.joc.model.xmleditor.remove.RemoveOtherAnswer;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.resource.IRemoveResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class RemoveResourceImpl extends ACommonResourceImpl implements IRemoveResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceImpl.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeleteConfiguration.class);
            RemoveConfiguration in = Globals.objectMapper.readValue(filterBytes, RemoveConfiguration.class);

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

    private void checkRequiredParameters(final RemoveConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
    }

    private ReadStandardConfigurationAnswer handleStandardConfiguration(RemoveConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = updateStandardItem(in.getObjectType().name(), JocXmlEditor.getConfigurationName(in.getObjectType()), in
                .getRelease() == null ? false : in.getRelease().booleanValue());

        ReadConfigurationHandler handler = new ReadConfigurationHandler(in.getObjectType());
        handler.readCurrent(item, in.getControllerId(), true);
        postEvent(in);
        return handler.getAnswer();
    }

    private void postEvent(RemoveConfiguration in) {
        EventBus.getInstance().post(new NotificationConfigurationRemoved(in.getControllerId()));
    }

    private RemoveOtherAnswer handleMultipleConfigurations(RemoveConfiguration in) throws Exception {
        boolean removed = removeMultiple(in.getObjectType(), in.getId());
        RemoveOtherAnswer answer = new RemoveOtherAnswer();
        if (removed) {
            answer.setRemoved(new Date());
        } else {
            answer.setFound(false);
        }
        return answer;
    }

    private boolean removeMultiple(ObjectType type, Integer id) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            int deleted = dbLayer.deleteMultiple(type, id.longValue());
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

    private DBItemXmlEditorConfiguration updateStandardItem(String objectType, String name, boolean release) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(objectType, name);
            if (item == null) {
                if (isTraceEnabled) {
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
                item.setAccount(getAccount());
                item.setModified(new Date());
                session.update(item);

                if (isTraceEnabled) {
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
