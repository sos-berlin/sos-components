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
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.reporting.Report;
import com.sos.joc.model.reporting.RunReports;
import com.sos.joc.reporting.resource.IRunReportResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class RunReportImpl extends JOCResourceImpl implements IRunReportResource {
    
    @Override
    public JOCDefaultResponse runReports(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, RunReports.class);
            RunReports in = Globals.objectMapper.readValue(filterBytes, RunReports.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getReports().getManage()));
            if (response != null) {
                return response;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            storeAuditLog(in.getAuditLog());
            
            InventoryDBLayer dbLayer = new InventoryDBLayer(connection);
            List<String> reportNames = in.getReportPaths().stream().map(JocInventory::pathToName).collect(Collectors.toList());
            List<DBItemInventoryReleasedConfiguration> dbItems = dbLayer.getReleasedConfigurations(reportNames, ConfigurationType.REPORT);
            for (DBItemInventoryReleasedConfiguration dbItem : dbItems) {
                if (!canAdd(dbItem.getFolder(), permittedFolders)) {
                    continue;
                }
                
                Report reportRun = Globals.objectMapper.readValue(dbItem.getContent(), Report.class);
                reportRun.setPath(dbItem.getPath());
                
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
            Report in = Globals.objectMapper.readValue(filterBytes, Report.class);
            
            if (in.getPath() == null) {
                return runReports(accessToken, filterBytes);
            }
            
            filterBytes = initLogging(IMPL_SINGLE_RUN_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, Report.class);
            
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getReports().getManage()));
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
