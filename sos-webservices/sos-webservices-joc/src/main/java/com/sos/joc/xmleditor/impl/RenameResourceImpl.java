package com.sos.joc.xmleditor.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.rename.RenameConfiguration;
import com.sos.joc.model.xmleditor.rename.RenameConfigurationAnswer;
import com.sos.joc.xmleditor.resource.IRenameResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class RenameResourceImpl extends JOCResourceImpl implements IRenameResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JsonValidator.validateFailFast(filterBytes, RenameConfiguration.class);
            RenameConfiguration in = Globals.objectMapper.readValue(filterBytes, RenameConfiguration.class);

            if (in.getObjectType() != null && !in.getObjectType().equals(ObjectType.OTHER)) {
                throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                        "[%s][%s]unsupported object type for rename", in.getJobschedulerId(), in.getObjectType().name())));
            }
            checkRequiredParameters(in);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                session.beginTransaction();
                DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

                String name = in.getName().replaceAll("<br>", "");
                DBItemXmlEditorConfiguration item = getOthersObject(dbLayer, in, name);

                if (item == null) {
                    item = create(session, in, name);

                } else {
                    item = update(session, in, item, name);
                }

                session.commit();
                response = JOCDefaultResponse.responseStatus200(getSuccess(item.getId(), item.getModified()));
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

    private DBItemXmlEditorConfiguration getOthersObject(DbLayerXmlEditor dbLayer, RenameConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = null;
        if (in.getId() != null && in.getId() > 0) {
            item = dbLayer.getObject(in.getId().longValue());
            if (item != null && !item.getObjectType().equals(ObjectType.OTHER.name())) {
                item = null; // dbLayer.getObject(in.getJobschedulerId(), ObjectType.OTHER.name(), name);
            }
        }
        return item;
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, RenameConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setSchedulerId(in.getJobschedulerId());
        item.setObjectType(in.getObjectType().name());
        item.setName(name);
        item.setConfigurationDraft(null);
        item.setConfigurationDraftJson(null);
        item.setSchemaLocation(in.getSchemaIdentifier());
        item.setAuditLogId(new Long(0));// TODO
        item.setAccount(getAccount());
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        session.save(item);
        return item;
    }

    private DBItemXmlEditorConfiguration update(SOSHibernateSession session, RenameConfiguration in, DBItemXmlEditorConfiguration item, String name)
            throws Exception {
        item.setName(name);
        item.setSchemaLocation(in.getSchemaIdentifier());
        // item.setAuditLogId(new Long(0));// TODO
        item.setAccount(getAccount());
        item.setModified(new Date());
        session.update(item);
        return item;
    }

    private void checkRequiredParameters(final RenameConfiguration in) throws Exception {
        checkRequiredParameter("jobschedulerId", in.getJobschedulerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        if (in.getObjectType().equals(ObjectType.OTHER)) {
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RenameConfiguration in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit(in.getJobschedulerId(), accessToken);
        boolean permission = permissions.getJS7Controller().getAdministration().isEditPermissions();
        return init(IMPL_PATH, in, accessToken, in.getJobschedulerId(), permission);
    }

    private RenameConfigurationAnswer getSuccess(Long id, Date modified) {
        RenameConfigurationAnswer answer = new RenameConfigurationAnswer();
        answer.setId(id.intValue());
        answer.setModified(modified);
        return answer;
    }

}
