package com.sos.joc.xmleditor.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.all.DeleteAll;
import com.sos.joc.model.xmleditor.delete.all.DeleteAllAnswer;
import com.sos.joc.xmleditor.resource.IDeleteAllResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class DeleteAllResourceImpl extends ACommonResourceImpl implements IDeleteAllResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeleteAll.class);
            DeleteAll in = Globals.objectMapper.readValue(filterBytes, DeleteAll.class);

            // checkRequiredParameters(in);

            List<ObjectType> permittedObjectTypes = in.getObjectTypes().stream().filter(o -> getPermission(accessToken, o, Role.MANAGE)).distinct()
                    .collect(Collectors.toList());
            JOCDefaultResponse response = initPermissions(null, permittedObjectTypes.size() > 0);
            in.setObjectTypes(permittedObjectTypes);
            if (response == null) {
                ObjectType type = in.getObjectTypes().get(0);
                switch (type) {
                case YADE:
                case OTHER:
                    deleteAllMultiple(type);
                    response = JOCDefaultResponse.responseStatus200(getSuccess());
                    break;
                default:
                    throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                            "[%s]unsupported object type(s) for delete all", in.getObjectTypes().stream().map(ObjectType::value).collect(Collectors
                                    .joining(",")))));
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

    // private void checkRequiredParameters(final DeleteAll in) throws JocException {
    // made by schema: JocXmlEditor.checkRequiredParameter("objectTypes", in.getObjectTypes());
    // }

    private DeleteAllAnswer getSuccess() throws Exception {
        DeleteAllAnswer answer = new DeleteAllAnswer();
        answer.setDeleted(new Date());
        return answer;
    }

    private boolean deleteAllMultiple(ObjectType type) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            int deleted = dbLayer.deleteAllMultiple(type);
            session.commit();

            return Math.abs(deleted) > 0;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
