package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.TransferFile200;
import com.sos.joc.yade.common.TransferFileUtils;
import com.sos.joc.yade.resource.IYadeFileResource;
import com.sos.schema.JsonValidator;

@Path("yade")
public class YadeFileResourceImpl extends JOCResourceImpl implements IYadeFileResource {

    private static final String IMPL_PATH = "./yade/file";

    @Override
    public JOCDefaultResponse postYadeFile(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, FileFilter.class);
            FileFilter in = Globals.objectMapper.readValue(inBytes, FileFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBItemYadeFile file = session.get(DBItemYadeFile.class, in.getFileId());
            if (file == null) {
                throw new DBMissingDataException(String.format("File with id = %1$s not found!", in.getFileId()));
            }
            TransferFile200 answer = new TransferFile200();
            answer.setFile(TransferFileUtils.getFile(file));
            answer.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}
