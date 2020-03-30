package com.sos.joc.classes.jobscheduler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;

public class MasterCallable implements Callable<MasterAnswer> {
	private final DBItemInventoryInstance dbItemInventoryInstance;
	private final DBItemOperatingSystem dbOsSystem;
	private final String accessToken;
	private final boolean onlyDb;
    private static final Logger LOGGER = LoggerFactory.getLogger(MasterCallable.class);

    public MasterCallable(DBItemInventoryInstance dbItemInventoryInstance, DBItemOperatingSystem dbOsSystem, String accessToken) {
		this.dbItemInventoryInstance = dbItemInventoryInstance;
		this.dbOsSystem = dbOsSystem;
		this.accessToken = accessToken;
		this.onlyDb = false;
	}
    
    public MasterCallable(DBItemInventoryInstance dbItemInventoryInstance, DBItemOperatingSystem dbOsSystem, String accessToken, boolean onlyDb) {
        this.dbItemInventoryInstance = dbItemInventoryInstance;
        this.dbOsSystem = dbOsSystem;
        this.accessToken = accessToken;
        this.onlyDb = onlyDb;
    }

	@Override
	public MasterAnswer call() throws JobSchedulerInvalidResponseDataException {
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
            } catch (JobSchedulerInvalidResponseDataException e) {
                throw e;
            } catch (JocException e) {
                LOGGER.info("", e);
            }
        }
        MasterAnswer js = new MasterAnswer(overview, clusterState, dbItemInventoryInstance, dbOsSystem, onlyDb);
		js.setFields();
		return js;
	}
}
