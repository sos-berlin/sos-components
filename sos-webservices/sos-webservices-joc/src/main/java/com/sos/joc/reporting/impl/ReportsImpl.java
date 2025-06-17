package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.audit.CategoryType;
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
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, ReportsFilter.class);
            ReportsFilter in = Globals.objectMapper.readValue(filterBytes, ReportsFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            
            final Set<Folder> permittedFolders = folderPermissions.getPermittedFolders(in.getFolders());
            
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            
            Stream<DBItemInventoryReleasedConfiguration> dbItems = Stream.empty();
            
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
            
            Predicate<DBItemInventoryReleasedConfiguration> isPermitted = item -> canAdd(item.getFolder(), permittedFolders);
            
            connection = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(connection);
            
            if (in.getReportPaths() != null && !in.getReportPaths().isEmpty()) {
                List<String> reportNames = in.getReportPaths().stream().map(JocInventory::pathToName).collect(Collectors.toList());
                dbItems = dbLayer.getReleasedConfigurations(reportNames, ConfigurationType.REPORT).stream().filter(isPermitted);
            } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                // no folder permissions
            } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
                // isPermitted is already made with above folderPermissions.getPermittedFolders(in.getFolders());
                dbItems = dbLayer.getReleasedConfigurationsByFolder(permittedFolders, ConfigurationType.REPORT).stream();
            } else {
                dbItems = dbLayer.getReleasedConfigurations(Collections.emptyList(), ConfigurationType.REPORT).stream().filter(isPermitted);
            }
            
            Reports reports = new Reports();
            reports.setReports(dbItems.map(mapDbItemToReport).filter(Objects::nonNull).collect(Collectors.toList()));
            reports.setDeliveryDate(Date.from(Instant.now()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(reports));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

}
