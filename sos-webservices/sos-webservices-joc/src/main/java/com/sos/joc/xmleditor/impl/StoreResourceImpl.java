package com.sos.joc.xmleditor.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.model.xmleditor.store.StoreConfigurationAnswer;
import com.sos.joc.xmleditor.resource.IStoreResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class StoreResourceImpl extends JOCResourceImpl implements IStoreResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, StoreConfiguration.class);
            StoreConfiguration in = Globals.objectMapper.readValue(filterBytes, StoreConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                JocXmlEditor.parseXml(in.getConfiguration());

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                session.beginTransaction();
                DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

                DBItemXmlEditorConfiguration item = null;
                String name = null;
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    name = in.getName();
                    item = getObject(dbLayer, in, name);
                    break;
                default:
                    name = JocXmlEditor.getConfigurationName(in.getObjectType());
                    item = getStandardObject(dbLayer, in);
                    break;
                }

                if (item == null) {
                    item = create(session, in, name);

                } else {
                    item = update(session, in, item, name);
                }

                session.commit();
                response = JOCDefaultResponse.responseStatus200(getSuccess(in.getObjectType(), item.getId(), item.getModified(), item.getDeployed()));
            }
            return response;
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

    private DBItemXmlEditorConfiguration getObject(DbLayerXmlEditor dbLayer, StoreConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = null;
        if (in.getId() != null && in.getId() > 0) {
            item = dbLayer.getObject(in.getId().longValue());
        }
        return item;
    }

    private DBItemXmlEditorConfiguration getStandardObject(DbLayerXmlEditor dbLayer, StoreConfiguration in) throws Exception {
        return dbLayer.getObject(in.getControllerId(), in.getObjectType().name(), JocXmlEditor.getConfigurationName(in.getObjectType(), in
                .getName()));
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, StoreConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setControllerId(in.getControllerId());
        item.setType(in.getObjectType().name());
        item.setName(name.trim());
        item.setConfigurationDraft(in.getConfiguration());
        item.setConfigurationDraftJson(in.getConfigurationJson());
        if (in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            item.setSchemaLocation(JocXmlEditor.getStandardRelativeSchemaLocation(in.getObjectType()));
        } else {
            item.setSchemaLocation(in.getSchemaIdentifier());
        }
        item.setAuditLogId(new Long(0));// TODO
        item.setAccount(getAccount());
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        session.save(item);
        return item;
    }

    private DBItemXmlEditorConfiguration update(SOSHibernateSession session, StoreConfiguration in, DBItemXmlEditorConfiguration item, String name)
            throws Exception {
        item.setName(name.trim());
        item.setConfigurationDraft(SOSString.isEmpty(in.getConfiguration()) ? null : in.getConfiguration());
        item.setConfigurationDraftJson(in.getConfigurationJson());
        if (in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            item.setSchemaLocation(JocXmlEditor.getStandardRelativeSchemaLocation(in.getObjectType()));
        } else {
            item.setSchemaLocation(in.getSchemaIdentifier());
        }
        // item.setAuditLogId(new Long(0));// TODO
        item.setAccount(getAccount());
        item.setModified(new Date());
        session.update(item);
        return item;
    }

    private void checkRequiredParameters(final StoreConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        checkRequiredParameter("configuration", in.getConfiguration());
        checkRequiredParameter("configurationJson", in.getConfigurationJson());
        if (!in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final StoreConfiguration in) throws Exception {
        // TODO premission - maybe getJocPermissions(accessToken)
        ControllerPermissions permissions = getControllerPermissions(in.getControllerId(), accessToken);
        boolean permission = true; //permissions.getJS7Controller().getAdministration().isEditPermissions();
        return initPermissions(in.getControllerId(), permission);
    }

    private StoreConfigurationAnswer getSuccess(ObjectType type, Long id, Date modified, Date deployed) {
        StoreConfigurationAnswer answer = new StoreConfigurationAnswer();
        answer.setId(id.intValue());
        answer.setModified(modified);
        if (type.equals(ObjectType.NOTIFICATION)) {
            answer.setMessage(new AnswerMessage());
            if (deployed == null) {
                answer.getMessage().setCode(JocXmlEditor.MESSAGE_CODE_LIVE_NOT_EXIST);
                answer.getMessage().setMessage(JocXmlEditor.MESSAGE_LIVE_NOT_EXIST);
            } else {
                answer.getMessage().setCode(JocXmlEditor.MESSAGE_CODE_DRAFT_IS_NEWER);
                answer.getMessage().setMessage(JocXmlEditor.MESSAGE_DRAFT_IS_NEWER);
            }
        }
        return answer;
    }

}
