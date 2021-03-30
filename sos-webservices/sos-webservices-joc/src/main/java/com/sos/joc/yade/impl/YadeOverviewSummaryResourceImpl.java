package com.sos.joc.yade.impl;

import java.util.Date;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.TransferFilesSummary;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.yade.resource.IYadeOverviewSummaryResource;
import com.sos.schema.JsonValidator;

@Path("yade")
public class YadeOverviewSummaryResourceImpl extends JOCResourceImpl implements IYadeOverviewSummaryResource {

    private static final String IMPL_PATH = "./yade/overview/summary";

    @Override
    public JOCDefaultResponse postYadeOverviewSummary(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, FileFilter.class);
            TransferFilter in = Globals.objectMapper.readValue(inBytes, TransferFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            // filter values
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            String dateFrom = in.getDateFrom();
            String dateTo = in.getDateTo();
            String timeZone = in.getTimeZone();
            Date from = null;
            Date to = null;
            if (dateFrom != null && !dateFrom.isEmpty()) {
                from = JobSchedulerDate.getDateFrom(dateFrom, timeZone);
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                to = JobSchedulerDate.getDateTo(dateTo, timeZone);
            }

            TransferFilesSummary answer = new TransferFilesSummary();
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            answer.setSuccessful(dbLayer.getSuccessFulTransfersCount(in.getControllerId(), from, to, permittedFolders));
            answer.setFailed(dbLayer.getFailedTransfersCount(in.getControllerId(), from, to, permittedFolders));
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
