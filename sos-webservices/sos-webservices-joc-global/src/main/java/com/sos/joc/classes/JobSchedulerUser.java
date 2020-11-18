package com.sos.joc.classes;

import java.util.Set;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.auth.rest.SOSShiroSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;

public class JobSchedulerUser {

	private String accessToken;
	private SOSShiroCurrentUser sosShiroCurrentUser;

	public JobSchedulerUser(String accessToken) {
		super();
		this.accessToken = accessToken;
	}

	public SOSShiroCurrentUser getSosShiroCurrentUser() throws SessionNotExistException {
		if (sosShiroCurrentUser == null && Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
			sosShiroCurrentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
		}
		if (sosShiroCurrentUser == null) {
			throw new SessionNotExistException("Session doesn't exist");
		}
		return sosShiroCurrentUser;
	}

	public boolean isAuthenticated() {
		if (sosShiroCurrentUser == null && Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
			sosShiroCurrentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
		}
		return (sosShiroCurrentUser != null);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public DBItemInventoryJSInstance getSchedulerInstance(String controllerId) throws JocException {
		SOSHibernateSession session = null;
		try {
			if (getSosShiroCurrentUser().getSchedulerInstanceDBItem(controllerId) == null) {
				session = Globals.createSosHibernateStatelessConnection("getSchedulerInstance");
				InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(session);
				Globals.beginTransaction(session);
				getSosShiroCurrentUser().addSchedulerInstanceDBItem(controllerId,
						dbLayer.getInventoryInstanceByControllerId(controllerId, getAccessToken()));
			}
		} finally {
			Globals.rollback(session);
			Globals.disconnect(session);
		}
		return getSosShiroCurrentUser().getSchedulerInstanceDBItem(controllerId);
	}

	public boolean resetTimeOut() throws SessionNotExistException {

		if (sosShiroCurrentUser != null) {
			SOSShiroSession sosShiroSession = new SOSShiroSession(sosShiroCurrentUser);
			sosShiroSession.touch();
		} else {
			throw new org.apache.shiro.session.InvalidSessionException("Session doesn't exist");
		}

		return (sosShiroCurrentUser != null);
	}

	public void setJocJsonCommands(Set<JOCJsonCommand> jocJsonCommands) {
		sosShiroCurrentUser.setJocJsonCommands(jocJsonCommands);
	}

}
