package com.sos.joc.xmleditor.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.rename.RenameConfiguration;
import com.sos.joc.model.xmleditor.rename.RenameConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.resource.IRenameResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class RenameResourceImpl extends ACommonResourceImpl implements IRenameResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, RenameConfiguration.class);
            RenameConfiguration in = Globals.objectMapper.readValue(filterBytes, RenameConfiguration.class);

            if (in.getObjectType() != null && in.getObjectType().equals(ObjectType.NOTIFICATION)) {
                throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                        "[%s]unsupported object type for rename", in.getObjectType().name())));
            }
            //checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                session.beginTransaction();
                XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

                String name = in.getName().replaceAll("<br>", "");
                DBItemXmlEditorConfiguration item = getObject(dbLayer, in);

                if (item == null) {
                    item = create(session, in, name);

                } else {
                    item = update(session, in, item, name);
                }

                session.commit();
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(getSuccess(item.getId(), item.getModified())));
            }
            return response;
        } catch (Exception e) {
            Globals.rollback(session);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private DBItemXmlEditorConfiguration getObject(XmlEditorDbLayer dbLayer, RenameConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = null;
        if (in.getId() != null && in.getId() > 0) {
            item = dbLayer.getObject(in.getId());
        }
        return item;
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, RenameConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setType(in.getObjectType().name());
        item.setName(name);
        item.setConfigurationDraft(null);
        item.setConfigurationDraftJson(null);
        item.setSchemaLocation(in.getSchemaIdentifier());
        item.setAuditLogId(Long.valueOf(0));// TODO
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
        // item.setAuditLogId(Long.valueOf(0));// TODO
        item.setAccount(getAccount());
        item.setModified(new Date());
        session.update(item);
        return item;
    }

//    private void checkRequiredParameters(final RenameConfiguration in) throws Exception {
//        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
//        checkRequiredParameter("id", in.getId());
//        checkRequiredParameter("name", in.getName());
//        checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
//    }

    private RenameConfigurationAnswer getSuccess(Long id, Date modified) {
        RenameConfigurationAnswer answer = new RenameConfigurationAnswer();
        answer.setId(id);
        answer.setModified(modified);
        return answer;
    }

}
