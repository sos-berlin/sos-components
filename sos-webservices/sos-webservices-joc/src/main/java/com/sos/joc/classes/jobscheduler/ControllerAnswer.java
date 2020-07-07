package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.jobscheduler.model.command.overview.SystemProperties;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.model.jobscheduler.ComponentStateText;
import com.sos.joc.model.jobscheduler.ConnectionStateText;
import com.sos.joc.model.jobscheduler.Controller;
import com.sos.joc.model.jobscheduler.OperatingSystem;
import com.sos.joc.model.jobscheduler.Role;

public class ControllerAnswer extends Controller {

    @JsonIgnore
	private final Overview overviewJson;
	@JsonIgnore
    private final ClusterState clusterStateJson;
    @JsonIgnore
	private DBItemInventoryJSInstance dbInstance;
	@JsonIgnore
	private DBItemInventoryOperatingSystem dbOs;
	@JsonIgnore
	private boolean updateDbInstance = false;
	@JsonIgnore
    private ClusterType clusterState = null;
	@JsonIgnore
    private boolean onlyDb = false;

	public ControllerAnswer(Overview overview, ClusterState clusterState, DBItemInventoryJSInstance dbInstance, DBItemInventoryOperatingSystem dbOs, boolean onlyDb) {
		this.overviewJson = overview;
		this.clusterStateJson = clusterState;
		if (clusterState != null) {
		    this.clusterState = clusterState.getTYPE();
		}
        this.dbInstance = dbInstance;
		if (dbOs == null) {
			dbOs = new DBItemInventoryOperatingSystem();
			dbOs.setId(null);
		}
		this.dbOs = dbOs;
		this.onlyDb = onlyDb;
	}

	@JsonIgnore
	public DBItemInventoryJSInstance getDbInstance() {
		return dbInstance;
	}

	@JsonIgnore
	public void setDbInstance(DBItemInventoryJSInstance dbInstance) {
		this.dbInstance = dbInstance;
	}

	@JsonIgnore
	public DBItemInventoryOperatingSystem getDbOs() {
		return dbOs;
	}

	@JsonIgnore
	public void setDbOs(DBItemInventoryOperatingSystem dbOs) {
		this.dbOs = dbOs;
	}
	
	@JsonIgnore
    public ClusterType getClusterState() {
        return clusterState;
    }
	
	@JsonIgnore
    public boolean isCoupledOrPreparedTobeCoupled() {
        return clusterState != null && (clusterState == ClusterType.COUPLED || clusterState == ClusterType.PREPARED_TO_BE_COUPLED);
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
			    case EMPTY:
			        isActive = true;
			        break;
			    default:
			        String activeClusterUri = clusterStateJson.getIdToUri().getAdditionalProperties().get(clusterStateJson.getActiveId());
                    isActive = activeClusterUri.equalsIgnoreCase(dbInstance.getClusterUri()) || activeClusterUri.equalsIgnoreCase(dbInstance.getUri());
			        break;
			    }
	        }
			if (clusterState != null && clusterState == ClusterType.PREPARED_TO_BE_COUPLED) {
			    setComponentState(States.getComponentState(ComponentStateText.inoperable));
			} else {
			    setComponentState(States.getComponentState(ComponentStateText.operational));
			}
			setIsCoupled(clusterState != null && clusterState == ClusterType.COUPLED);
			setClusterNodeState(States.getClusterNodeState(isActive, dbInstance.getIsCluster()));
			setConnectionState(States.getConnectionState(ConnectionStateText.established));
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
//			if (dbInstance.getIsCluster() && isActive != null) {
//			    dbInstance.setIsActive(isActive);
//                updateDbInstance = true;
//            }
			// dbInstance.setTimezone(val); TODO doesn't contain in answer yet
		} else {
			setSurveyDate(dbInstance.getModified());
			setStartedAt(dbInstance.getStartedAt());
			if (!onlyDb) {
			    setComponentState(States.getComponentState(ComponentStateText.unknown));
			    setClusterNodeState(States.getClusterNodeState(null, dbInstance.getIsCluster()));
			    setConnectionState(States.getConnectionState(ConnectionStateText.unreachable));
			}
		}
		
		setId(dbInstance.getId());
		setJobschedulerId(dbInstance.getSchedulerId());
		setClusterUrl(getClusterUrl(dbInstance));
		setRole(getRole(dbInstance));
		setTitle(getTitle(dbInstance));
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
	
	public static String getClusterUrl(DBItemInventoryJSInstance dbInstance) {
	    if (dbInstance.getIsCluster()) {
	        return dbInstance.getClusterUri();
	    } else {
	        return null;
	    }
	}
	
	public static String getTitle(DBItemInventoryJSInstance dbInstance) {
        if (dbInstance.getTitle() == null || dbInstance.getTitle().isEmpty()) {
            return getRole(dbInstance).value();
        } else {
            return dbInstance.getTitle();
        }
    }
	
	public static Role getRole(DBItemInventoryJSInstance dbInstance) {
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

}
