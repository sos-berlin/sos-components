package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.reporting.Report;
import com.sos.joc.model.reporting.Reports;
import com.sos.joc.model.reporting.ReportsFilter;
import com.sos.joc.reporting.resource.IReportsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class ReportsImpl extends JOCResourceImpl implements IReportsResource {
    
    @Override
    public JOCDefaultResponse reports(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReportsFilter.class);
            ReportsFilter in = Globals.objectMapper.readValue(filterBytes, ReportsFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            
            final Set<Folder> permittedFolders = folderPermissions.getPermittedFolders(in.getFolders());
            
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            
            List<DBItemInventoryReleasedConfiguration> dbItems = Collections.emptyList();
            
            Function<DBItemInventoryReleasedConfiguration, Report> mapDbItemToReport = dbItem -> {
                try {
                    Report report = Globals.objectMapper.readValue(dbItem.getContent(), Report.class);
                    report.setPath(dbItem.getPath());
                    report.setVersion(null);
                    return report;
                } catch (Exception e) {
                    // TODO error handling
                    return null;
                }
            };
            
            connection = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(connection);
            
            if (in.getReportPaths() != null && !in.getReportPaths().isEmpty()) {
                List<String> reportNames = in.getReportPaths().stream().map(JocInventory::pathToName).collect(Collectors.toList());
                dbItems = dbLayer.getReleasedConfigurations(reportNames, ConfigurationType.REPORT);
            } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                // no folder permisions
            } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
                // TODO dbLayer.getReleasedConfigurations(reportNames, ConfigurationType.REPORT); needs folder filter
//                dbFilter.setFolders(permittedFolders);
                dbItems = dbLayer.getReleasedConfigurations(Collections.emptyList(), ConfigurationType.REPORT);
            } else {
                dbItems = dbLayer.getReleasedConfigurations(Collections.emptyList(), ConfigurationType.REPORT);
            }
            
            Reports reports = new Reports();
            reports.setReports(dbItems.stream().filter(item -> canAdd(item.getFolder(), permittedFolders)).map(mapDbItemToReport).filter(
                    Objects::nonNull).collect(Collectors.toList()));
            reports.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(reports));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
