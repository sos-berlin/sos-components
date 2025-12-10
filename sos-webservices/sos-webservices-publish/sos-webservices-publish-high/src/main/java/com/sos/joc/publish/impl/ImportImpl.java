package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocImportException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.inventory.impl.RevalidateResourceImpl;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.validate.Report;
import com.sos.joc.model.inventory.validate.ReportItem;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.ArchiveValues;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.ImportUtils;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory")
public class ImportImpl extends JOCResourceImpl implements IImportResource {

    private static final String API_CALL = "./inventory/import";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportImpl.class);

    @Override
	public JOCDefaultResponse postImportConfiguration(String xAccessToken, 
			FormDataBodyPart body, 
            String format,
			boolean overwrite,
			Boolean overwriteTags,
            String targetFolder,
			String prefix, 
			String suffix,
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
        filter.setTargetFolder(!"/".equals(targetFolder) ? targetFolder : null);
        filter.setOverwrite(overwrite);
        filter.setOverwriteTags(Boolean.TRUE.equals(overwriteTags));
        filter.setPrefix(prefix);
        filter.setSuffix(suffix);
        filter.setFormat(ArchiveFormat.fromValue(format));
        filter.setFilename(PublishUtils.getImportFilename(body));
		return postImportConfiguration(xAccessToken, body, filter, auditLog);
	}

	private JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body, ImportFilter filter,
			AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            byte[] fakeRequest = Globals.objectMapper.writeValueAsBytes(filter);
            initLogging(API_CALL, fakeRequest, xAccessToken, CategoryType.INVENTORY); 
            JsonValidator.validate(fakeRequest, ImportFilter.class);
            //4-eyes principle cannot support uploads
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getInventory().getManage(), false);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            if (filter.getOverwrite() && (filter.getSuffix() != null || filter.getPrefix() != null)) {
            	throw new JocImportException("conflicting arguments: overwrite=true - no prefix/suffix allowed!");
            }
            if (filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                SOSCheckJavaVariableName.testFolder("target folder", filter.getTargetFolder());
            }
            
            DBItemJocAuditLog dbAuditItem = storeAuditLog(filter.getAuditLog());
            Long auditLogId = dbAuditItem.getId();
            
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            stream = body.getEntityAs(InputStream.class);
            ArchiveValues values = null;
            Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
            JocMetaInfo jocMetaInfo = new JocMetaInfo();
            // process uploaded archive
            if (ArchiveFormat.ZIP.equals(filter.getFormat())) {
                values = ImportUtils.readZipFileContent(stream, jocMetaInfo);
                configurations = values.getConfigurations();
                
            } else if (ArchiveFormat.TAR_GZ.equals(filter.getFormat())) {
                values = ImportUtils.readTarGzipFileContent(stream, jocMetaInfo);
                configurations = values.getConfigurations();
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have one of the formats zip or tar.gz!", uploadFileName)); 
            }
            if(!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                // TODO: process transformation rules 
                LOGGER.info(String.format("Imported from JS7 JOC Cockpit version: %1$s", jocMetaInfo.getJocVersion()));
                LOGGER.info(String.format("  with inventory schema version: %1$s", jocMetaInfo.getInventorySchemaVersion()));
                LOGGER.info(String.format("  and API schema version: %1$s", jocMetaInfo.getApiVersion()));
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            Set<String> agentNames = agentDbLayer.getVisibleAgentNames();
            
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Set<ConfigurationObject> filteredConfigurations = new HashSet<ConfigurationObject>();
            final List<ConfigurationType> importOrder = ImportUtils.getImportOrder();
            List<DBItemInventoryConfiguration> storedConfigurations = new ArrayList<DBItemInventoryConfiguration>();
            Map<ConfigurationType, Map<String, String>> oldNewNameMap = new HashMap<>();
            List<DBItemInventoryConfiguration> updated = new ArrayList<DBItemInventoryConfiguration>();
            
            if (!configurations.isEmpty()) {
                if (filter.getOverwrite()) {
                    Stream<ConfigurationObject> cfgStream = configurations.stream().filter(Objects::nonNull);
                    if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                        cfgStream = cfgStream.peek(item -> item.setPath(filter.getTargetFolder() + item.getPath()));
                        // filter according to folder permissions on target folder
                    }
                    filteredConfigurations = cfgStream.filter(configuration 
                            -> canAdd(configuration.getPath(), permittedFolders)).collect(Collectors.toSet());
                    if (!filteredConfigurations.isEmpty()) {
                        Map<ConfigurationType, List<ConfigurationObject>> configurationsByType = filteredConfigurations.stream()
                                .collect(Collectors.groupingBy(ConfigurationObject::getObjectType));
                        for (ConfigurationType type : importOrder) {
                            List<ConfigurationObject> configurationObjectsByType = configurationsByType.get(type);
                            if (configurationObjectsByType != null && !configurationObjectsByType.isEmpty()) {
                                for (ConfigurationObject configuration : configurationObjectsByType) {
                                    DBItemInventoryConfiguration existingItem = dbLayer.getConfigurationByName(configuration.getName(), configuration
                                            .getObjectType());
                                    if (existingItem != null) {
                                        if (!JocInventory.isJsonHashEqual(existingItem.getContent(), configuration.getConfiguration())) {
                                            storedConfigurations.add(dbLayer.updateInventoryConfiguration(configuration, existingItem, account,
                                                    auditLogId, agentNames));
                                        } else {
                                            /*
                                             * hash is equal, content did not differ, but overwrite is set to true - conflicting
                                             * - currently overwrite is ignored
                                             */
                                        }
                                    } else {
                                        storedConfigurations.add(dbLayer.saveInventoryConfiguration(configuration, account, auditLogId, agentNames));
                                    }
                                }
                            }
                        }
                    }
            	} else {
                    if ((filter.getSuffix() != null && !filter.getSuffix().isEmpty()) ||
                    		(filter.getPrefix() != null && !filter.getPrefix().isEmpty())) {
                    	// process prefix/suffix only if overwrite==false AND one of both not empty 
                        
                    	Map<ConfigurationType, List<ConfigurationObject>> configurationsByType = configurations.stream()
                    			.collect(Collectors.groupingBy(ConfigurationObject::getObjectType));
                        Map<ConfigurationObject, Set<ConfigurationObject>> updatedReferencesByUpdateableConfiguration = new HashMap<>();
                        for (ConfigurationType type : importOrder) {
                            List<ConfigurationObject> configurationObjectsByType = configurationsByType.get(type);
                            if (configurationObjectsByType != null && !configurationObjectsByType.isEmpty()) {
                                for (ConfigurationObject configuration : configurationObjectsByType) {
                                    DBItemInventoryConfiguration existingConfiguration = dbLayer.getConfigurationByName(configuration.getName(),
                                            configuration.getObjectType());
                                    if (canAdd(configuration.getPath(), permittedFolders)) {
                                        filteredConfigurations.add(configuration);
                                        UpdateableConfigurationObject updateable = ImportUtils.createUpdateableConfiguration(
                                                existingConfiguration, configuration, configurationsByType, filter.getPrefix(), 
                                                filter.getSuffix(), filter.getTargetFolder(), dbLayer);
                                        ImportUtils.replaceReferences(updateable);
                                        updatedReferencesByUpdateableConfiguration.put(updateable.getConfigurationObject(), 
                                                updateable.getReferencedBy());
                                        
                                        storedConfigurations.add(dbLayer.saveInventoryConfiguration(updateable.getConfigurationObject(), account,
                                                auditLogId, agentNames));
                                        oldNewNameMap.putIfAbsent(updateable.getConfigurationObject().getObjectType(), new HashMap<>());
                                        oldNewNameMap.get(updateable.getConfigurationObject().getObjectType()).put(updateable.getOldName(), updateable
                                                .getNewName());
                                    }
                                }
                            }
                        }
                        // update the changed referenced object if already exists
                        Set<ConfigurationObject> alreadyStored = new HashSet<ConfigurationObject>();
                        for (ConfigurationObject reference : updatedReferencesByUpdateableConfiguration.keySet()) {
                            Set<ConfigurationObject> referencedBy = updatedReferencesByUpdateableConfiguration.get(reference);
                            if(referencedBy != null) {
                                 for (ConfigurationObject refBy : referencedBy) { 
                                     if (!alreadyStored.contains(refBy)) {
                                         DBItemInventoryConfiguration stored = 
                                                 ImportUtils.updateConfigurationWithChangedReferences(dbLayer, refBy);
                                         alreadyStored.add(refBy);
                                         if(stored != null) {
                                             updated.add(stored);
                                         }
                                     }
                                 }
                            }
                        }
                    } else {
                        // check if items to import already exist in current configuration and ignore them
                        // import only if item does not exist yet
                        Map<ConfigurationType, List<ConfigurationObject>> configurationsByType = configurations.stream()
                                .collect(Collectors.groupingBy(ConfigurationObject::getObjectType));
                        for (ConfigurationType type : importOrder) {
                            List<ConfigurationObject> configurationObjectsByType = configurationsByType.get(type);
                            if (configurationObjectsByType != null && !configurationObjectsByType.isEmpty()) {
                                for (ConfigurationObject configuration : configurationsByType.get(type)) {
                                    DBItemInventoryConfiguration existingConfiguration = 
                                            dbLayer.getConfigurationByName(configuration.getName(), configuration.getObjectType());
                                    if (existingConfiguration == null) {
                                        if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                                            if(!configuration.getPath().startsWith(filter.getTargetFolder())) {
                                                configuration.setPath(filter.getTargetFolder() + configuration.getPath());
                                            }
                                            if (canAdd(configuration.getPath(), permittedFolders)) {
                                                filteredConfigurations.add(configuration);
                                                storedConfigurations.add(dbLayer.saveInventoryConfiguration(configuration, account, auditLogId, agentNames));
                                            }
                                        } else {
                                            if (canAdd(configuration.getPath(), permittedFolders)) {
                                                filteredConfigurations.add(configuration);
                                                storedConfigurations.add(dbLayer.saveInventoryConfiguration(configuration, account, auditLogId, agentNames));
                                            }
                                        }
                                    } else {
                                      storedConfigurations.add(existingConfiguration);
                                    }
                                }
                            }
                        }
                    }
            	}
                updated.addAll(storedConfigurations);
                DependencyResolver.updateDependencies(updated);
                if (!filteredConfigurations.isEmpty()) {
                    JocAuditLog.storeAuditLogDetails(filteredConfigurations.stream().map(i -> new AuditLogDetail(i.getPath(), i.getObjectType()
                            .intValue())), hibernateSession, auditLogId, dbAuditItem.getCreated());
                    InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
                    filteredConfigurations.stream().map(ConfigurationObject::getPath).map(path -> Paths.get(path).getParent()).distinct().forEach(
                            item -> {
                                try {
                                    JocInventory.makeParentDirs(invDbLayer, item, auditLogId, ConfigurationType.FOLDER);
                                } catch (SOSHibernateException e) {
                                    throw new JocSosHibernateException(e);
                                }
                            });
                }
            }
            
            if (values.getTags() != null) {
                ImportUtils.importTags(storedConfigurations, oldNewNameMap, values.getTags(), filter.getOverwriteTags(), hibernateSession);
            }
            InventoryDBLayer invDbLayer = new InventoryDBLayer(hibernateSession);
            List<DBItemInventoryConfiguration> invalidDBItems = invDbLayer.getAllInvalidConfigurations();
            CompletableFuture.runAsync(() -> {
                Report report = new Report();
                try {
                    report = RevalidateResourceImpl.revalidate(invalidDBItems, getJocError());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                // post events
                Stream.concat(storedConfigurations.stream().map(DBItemInventoryConfiguration::getPath), report.getValidObjs().stream()
                        .map(ReportItem::getPath)).map(JOCResourceImpl::getParent).distinct().forEach(JocInventory::postEvent);
                // post folder events
                if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                    storedConfigurations.stream().map(DBItemInventoryConfiguration::getFolder).distinct()
                    .peek(JocInventory::postFolderEvent)
                    .map(parent -> parent.replaceFirst(filter.getTargetFolder(), ""))
                    .forEach(JocInventory::postFolderEvent);
                } else {
                    storedConfigurations.stream().map(DBItemInventoryConfiguration::getFolder).distinct()
                    .forEach(JocInventory::postFolderEvent);
                }
            });
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
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
