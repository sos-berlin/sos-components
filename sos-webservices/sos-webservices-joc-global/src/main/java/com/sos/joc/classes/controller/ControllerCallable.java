package com.sos.joc.classes.controller;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.cluster.ClusterSetting;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.controller.model.cluster.IdToUri;
import com.sos.controller.model.command.Overview;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.ControllerServiceUnavailableException;
import com.sos.joc.exceptions.JocException;

public class ControllerCallable implements Callable<ControllerAnswer> {
	private final DBItemInventoryJSInstance dbItemInventoryInstance;
	private final DBItemInventoryOperatingSystem dbOsSystem;
	private final String accessToken;
	private final boolean onlyDb;
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCallable.class);

    public ControllerCallable(DBItemInventoryJSInstance dbItemInventoryInstance, DBItemInventoryOperatingSystem dbOsSystem, String accessToken) {
        this.dbItemInventoryInstance = dbItemInventoryInstance;
        this.dbOsSystem = dbOsSystem;
        this.accessToken = accessToken;
        this.onlyDb = false;
    }

    public ControllerCallable(DBItemInventoryJSInstance dbItemInventoryInstance, DBItemInventoryOperatingSystem dbOsSystem, String accessToken,
            boolean onlyDb) {
        this.dbItemInventoryInstance = dbItemInventoryInstance;
        this.dbOsSystem = dbOsSystem;
        this.accessToken = accessToken;
        this.onlyDb = onlyDb;
    }
    
    public ControllerCallable(DBItemInventoryJSInstance dbItemInventoryInstance) {
        this.dbItemInventoryInstance = dbItemInventoryInstance;
        this.dbOsSystem = null;
        this.accessToken = null;
        this.onlyDb = false;
    }

	@Override
	public ControllerAnswer call() throws ControllerInvalidResponseDataException {
		Overview overview = null;
		ClusterState clusterState = null;
        if (!onlyDb) {
            try {
                JOCJsonCommand jocJsonCommand = new JOCJsonCommand(dbItemInventoryInstance, accessToken);
                jocJsonCommand.setAutoCloseHttpClient(false);
                jocJsonCommand.setUriBuilderForOverview();
                overview = jocJsonCommand.getJsonObjectFromGet(Overview.class);
                jocJsonCommand.setUriBuilderForCluster();
                clusterState = jocJsonCommand.getJsonObjectFromGet(ClusterState.class);
                jocJsonCommand.closeHttpClient();
            } catch (ControllerInvalidResponseDataException e) {
                throw e;
            } catch (ControllerConnectionRefusedException | ControllerConnectionResetException e) {
                LOGGER.debug(e.toString());
            } catch (ControllerServiceUnavailableException e) {
                if (dbItemInventoryInstance.getIsCluster() && e.getMessage().startsWith("ClusterNodeIsNotReady:")) {
                    String activeId = dbItemInventoryInstance.getIsPrimary() ? "Primary" : "Backup";
                    IdToUri idToUri = new IdToUri();
                    idToUri.setAdditionalProperty(activeId, dbItemInventoryInstance.getClusterUri());
                    clusterState = new ClusterState(ClusterType.ACTIVE_NODE_IS_NOT_READY, new ClusterSetting(idToUri, activeId, null, null));
                } else {
                    LOGGER.info(e.toString());
                }
            } catch (JocException e) {
                LOGGER.info(e.toString());
            }
        }
        ControllerAnswer js = new ControllerAnswer(overview, clusterState, dbItemInventoryInstance, dbOsSystem, onlyDb);
		js.setFields();
		return js;
	}
}
