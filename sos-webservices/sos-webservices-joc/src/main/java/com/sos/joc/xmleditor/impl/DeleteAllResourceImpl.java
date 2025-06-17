package com.sos.joc.xmleditor.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.all.DeleteAll;
import com.sos.joc.model.xmleditor.delete.all.DeleteAllAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.resource.IDeleteAllResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class DeleteAllResourceImpl extends ACommonResourceImpl implements IDeleteAllResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, DeleteAll.class);
            DeleteAll in = Globals.objectMapper.readValue(filterBytes, DeleteAll.class);
            
            Map<Boolean, Set<ObjectType>> supportedTypes = in.getObjectTypes().stream().collect(Collectors.groupingBy(objT -> ObjectType.YADE
                    .equals(objT) || ObjectType.OTHER.equals(objT), Collectors.toSet()));
            
            Set<ObjectType> unSupportedTypes = supportedTypes.get(false);
            
            if (unSupportedTypes != null) {
                throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                        "[%s]unsupported object type(s) for delete all", unSupportedTypes.stream().map(ObjectType::value).collect(Collectors
                                .joining(",")))));
            }
            
            if (supportedTypes.get(true) != null) {
                JOCDefaultResponse jocDefaultResponse = null;
                if (in.getObjectTypes().contains(ObjectType.YADE) && in.getObjectTypes().contains(ObjectType.OTHER)) {
                    jocDefaultResponse = initAndPermissions(null, getJocPermissions(accessToken).map(p -> p.getFileTransfer().getManage()),
                            getJocPermissions(accessToken).map(p -> p.getOthers().getManage()));
                } else if (in.getObjectTypes().contains(ObjectType.YADE)) {
                    jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getFileTransfer().getManage()));
                } else if (in.getObjectTypes().contains(ObjectType.OTHER)) {
                    jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getOthers().getManage()));
                }
                
                if (jocDefaultResponse != null) {
                    return jocDefaultResponse;
                }
                
                for (ObjectType type : supportedTypes.get(true)) {
                    deleteAllMultiple(type);
                }
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(getSuccess()));
        } catch (Exception e) {
            return responseStatusJSError(e);
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
