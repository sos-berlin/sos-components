package com.sos.joc.publish.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.RedeploySyncFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IRedeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.fileordersource.FileOrderSource;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) {
        return deploy(xAccessToken, filter, StoreDeployments.API_CALL_REDEPLOY);
    }

    @Override
    public JOCDefaultResponse postSync(String xAccessToken, byte[] filter) {
        return deploy(xAccessToken, filter, StoreDeployments.API_CALL_SYNC);
    }

    public JOCDefaultResponse deploy(String xAccessToken, byte[] filter, String action) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(action, filter, xAccessToken, CategoryType.DEPLOYMENT);
            if(action.equals(StoreDeployments.API_CALL_REDEPLOY)) {
                JsonValidator.validateFailFast(filter, RedeployFilter.class);
            } else {
                JsonValidator.validateFailFast(filter, RedeploySyncFilter.class);
            }
            RedeploySyncFilter redeployFilter = Globals.objectMapper.readValue(filter, RedeploySyncFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(redeployFilter.getAuditLog());

            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            hibernateSession = Globals.createSosHibernateStatelessConnection(action);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            String controllerId = redeployFilter.getControllerId();
            List<DBItemDeploymentHistory> latest = new ArrayList<DBItemDeploymentHistory>();
            // get all latest active history objects from the database for the provided controllerId and folder from the filter
            if(StoreDeployments.API_CALL_REDEPLOY.equals(action)) {
                //redeploy
                latest = dbLayer.getLatestDepHistoryItemsFromFolder("/", controllerId, true).toList();
            } else {
                //synchronize
                latest = dbLayer.getLatestDepHistoryItemsFromFolder(redeployFilter.getFolder(), controllerId, redeployFilter.getRecursive()).toList();
            }
            // all items will be resigned with a new commitId
            final String commitId = UUID.randomUUID().toString();
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);

            final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();

            List<DBItemDeploymentHistory> unsignedRedeployables = null;
            if (!latest.isEmpty()) {
                Stream<DBItemDeploymentHistory> latestStream = latest.stream().filter(item -> OperationType.DELETE.value() != item.getOperation());
                if (StoreDeployments.API_CALL_SYNC.equals(action)) {
                    // filter latest with only "not in sync" objects
                    final JControllerState currentstate = SyncStateHelper.getControllerState(controllerId, xAccessToken, getJocError());
                    latestStream = latestStream.filter(item -> SyncStateHelper.isNotInSync(currentstate, item.getName(), item.getType()));
                }
                // add dependencies for latest items from the folder to get related items from other folders, too
                unsignedRedeployables = latestStream.peek(item -> {
                    try {
                        item.writeUpdateableContent(JsonConverter.readAsConvertedDeployObject(item.getControllerId(), item.getPath(), item.getInvContent(),
                                StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
                    } catch (IOException e) {
                        throw new JocException(e);
                    }
                }).collect(Collectors.toList());
            }
            // preparations
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedRedeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            
            if (StoreDeployments.API_CALL_SYNC.equals(action)) {
                // determine dependencies for deployment history items for sync only
                // add all dependencies (even from different folders), too. not needed for redeploy
                if (unsignedRedeployables != null) {
                    InventoryDBLayer layer = new InventoryDBLayer(dbLayer.getSession()); 
                    DBLayerDependencies dependencyLayer = new DBLayerDependencies(layer.getSession());
                    Map<DBItemDeploymentHistory,ReferencedDbItem> dependenciesFromInventory = unsignedRedeployables.stream().filter(Objects::nonNull).distinct()
                            .collect(Collectors.toMap(Function.identity(), item -> {
                                try {
                                    DBItemInventoryConfiguration cfg = layer.getConfiguration(item.getInventoryConfigurationId());
                                    if (cfg != null) {
                                        List<DBItemInventoryDependency> dbDependencies = dependencyLayer.getRequestedDependencies(cfg);
                                        return DependencyResolver.convert(layer.getSession(), cfg, dbDependencies);
                                    } else {
                                        return new ReferencedDbItem(null);
                                    }
                                } catch (IOException | SOSHibernateException e) {
                                    return new ReferencedDbItem(null);
                                }
                            }));
                    Set<DBItemDeploymentHistory> dependencyHistorieItems = new HashSet<DBItemDeploymentHistory>();
                    dependenciesFromInventory.entrySet().forEach(entry -> {
                        if(entry.getValue().getReferencedItem() != null) {
                            entry.getValue().getReferences().forEach( inv -> {
                                try {
                                    dependencyHistorieItems.add(dbLayer.getLatestActiveDepHistoryItem(inv.getId(), entry.getKey().getControllerId()));
                                } catch (SOSHibernateException e) {}
                            });
                            entry.getValue().getReferencedBy().stream().filter(inv -> ConfigurationType.WORKFLOW.equals(inv.getTypeAsEnum()))
                            .forEach( inv -> {
                                try {
                                    dependencyHistorieItems.add(dbLayer.getLatestActiveDepHistoryItem(inv.getId(), entry.getKey().getControllerId()));
                                } catch (SOSHibernateException e) {}
                            });
                        }
                    });
                    Set<DBItemDeploymentHistory> dependencies = dependencyHistorieItems.stream()
                            .filter(Objects::nonNull)
                            .filter(item -> OperationType.UPDATE.value().equals(item.getOperation()))
                            .peek(item -> {
                                try {
                                    item.writeUpdateableContent(JsonConverter.readAsConvertedDeployObject(item.getControllerId(), item.getPath(), item.getInvContent(),
                                            StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
                                } catch (IOException e) {
                                    throw new JocException(e);
                                }
                            }).collect(Collectors.toSet());
                    for(DBItemDeploymentHistory dependency : dependencies) {
                        if(!unsignedRedeployables.contains(dependency)) {
                            unsignedRedeployables.add(dependency);
                        }
                    }
                    // END: determine dependencies for deployment history items for sync only
                }
            }
            if (unsignedRedeployables != null && !unsignedRedeployables.isEmpty()) {
                unsignedRedeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                unsignedRedeployables.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.equals(ConfigurationType.fromValue(item.getType())))
                        .forEach(item -> {
                            UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(item, controllerId,
                                    dbLayer);
                            updateableAgentNamesFileOrderSources.add(update);
                            try {
                                ((FileOrderSource) item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                updateableAgentNamesFileOrderSources.add(update);
                            } catch (Exception e) {
                            }
                        });
                unsignedRedeployables = unsignedRedeployables.stream().map(item -> ADeploy.cloneToNew(item)).collect(Collectors.toList());
                verifiedRedeployables.putAll(PublishUtils.getDraftsWithSignature(commitId, account, unsignedRedeployables, updateableAgentNames,
                        keyPair, controllerId, hibernateSession));
            }
            if (verifiedRedeployables != null && !verifiedRedeployables.isEmpty()) {
                SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, verifiedRedeployables, updateableAgentNames,
                        updateableAgentNamesFileOrderSources, dbAuditlog.getId());
                StoreDeployments.storeNewDepHistoryEntriesForRedeploy(signedItemsSpec, account, commitId, controllerId, getAccessToken(),
                        getJocError(), dbLayer);
                // call updateItems command via ControllerApi for given controllers
                StoreDeployments.callUpdateItemsFor(dbLayer, signedItemsSpec, Collections.emptySet(), account, commitId, controllerId,
                        getAccessToken(), getJocError(), action);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}