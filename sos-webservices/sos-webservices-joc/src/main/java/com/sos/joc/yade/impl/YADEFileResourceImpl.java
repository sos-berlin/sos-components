package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.TransferFile200;
import com.sos.joc.yade.common.TransferFileUtils;
import com.sos.joc.yade.resource.IYADEFileResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("yade")
public class YADEFileResourceImpl extends JOCResourceImpl implements IYADEFileResource {

    private static final String IMPL_PATH = "./yade/file";

    @Override
    public JOCDefaultResponse postYADEFile(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(inBytes, FileFilter.class);
            FileFilter in = Globals.objectMapper.readValue(inBytes, FileFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getFileTransfer().getView());
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
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
