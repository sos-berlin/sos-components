package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.yade.FilesFilter;
import com.sos.joc.model.yade.TransferFiles;
import com.sos.joc.yade.common.TransferFileUtils;
import com.sos.joc.yade.resource.IYADEFilesResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("yade")
public class YADEFilesResourceImpl extends JOCResourceImpl implements IYADEFilesResource {

    private static final String IMPL_PATH = "./yade/files";

    @Override
    public JOCDefaultResponse postYADEFiles(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(inBytes, FilesFilter.class);
            FilesFilter in = Globals.objectMapper.readValue(inBytes, FilesFilter.class);

            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            }
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<Long> transferIds = in.getTransferIds();
            in.setTransferIds(null);
            List<DBItemYadeFile> dbFiles = dbLayer.getFilteredTransferFiles(transferIds, in, limit);

            TransferFiles answer = new TransferFiles();
            if (dbFiles != null) {
                answer.setFiles(dbFiles.stream().map(file -> TransferFileUtils.getFile(file)).collect(Collectors.toList()));
            }
            answer.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
