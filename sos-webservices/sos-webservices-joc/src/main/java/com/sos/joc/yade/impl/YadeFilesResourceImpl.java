package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.FilesFilter;
import com.sos.joc.model.yade.TransferFile;
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
            JsonValidator.validate(inBytes, FileFilter.class);
            FilesFilter in = Globals.objectMapper.readValue(inBytes, FilesFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermissonsJocCockpit(in.getControllerId(), accessToken).getYADE()
                    .getView().isFiles());
            if (response != null) {
                return response;
            }

            if (in.getControllerId() == null) {
                in.setControllerId("");
            }
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            Integer limit = in.getLimit();
            if (limit == null) {
                limit = 10000; // default
            } else if (limit == -1) {
                limit = null; // unlimited
            }
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            List<DBItemYadeFile> dbFiles = dbLayer.getFilteredTransferFiles(in.getTransferIds(), in.getStates(), in.getSourceFiles(), in
                    .getTargetFiles(), in.getInterventionTransferIds(), limit);

            List<TransferFile> files = new ArrayList<TransferFile>();
            for (DBItemYadeFile file : dbFiles) {
                files.add(TransferFileUtils.getFile(file));
            }

            TransferFiles answer = new TransferFiles();
            answer.setFiles(files);
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
