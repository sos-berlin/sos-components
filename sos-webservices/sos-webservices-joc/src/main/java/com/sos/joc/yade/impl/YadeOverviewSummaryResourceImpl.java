package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.yade.JocDBLayerYade;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.yade.TransferFilesOverView;
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
            JsonValidator.validateFailFast(inBytes, TransferFilter.class);
            TransferFilter in = Globals.objectMapper.readValue(inBytes, TransferFilter.class);

            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }
            
            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getView()).collect(
                                Collectors.toSet());
            } else {
                if (getControllerPermissions(controllerId, accessToken).getView()) {
                    allowedControllers = Collections.singleton(controllerId);
                }
            }
            
            Map<String, Set<Folder>> permittedFoldersMap = null;
            if (!folderPermissions.noFolderRestrictionAreSpecified(allowedControllers)) {
                permittedFoldersMap = folderPermissions.getListOfFolders(allowedControllers);
            }
            if (controllerId.isEmpty() && allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            Date from = JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone());
            Date to = JobSchedulerDate.getDateFrom(in.getDateTo(), in.getTimeZone());

            TransferFilesOverView answer = new TransferFilesOverView();
            answer.setSurveyDate(Date.from(Instant.now()));
            TransferFilesSummary files = new TransferFilesSummary();
            JocDBLayerYade dbLayer = new JocDBLayerYade(session);
            files.setSuccessful(dbLayer.getSuccessFulTransfersCount(allowedControllers, from, to, permittedFoldersMap));
            files.setFailed(dbLayer.getFailedTransfersCount(allowedControllers, from, to, permittedFoldersMap));
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
