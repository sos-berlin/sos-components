package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.command.ClusterState;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.jobscheduler.model.command.overview.SystemProperties;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.model.jobscheduler.JobScheduler;
import com.sos.joc.model.jobscheduler.JobSchedulerState;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;
import com.sos.joc.model.jobscheduler.OperatingSystem;
import com.sos.joc.model.jobscheduler.Role;

public class JobSchedulerAnswer extends JobScheduler {

    @JsonIgnore
    private static final String CLUSTERSTATE_IF_STANDALONE = "ClusterEmpty";
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
    private String clusterState = null;

	public JobSchedulerAnswer(Overview overview, ClusterState clusterState, DBItemInventoryInstance dbInstance, DBItemOperatingSystem dbOs) {
		this.overviewJson = overview;
		this.clusterStateJson = clusterState;
		if (clusterState != null && !CLUSTERSTATE_IF_STANDALONE.equals(clusterState.getTYPE())) {
		    this.clusterState = clusterState.getTYPE();
		}
        this.dbInstance = dbInstance;
		if (dbOs == null) {
			dbOs = new DBItemOperatingSystem();
			dbOs.setId(null);
		}
		this.dbOs = dbOs;
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
    public String getClusterState() {
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
			    if (CLUSTERSTATE_IF_STANDALONE.equals(clusterStateJson.getTYPE())) {
			        isActive = true;
			    } else if (clusterStateJson.getActive() != null && clusterStateJson.getUris() != null && !clusterStateJson.getUris().isEmpty()) {
			        String activeClusterUri = clusterStateJson.getUris().get(clusterStateJson.getActive());
			        isActive = activeClusterUri.equalsIgnoreCase(dbInstance.getClusterUri()) || activeClusterUri.equalsIgnoreCase(dbInstance.getUri());
			    } else {
			        isActive = false;
			    }
	        }
			setState(getJobSchedulerState("running", isActive));
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
			// dbInstance.setCluster(val); TODO doesn't contain in answer yet
			// dbInstance.setPrimaryMaster(val); TODO doesn't contain in answer yet
			// dbInstance.setIsActive(val); TODO doesn't contain in answer yet
		} else {
			setSurveyDate(dbInstance.getModified());
			setStartedAt(dbInstance.getStartedAt());
			setState(getJobSchedulerState("unreachable"));
		}
		
		setId(dbInstance.getId());
		setJobschedulerId(dbInstance.getSchedulerId());
		if (dbInstance.getIsCluster()) {
		    setClusterUrl(dbInstance.getClusterUri());
		}
		setClusterUrl(getClusterUrl(dbInstance));
		setRole(getRole(dbInstance));
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
	
//	public boolean dbInstanceEqualsWith(DBItemInventoryInstance oldDbInstance) {
//		EqualsBuilder eb = new EqualsBuilder();
//		eb.append(oldDbInstance.getOsId(), dbInstance.getOsId())
//				.append(oldDbInstance.getCluster(), dbInstance.getCluster())
//				.append(oldDbInstance.getPrimaryMaster(), dbInstance.getPrimaryMaster())
//				.append(oldDbInstance.getStartedAt(), dbInstance.getStartedAt())
//				.append(oldDbInstance.getSchedulerId(), dbInstance.getSchedulerId())
//				.append(oldDbInstance.getTimezone(), dbInstance.getTimezone())
//				.append(oldDbInstance.getUri(), dbInstance.getUri())
//				.append(oldDbInstance.getVersion(), dbInstance.getVersion());
//		return eb.isEquals();
//	}
	
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
	
	public static JobSchedulerState getJobSchedulerState(String state) {
	    return getJobSchedulerState(state, null);
	}

	public static JobSchedulerState getJobSchedulerState(String state, Boolean isActive) {
		// TODO which states we have in JS2?
	    if (isActive != null && !isActive && "running".equals(state)) {
	        state = "waiting_for_activation";
	    }
		JobSchedulerState jobSchedulerState = new JobSchedulerState();
		switch (state) {
		case "running":
			jobSchedulerState.set_text(JobSchedulerStateText.RUNNING);
			jobSchedulerState.setSeverity(0);
			break;
		case "waiting_for_activation":
			jobSchedulerState.set_text(JobSchedulerStateText.WAITING_FOR_ACTIVATION);
			jobSchedulerState.setSeverity(3);
			break;
		case "unreachable":
			jobSchedulerState.set_text(JobSchedulerStateText.UNREACHABLE);
			jobSchedulerState.setSeverity(2);
			break;
		}
		return jobSchedulerState;
	}

}
