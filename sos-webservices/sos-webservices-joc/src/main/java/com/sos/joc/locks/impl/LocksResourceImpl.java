package com.sos.joc.locks.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.lock.common.LockEntryHelper;
import com.sos.joc.locks.resource.ILocksResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.lock.Locks;
import com.sos.joc.model.lock.LocksFilter;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;

@Path("locks")
public class LocksResourceImpl extends JOCResourceImpl implements ILocksResource {

    private static final String API_CALL = "./locks";
    private static final Logger LOGGER = LoggerFactory.getLogger(LocksResourceImpl.class);

    @Override
    public JOCDefaultResponse postLocks(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LocksFilter.class);
            LocksFilter filter = Globals.objectMapper.readValue(filterBytes, LocksFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getLocks().getView());
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
            dbFilter.setObjectTypes(Collections.singleton(DeployType.LOCK.intValue()));

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
                dbFilter.setNames(paths.stream().map(p -> JocInventory.pathToName(p)).collect(Collectors.toSet()));
                contents = dbLayer.getDeployedInventory(dbFilter);

            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
            } else if (folders != null && !folders.isEmpty()) {
                dbFilter.setFolders(folders);
                contents = dbLayer.getDeployedInventory(dbFilter);
            } else {
                contents = dbLayer.getDeployedInventory(dbFilter);
            }

            Locks answer = new Locks();
            answer.setSurveyDate(Date.from(Instant.now()));
            LockEntryHelper helper = new LockEntryHelper(filter.getControllerId(), filter.getCompact(), filter.getLimit());
            final JControllerState controllerState = getCurrentState(filter.getControllerId());
            if (controllerState != null) {
                answer.setSurveyDate(Date.from(controllerState.instant()));
            }
            JocError jocError = getJocError();
            if (contents != null) {
                answer.setLocks(contents.stream().filter(dc -> canAdd(dc.getPath(), folders)).map(dc -> {
                    try {
                        if (dc.getContent() == null || dc.getContent().isEmpty()) {
                            throw new DBMissingDataException("doesn't exist");
                        }
                        return helper.getLockEntry(controllerState, dc);
                    } catch (Throwable e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            answer.setDeliveryDate(Date.from(Instant.now()));
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }

    }
    
    private JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

}