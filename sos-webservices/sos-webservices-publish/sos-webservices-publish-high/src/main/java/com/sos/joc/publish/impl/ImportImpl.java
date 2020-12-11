package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ImportImpl extends JOCResourceImpl implements IImportResource {

    private static final String API_CALL = "./inventory/import";

    @Override
	public JOCDefaultResponse postImportConfiguration(String xAccessToken, 
			FormDataBodyPart body, 
			boolean overwrite,
			String folder,
			String timeSpent,
			String ticketLink,
			String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
        ImportFilter filter = new ImportFilter();
        filter.setAuditLog(auditLog);
        filter.setFolder(folder);
        filter.setOverwrite(overwrite);
		return postImportConfiguration(xAccessToken, body, filter, auditLog);
	}

	private JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body, ImportFilter filter,
			AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken); 
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(filter), ImportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImport());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
            // process uploaded archive
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                configurations = PublishUtils.readZipFileContent(stream);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz") || mediaSubType.contains("gzip")) {
                configurations = PublishUtils.readTarGzipFileContent(stream);
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have one of the formats zip, tar.gz or tgz!", uploadFileName)); 
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            ImportAudit importAudit = new ImportAudit(filter, 
                    String.format("%1$d configuration object(s) imported with profile %2$s", configurations.size(), account));
            logAuditMessage(importAudit);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(importAudit);
            Set<java.nio.file.Path> folders = new HashSet<java.nio.file.Path>();
            if(filter.getFolder() != null && !filter.getFolder().isEmpty()) {
                configurations.stream().forEach(item -> dbLayer.saveOrUpdateInventoryConfiguration(item, account, dbItemAuditLog.getId(), filter.getFolder()));
            } else {
                configurations.stream().forEach(item -> dbLayer.saveOrUpdateInventoryConfiguration(item, account, dbItemAuditLog.getId(), filter.getOverwrite()));
            }
            folders = configurations.stream().map(cfg -> cfg.getPath()).map(path -> Paths.get(path).getParent()).collect(Collectors.toSet());
            dbLayer.createInvConfigurationsDBItemsForFoldersIfNotExists(PublishUtils.updateSetOfPathsWithParents(folders), dbItemAuditLog.getId());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
	}

}
