package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.jobscheduler.model.command.overview.SystemProperties;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.model.jobscheduler.ClusterMemberType;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.JobScheduler;
import com.sos.joc.model.jobscheduler.JobSchedulerState;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;
import com.sos.joc.model.jobscheduler.OperatingSystem;

public class JobSchedulerAnswer extends JobScheduler {

	@JsonIgnore
	private final Overview json;
	@JsonIgnore
	private DBItemInventoryInstance dbInstance;
	@JsonIgnore
	private DBItemOperatingSystem dbOs;
	@JsonIgnore
	private boolean updateDbInstance = false;

	public JobSchedulerAnswer(Overview json, DBItemInventoryInstance dbInstance, DBItemOperatingSystem dbOs) {
		this.json = json;
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

	public void setFields() throws JobSchedulerInvalidResponseDataException {
		if (json != null) {
			if (!dbInstance.getSchedulerId().equals(json.getId())) {
				throw new JobSchedulerInvalidResponseDataException("unexpected JobSchedulerId " + json.getId());
			}
			setSurveyDate(Date.from(Instant.now()));
			setStartedAt(Date.from(Instant.ofEpochMilli(json.getStartedAt())));
			// TODO state is not in the answer
			setState(getJobSchedulerState("running"));
			dbOs.setHostname(json.getSystem().getHostname());
			SystemProperties systemProps = json.getJava().getSystemProperties();
			dbOs.setArchitecture(systemProps.getOs_arch());
			dbOs.setDistribution(systemProps.getOs_version());
			dbOs.setName(systemProps.getOs_name());
			
			if (!getStartedAt().equals(dbInstance.getStartedAt())) {
				dbInstance.setStartedAt(getStartedAt());
				updateDbInstance = true;
			}
			final String version = json.getVersion().split(" ", 2)[0];
			if (!version.equals(dbInstance.getVersion())) {
				dbInstance.setVersion(version);
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
		// TODO Cluster infos should be part of the answer too
		setClusterType(getClusterMemberType(dbInstance));
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
	
	public static ClusterMemberType getClusterMemberType(DBItemInventoryInstance dbInstance) {
		// TODO Cluster infos should be part of the answer too
		ClusterMemberType clusterMemberType = new ClusterMemberType();
		if (dbInstance.getIsCluster()) {
			clusterMemberType.set_type(ClusterType.PASSIVE);
			clusterMemberType.setPrecedence(dbInstance.getIsPrimaryMaster() ? 0 : 1);
			clusterMemberType.setUrl(dbInstance.getClusterUri());
	        clusterMemberType.setIsActive(dbInstance.getIsActive());
		} else {
			clusterMemberType.set_type(ClusterType.STANDALONE);
			clusterMemberType.setPrecedence(0);
		}
		return clusterMemberType;
	}

	public static JobSchedulerState getJobSchedulerState(String state) {
		// TODO which states we have in JS2?
		JobSchedulerState jobSchedulerState = new JobSchedulerState();
		switch (state) {
		case "starting":
			jobSchedulerState.set_text(JobSchedulerStateText.STARTING);
			jobSchedulerState.setSeverity(0);
			break;
		case "running":
			jobSchedulerState.set_text(JobSchedulerStateText.RUNNING);
			jobSchedulerState.setSeverity(0);
			break;
		case "paused":
			jobSchedulerState.set_text(JobSchedulerStateText.PAUSED);
			jobSchedulerState.setSeverity(1);
			break;
		case "stopping":
		case "stopping_let_run":
		case "stopped":
			jobSchedulerState.set_text(JobSchedulerStateText.TERMINATING);
			jobSchedulerState.setSeverity(3);
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
