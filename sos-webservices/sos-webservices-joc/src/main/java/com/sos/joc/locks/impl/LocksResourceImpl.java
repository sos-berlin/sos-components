package com.sos.joc.locks.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.lock.Lock;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.locks.resource.ILocksResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.lock.Locks;
import com.sos.joc.model.lock.LocksFilter;
import com.sos.schema.JsonValidator;

@Path("locks")
public class LocksResourceImpl extends JOCResourceImpl implements ILocksResource {

    private static final String API_CALL = "./locks";

    @Override
    public JOCDefaultResponse postLocks(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LocksFilter.class);
            LocksFilter filter = Globals.objectMapper.readValue(filterBytes, LocksFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getPermissonsJocCockpit(filter.getControllerId(), accessToken)
                    .getOrder().getView().isStatus());
            if (response != null) {
                return response;
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(getLocks(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Locks getLocks(LocksFilter filter) throws Exception {
        SOSHibernateSession session = null;
        try {
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(filter.getControllerId());
            dbFilter.setObjectTypes(Arrays.asList(DeployType.LOCK.intValue()));

            List<String> paths = filter.getLockPaths();
            if (paths != null && !paths.isEmpty()) {
                filter.setFolders(null);
            }
            boolean withFolderFilter = filter.getFolders() != null && !filter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(filter.getFolders());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            List<DeployedContent> contents = null;
            if (paths != null && !paths.isEmpty()) {
                dbFilter.setPaths(new HashSet<String>(paths));
                contents = dbLayer.getDeployedInventory(dbFilter);

            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
            } else if (folders != null && !folders.isEmpty()) {
                dbFilter.setFolders(folders);
                contents = dbLayer.getDeployedInventory(dbFilter);
            } else {
                contents = dbLayer.getDeployedInventory(dbFilter);
            }

            Locks locks = new Locks();
            if (contents != null) {
                Stream<Lock> stream = contents.stream().map(c -> {
                    try {
                        Lock item = Globals.objectMapper.readValue(c.getContent(), Lock.class);
                        // item.setId(JocInventory.pathToName(c.getPath()));
                        return item;
                    } catch (Exception e) {
                        // TODO
                        return null;
                    }
                }).filter(Objects::nonNull);
                locks.setLocks(stream.collect(Collectors.toList()));
            }
            locks.setDeliveryDate(Date.from(Instant.now()));
            return locks;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }

    }

}