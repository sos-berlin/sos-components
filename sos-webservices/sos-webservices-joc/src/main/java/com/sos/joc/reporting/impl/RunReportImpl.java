package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.reporting.RunReport;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.reporting.Report;
import com.sos.joc.model.reporting.ReportPaths;
import com.sos.joc.reporting.resource.IRunReportResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class RunReportImpl extends JOCResourceImpl implements IRunReportResource {
    
    @Override
    public JOCDefaultResponse runReports(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReportPaths.class);
            ReportPaths in = Globals.objectMapper.readValue(filterBytes, ReportPaths.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getReports().getManage());
            if (response != null) {
                return response;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            InventoryDBLayer dbLayer = new InventoryDBLayer(connection);
            List<String> reportNames = in.getReportPaths().stream().map(JocInventory::pathToName).collect(Collectors.toList());
            List<DBItemInventoryReleasedConfiguration> dbItems = dbLayer.getReleasedConfigurations(reportNames, ConfigurationType.REPORT);
            for (DBItemInventoryReleasedConfiguration dbItem : dbItems) {
                if (!canAdd(dbItem.getFolder(), permittedFolders)) {
                    continue;
                }
                
                Report reportRun = Globals.objectMapper.readValue(dbItem.getContent(), Report.class);
                reportRun.setPath(dbItem.getName()); // TODO use Path after DB table change
                // TODO: map relative monthFrom/To to specific Month from now
                if (reportRun.getMonthFrom() != null && !reportRun.getMonthFrom().matches("\\d{4}-\\d{2}")) {
                    throw new JocNotImplementedException("unsupported relative monthFrom");
                }
                if (reportRun.getMonthTo() != null && !reportRun.getMonthTo().matches("\\d{4}-\\d{2}")) {
                    throw new JocNotImplementedException("unsupported relative monthTo");
                }
                // TODO: event when ready without exception
                RunReport.run(reportRun).thenAccept(e -> ProblemHelper.postExceptionEventIfExist(e, accessToken, getJocError(), null));
            }
            
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    @Override
    public JOCDefaultResponse runReport(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_SINGLE_RUN_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, Report.class);
            Report in = Globals.objectMapper.readValue(filterBytes, Report.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getReports().getManage());
            if (response != null) {
                return response;
            }
            // TODO: event when ready without exception
            RunReport.run(in).thenAccept(e -> ProblemHelper.postExceptionEventIfExist(e, accessToken, getJocError(), null));
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
