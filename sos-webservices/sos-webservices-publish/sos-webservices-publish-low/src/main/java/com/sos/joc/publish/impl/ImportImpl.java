package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.ImportUtils;
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
        filter.setTargetFolder(targetFolder);
        filter.setOverwrite(overwrite);
        filter.setPrefix(prefix);
        filter.setSuffix(suffix);
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
            
            DBItemJocAuditLog dbAuditItem = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            Long auditLogId = dbAuditItem.getId();
            
            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
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
            
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Set<ConfigurationObject> filteredConfigurations = new HashSet<ConfigurationObject>();
            if (!configurations.isEmpty()) {
                if (filter.getOverwrite()) {
                    if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                    	// filter according to folder permissions on target folder
                    	filteredConfigurations = configurations.stream().peek(item -> item.setPath(filter.getTargetFolder() + item.getPath()))
                    			.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                    	if (!filteredConfigurations.isEmpty()) {
                    		filteredConfigurations.stream().forEach(configuration 
                    				-> dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames));
                    	}
                    } else {
                		// filter according to folder permissions
                    	filteredConfigurations = configurations.stream().filter(configuration 
                    			-> canAdd(configuration.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                    	if (!filteredConfigurations.isEmpty()) {
                    		filteredConfigurations.stream().forEach(configuration 
                    				-> dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames));
                    	}
                    }
            	} else {
                    if ((filter.getSuffix() != null && !filter.getSuffix().isEmpty()) ||
                    		(filter.getPrefix() != null && !filter.getPrefix().isEmpty())) {
                    	// process prefix/suffix only if overwrite==false AND one of both not empty 
                		// TargetFolder
                        final List<ConfigurationType> importOrder = Arrays.asList(ConfigurationType.LOCK, ConfigurationType.JOBRESOURCE,
                                ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.WORKFLOW,
                                ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE);
                        
                    	Map<ConfigurationType, List<ConfigurationObject>> configurationsByType = configurations.stream()
                    			.collect(Collectors.groupingBy(ConfigurationObject::getObjectType));
                    	for (ConfigurationType type : importOrder) {
                    		List<ConfigurationObject> configurationObjectsByType = configurationsByType.get(type);
                    		if (configurationObjectsByType != null && !configurationObjectsByType.isEmpty()) {
                        		for (ConfigurationObject configuration : configurationsByType.get(type)) {
                            		DBItemInventoryConfiguration existingConfiguration = dbLayer.getConfigurationByName(configuration.getName(), configuration.getObjectType());
                            		if (existingConfiguration != null) {
                            			if (canAdd(configuration.getPath(), permittedFolders)) {
                            				filteredConfigurations.add(configuration);
                                        	UpdateableConfigurationObject updateable =  ImportUtils.createUpdateableConfiguration(
                                        			existingConfiguration, configuration, configurations, filter.getPrefix(), filter.getSuffix(), filter.getTargetFolder(), dbLayer);
                                        	ImportUtils.replaceReferences(updateable);
                                        	dbLayer.saveNewInventoryConfiguration(updateable.getConfigurationObject(), account, auditLogId, filter.getOverwrite(), agentNames);
                            			}
                            		} else {
                                        if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                                        	if (!configuration.getPath().startsWith(filter.getTargetFolder())) {
                                        		configuration.setPath(filter.getTargetFolder() + configuration.getPath());
                                        	}
                                            if (canAdd(configuration.getPath(), permittedFolders)) {
                                				filteredConfigurations.add(configuration);
                                                dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames);
                                            }
                                        } else {
                                        	if (canAdd(configuration.getPath(), permittedFolders)) {
                                				filteredConfigurations.add(configuration);
                                        		dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames);
                                        	}
                                        }
                            		}
                        		}
                    		}
                    	}
                    } else {
                    	// check if items to import already exist in current configuration and ignore them
                    	// import only if item does not exist yet
                    	for (ConfigurationObject configuration : configurations) {
                    		DBItemInventoryConfiguration existingConfiguration = 
                    				dbLayer.getConfigurationByName(configuration.getName(), configuration.getObjectType());
                    		if (existingConfiguration == null) {
                    			if(filter.getTargetFolder() != null && !filter.getTargetFolder().isEmpty()) {
                    				if(!configuration.getPath().startsWith(filter.getTargetFolder())) {
                    					configuration.setPath(filter.getTargetFolder() + configuration.getPath());
                    				}
                    				if (canAdd(configuration.getPath(), permittedFolders)) {
                        				filteredConfigurations.add(configuration);
                    					dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames);
                    				}
                    			} else {
                    				if (canAdd(configuration.getPath(), permittedFolders)) {
                        				filteredConfigurations.add(configuration);
                    					dbLayer.saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, filter.getOverwrite(), agentNames);
                    				}
                    			}
                    		}
                    	}
                    	
                    }
            	}
                if (!filteredConfigurations.isEmpty()) {
                    JocAuditLog.storeAuditLogDetails(filteredConfigurations.stream().map(i -> new AuditLogDetail(i.getPath(), i.getObjectType()
                            .intValue())), hibernateSession, auditLogId, dbAuditItem.getCreated());
                    InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
                    filteredConfigurations.stream().map(ConfigurationObject::getPath).distinct().map(path -> Paths.get(path).getParent()).forEach(item -> {
                        try {
                            JocInventory.makeParentDirs(invDbLayer, item, auditLogId);
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                    });
                }
            }
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
