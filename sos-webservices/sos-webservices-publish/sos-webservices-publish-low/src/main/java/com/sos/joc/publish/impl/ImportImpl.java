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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ImportImpl extends JOCResourceImpl implements IImportResource {

    private static final String API_CALL = "./inventory/import";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportImpl.class);

    @Override
	public JOCDefaultResponse postImportConfiguration(String xAccessToken, 
			FormDataBodyPart body, 
			String format,
			boolean overwrite,
			String targetFolder,
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
        filter.setFormat(ArchiveFormat.fromValue(format));
        filter.setAuditLog(auditLog);
        filter.setTargetFolder(targetFolder);
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
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            String account = Globals.getConfigurationGlobalsJoc().getDefaultProfileAccount().getValue();
            stream = body.getEntityAs(InputStream.class);
            Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
            JocMetaInfo jocMetaInfo = new JocMetaInfo();
            // process uploaded archive
            if (ArchiveFormat.ZIP.equals(filter.getFormat())) {
                configurations = PublishUtils.readZipFileContent(stream, jocMetaInfo);
            } else if (ArchiveFormat.TAR_GZ.equals(filter.getFormat())) {
                configurations = PublishUtils.readTarGzipFileContent(stream, jocMetaInfo);
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have one of the formats zip or tar.gz!", uploadFileName)); 
            }
            if(!PublishUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                // TODO: process transformation rules 
                LOGGER.info(String.format("Imported from JS7 JOC Cockpit version: %1$s", jocMetaInfo.getJocVersion()));
                LOGGER.info(String.format("  with inventory schema version: %1$s", jocMetaInfo.getInventorySchemaVersion()));
                LOGGER.info(String.format("  and API version: %1$s", jocMetaInfo.getApiVersion()));
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            Set<String> agentNames = agentDbLayer.getEnabledAgentNames();
            ImportAudit importAudit = new ImportAudit(filter, 
                    String.format("%1$d configuration object(s) imported with profile %2$s", configurations.size(), account));
            logAuditMessage(importAudit);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(importAudit);
            Set<java.nio.file.Path> folders = new HashSet<java.nio.file.Path>();
            if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                configurations.stream().map(item -> {
                    item.setPath(filter.getTargetFolder() + item.getPath());
                    return item;
                }).forEach(item -> dbLayer.saveOrUpdateInventoryConfiguration(
                        item, account, dbItemAuditLog.getId(), filter.getOverwrite(), filter.getTargetFolder(), agentNames));
            } else {
                configurations.stream().forEach(item -> dbLayer.saveOrUpdateInventoryConfiguration(
                        item, account, dbItemAuditLog.getId(), filter.getOverwrite(), agentNames));
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
