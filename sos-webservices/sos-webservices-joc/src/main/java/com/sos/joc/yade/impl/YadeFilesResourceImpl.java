package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.FilesFilter;
import com.sos.joc.model.yade.TransferFiles;
import com.sos.joc.yade.common.TransferFileUtils;
import com.sos.joc.yade.resource.IYadeFilesResource;
import com.sos.schema.JsonValidator;

@Path("yade")
public class YadeFilesResourceImpl extends JOCResourceImpl implements IYadeFilesResource {

    private static final String IMPL_PATH = "./yade/files";

    @Override
    public JOCDefaultResponse postYadeFiles(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, FilesFilter.class);
            FilesFilter in = Globals.objectMapper.readValue(inBytes, FilesFilter.class);

            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            }
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<DBItemYadeFile> dbFiles = dbLayer.getFilteredTransferFiles(in, limit);
            boolean compact = in.getCompact() == Boolean.TRUE;

            TransferFiles answer = new TransferFiles();
            if (dbFiles != null) {
                answer.setFiles(dbFiles.stream().map(file -> TransferFileUtils.getFile(file, compact)).collect(Collectors.toList()));
            }
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
