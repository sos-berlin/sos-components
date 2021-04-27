package com.sos.joc.xmleditor.impl;

import java.util.Date;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.all.DeleteAll;
import com.sos.joc.model.xmleditor.delete.all.DeleteAllAnswer;
import com.sos.joc.xmleditor.resource.IDeleteAllResource;
import com.sos.schema.JsonValidator;


@Path(JocXmlEditor.APPLICATION_PATH)
public class DeleteAllResourceImpl extends ACommonResourceImpl implements IDeleteAllResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllResourceImpl.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeleteAll.class);
            DeleteAll in = Globals.objectMapper.readValue(filterBytes, DeleteAll.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectTypes().get(0), Role.MANAGE);
            if (response == null) {
                ObjectType type = in.getObjectTypes().get(0);
                switch (type) {
                case YADE:
                case OTHER:
                    delete(type, in.getControllerId());
                    response = JOCDefaultResponse.responseStatus200(getSuccess());
                    break;
                default:
                    throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                            "[%s][%s]unsupported object type(s) for delete all", in.getControllerId(), in.getObjectTypes().stream().map(ObjectType::value).collect(Collectors.joining(",")))));
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

    private void checkRequiredParameters(final DeleteAll in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectTypes", in.getObjectTypes());
    }

    private DeleteAllAnswer getSuccess() throws Exception {
        DeleteAllAnswer answer = new DeleteAllAnswer();
        answer.setDeleted(new Date());
        return answer;
    }

    private boolean delete(ObjectType type, String controllerId) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            int deleted = dbLayer.deleteAll(type, controllerId);
            session.commit();
            if (isTraceEnabled) {
                LOGGER.trace(String.format("deleted=%s", deleted));
            }
            return Math.abs(deleted) > 0;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
