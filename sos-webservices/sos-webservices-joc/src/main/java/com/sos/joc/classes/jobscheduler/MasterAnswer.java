package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.jobscheduler.model.command.overview.SystemProperties;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.model.jobscheduler.ClusterNodeState;
import com.sos.joc.model.jobscheduler.ClusterNodeStateText;
import com.sos.joc.model.jobscheduler.ComponentState;
import com.sos.joc.model.jobscheduler.ComponentStateText;
import com.sos.joc.model.jobscheduler.ConnectionState;
import com.sos.joc.model.jobscheduler.ConnectionStateText;
import com.sos.joc.model.jobscheduler.Master;
import com.sos.joc.model.jobscheduler.OperatingSystem;
import com.sos.joc.model.jobscheduler.Role;

public class MasterAnswer extends Master {

    @JsonIgnore
	private final Overview overviewJson;
	@JsonIgnore
    private final ClusterState clusterStateJson;
    @JsonIgnore
	private DBItemInventoryInstance dbInstance;
	@JsonIgnore
	private DBItemOperatingSystem dbOs;
	@JsonIgnore
	private boolean updateDbInstance = false;
	@JsonIgnore
    private ClusterType clusterState = null;
	@JsonIgnore
    private boolean onlyDb = false;

	public MasterAnswer(Overview overview, ClusterState clusterState, DBItemInventoryInstance dbInstance, DBItemOperatingSystem dbOs, boolean onlyDb) {
		this.overviewJson = overview;
		this.clusterStateJson = clusterState;
		if (clusterState != null) {
		    this.clusterState = clusterState.getTYPE();
		}
        this.dbInstance = dbInstance;
		if (dbOs == null) {
			dbOs = new DBItemOperatingSystem();
			dbOs.setId(null);
		}
		this.dbOs = dbOs;
		this.onlyDb = onlyDb;
	}

	@JsonIgnore
	public DBItemInventoryInstance getDbInstance() {
		return dbInstance;
	}

	@JsonIgnore
	public void setDbInstance(DBItemInventoryInstance dbInstance) {
		this.dbInstance = dbInstance;
	}

	@JsonIgnore
	public DBItemOperatingSystem getDbOs() {
		return dbOs;
	}

	@JsonIgnore
	public void setDbOs(DBItemOperatingSystem dbOs) {
		this.dbOs = dbOs;
	}
	
	@JsonIgnore
    public ClusterType getClusterState() {
        return clusterState;
    }

	public void setFields() throws JobSchedulerInvalidResponseDataException {
		if (overviewJson != null) {
			if (!dbInstance.getSchedulerId().equals(overviewJson.getId())) {
				throw new JobSchedulerInvalidResponseDataException("unexpected JobSchedulerId " + overviewJson.getId());
			}
			setSurveyDate(Date.from(Instant.now()));
			setStartedAt(Date.from(Instant.ofEpochMilli(overviewJson.getStartedAt())));
			Boolean isActive = null;
			if (clusterStateJson != null) {
			    switch (clusterStateJson.getTYPE()) {
			    case CLUSTER_EMPTY:
			    case CLUSTER_SOLE:
			        isActive = true;
			        break;
			    case CLUSTER_NODES_APPOINTED:
			        isActive = true; //TODO is it right???
			        break;
			    default:
			        String activeClusterUri = clusterStateJson.getUris().get(clusterStateJson.getActive());
                    isActive = activeClusterUri.equalsIgnoreCase(dbInstance.getClusterUri()) || activeClusterUri.equalsIgnoreCase(dbInstance.getUri());
			        break;
			    }
	        }
			setComponentState(getComponentState(ComponentStateText.operational));
			setClusterNodeState(getClusterNodeState(isActive, dbInstance.getIsCluster()));
			setConnectionState(getConnectionState(ConnectionStateText.established));
			dbOs.setHostname(overviewJson.getSystem().getHostname());
			SystemProperties systemProps = overviewJson.getJava().getSystemProperties();
			dbOs.setArchitecture(systemProps.getOs_arch());
			dbOs.setDistribution(systemProps.getOs_version());
			dbOs.setName(systemProps.getOs_name());
			
			if (!getStartedAt().equals(dbInstance.getStartedAt())) {
				dbInstance.setStartedAt(getStartedAt());
				updateDbInstance = true;
			}
			final String version = overviewJson.getVersion().split(" ", 2)[0];
			if (!version.equals(dbInstance.getVersion())) {
				dbInstance.setVersion(version);
				updateDbInstance = true;
			}
			if (dbInstance.getIsCluster() && isActive != null) {
			    dbInstance.setIsActive(isActive);
                updateDbInstance = true;
            }
			// dbInstance.setTimezone(val); TODO doesn't contain in answer yet
		} else {
			setSurveyDate(dbInstance.getModified());
			setStartedAt(dbInstance.getStartedAt());
			if (!onlyDb) {
			    setComponentState(getComponentState(ComponentStateText.unknown));
			    setClusterNodeState(getClusterNodeState(null, dbInstance.getIsCluster()));
			    setConnectionState(getConnectionState(ConnectionStateText.unreachable));
			}
		}
		
		setId(dbInstance.getId());
		setJobschedulerId(dbInstance.getSchedulerId());
		setClusterUrl(getClusterUrl(dbInstance));
		setRole(getRole(dbInstance));
		//TODO title setTitle(dbInstance.getTitle());
        setTitle(getRole().value());
        setUrl(dbInstance.getUri());
		setOs(getOperatingSystem());
		setHost(dbOs.getHostname());
		setVersion(dbInstance.getVersion());
	}
	
	@JsonIgnore
	public void setOsId(Long osId) {
		if (osId != dbInstance.getOsId() && osId != 0L) {
			dbInstance.setOsId(osId);
			updateDbInstance = true;
		}
	}
	
	public boolean dbInstanceIsChanged() {
		return updateDbInstance;
	}
	
	private OperatingSystem getOperatingSystem() {
		OperatingSystem os = new OperatingSystem();
		os.setArchitecture(dbOs.getArchitecture());
		os.setDistribution(dbOs.getDistribution());
		os.setName(dbOs.getName());
		return os;
	}
	
	public static String getClusterUrl(DBItemInventoryInstance dbInstance) {
	    if (dbInstance.getIsCluster()) {
	        return dbInstance.getClusterUri();
	    } else {
	        return null;
	    }
	}
	
	public static Role getRole(DBItemInventoryInstance dbInstance) {
        if (dbInstance.getIsCluster()) {
            if (dbInstance.getIsPrimaryMaster()) {
                return Role.PRIMARY;
            } else {
                return Role.BACKUP;
            }
        } else {
            return Role.STANDALONE;
        }
    }
	
	public static ComponentState getComponentState(ComponentStateText state) {
	    ComponentState componentState = new ComponentState();
	    componentState.set_text(state);
		switch (state) {
		case operational:
		    componentState.setSeverity(0);
			break;
		case limited:
		    componentState.setSeverity(1);
			break;
		case inoperable:
		    componentState.setSeverity(2);
			break;
		case unknown:
		    componentState.setSeverity(3);
            break;
		}
		return componentState;
	}
	
	public static ConnectionState getConnectionState(ConnectionStateText state) {
	    ConnectionState connectionState = new ConnectionState();
	    connectionState.set_text(state);
        switch (state) {
        case established:
            connectionState.setSeverity(0);
            break;
        case unstable:
            connectionState.setSeverity(1);
            break;
        case unreachable:
            connectionState.setSeverity(2);
            break;
        case unknown:
            connectionState.setSeverity(3);
            break;
        }
        return connectionState;
    }
	
    public static ClusterNodeState getClusterNodeState(Boolean isActive, boolean isCluster) {
        if (!isCluster) {
           return null; 
        }
        ClusterNodeState clusterNodeState = new ClusterNodeState();
        if (isActive == null) {
            clusterNodeState.setSeverity(3);
            clusterNodeState.set_text(ClusterNodeStateText.unknown);
        } else if (isActive) {
            clusterNodeState.setSeverity(0);
            clusterNodeState.set_text(ClusterNodeStateText.active);
        } else {
            clusterNodeState.setSeverity(1);
            clusterNodeState.set_text(ClusterNodeStateText.inactive);
        }
        return clusterNodeState;
    }

}
