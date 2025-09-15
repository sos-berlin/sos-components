package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.inventory.resource.ISynchronizeResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.sync.RequestFilter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;

@Path(JocInventory.APPLICATION_PATH)
public class SynchronizeResourceImpl extends JOCResourceImpl implements ISynchronizeResource {

    private static final String API_CALL = JocInventory.getResourceImplPath("inventory/synchronize");

    @Override
    public JOCDefaultResponse synchronize(final String accessToken, byte[] requestBody) {
        SOSHibernateSession session = null;
        try {
            requestBody = initLogging(API_CALL, requestBody, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(requestBody, RequestFilter.class);
            RequestFilter filter = Globals.objectMapper.readValue(requestBody, RequestFilter.class);

            filter.setFolder(normalizeFolder(filter.getFolder()));
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if(filter.getAuditLog() != null) {
                storeAuditLog(filter.getAuditLog());
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            JControllerState currentstate = null;
            Set<Integer> objectTypes = JocInventory.DEPLOYABLE_OBJECTS.stream().map(ConfigurationType::intValue).collect(Collectors.toSet()); 
            objectTypes.add(ConfigurationType.FOLDER.intValue());
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(filter.getFolder(), filter.getRecursive(), objectTypes, true, false);
            try {
                DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
                DeployedConfigurationFilter deployedFilter = new DeployedConfigurationFilter();
                deployedFilter.setControllerId(filter.getControllerId());
                Folder fld = new Folder();
                fld.setFolder(filter.getFolder());
                fld.setRecursive(filter.getRecursive());
                deployedFilter.setFolders(Collections.singleton(fld));
                deployedFilter.setObjectTypes(objectTypes);
                currentstate = Proxy.of(filter.getControllerId()).currentState();
                // Map<objectType, Map<InventoryId, name>>
                Map<Integer, Map<Long, String>> deployedNames = deployedDbLayer.getDeployedNames(deployedFilter);
                
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                Set<String> parentFolders = new HashSet<String>();
                boolean updated = false;
                if (items != null) {
                    for (InventoryTreeFolderItem item : items) {
                        if (item == null) {// e.g. unknown type
                            continue;
                        }
                        if (filter.getRecursive() && !canAdd(item.getPath(), permittedFolders)) {
                            continue;
                        }
                        SyncState syncState = SyncStateHelper.getState(currentstate, item.getId(), item.getObjectType(),
                                deployedNames.get(item.getObjectType().intValue()));
                        if(!ConfigurationType.FOLDER.equals(item.getObjectType()) && SyncStateText.NOT_DEPLOYED.equals(syncState.get_text())
                                && SyncStateText.NOT_IN_SYNC.equals(syncState.get_text())) {
                            DBItemInventoryConfiguration invItem = session.get(DBItemInventoryConfiguration.class, item.getId());
                            invItem.setDeployed(false);
                            session.update(invItem);
                            updated = true;
                            parentFolders.add(invItem.getFolder());
                        } else if (ConfigurationType.FOLDER.equals(item.getObjectType())) {
                            parentFolders.add(item.getPath());
                        }
                    }
                }
                if (updated) {
                    parentFolders.forEach(JocInventory::postFolderEvent);
                }
            } catch (Exception e) {
                ProblemHelper.postExceptionEventIfExist(Either.left(e), null, getJocError(), null);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
