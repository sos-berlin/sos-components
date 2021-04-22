package com.sos.joc.publish.mapper;

import java.util.Set;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.ConfigurationObject;

public class UpdateableConfigurationObject {

	private ConfigurationObject configurationObject;
	private DBItemInventoryConfiguration configurationDbItem;
	private String oldName;
	private String newName;
	private Set<ConfigurationObject> referencedBy;
	private String targetFolder;
	
	public UpdateableConfigurationObject(ConfigurationObject configuration, DBItemInventoryConfiguration configurationDbItem, String oldName,
			String newName, Set<ConfigurationObject> referencedBy, String targetFolder) {
		this.configurationObject = configuration;
		this.configurationDbItem = configurationDbItem;
		this.oldName = oldName;
		this.newName = newName;
		this.referencedBy = referencedBy;
		this.targetFolder = targetFolder;
	}

	public ConfigurationObject getConfigurationObject() {
		return configurationObject;
	}

	public String getOldName() {
		return oldName;
	}

	public String getNewName() {
		return newName;
	}
	
	public Set<ConfigurationObject> getReferencedBy() {
		return referencedBy;
	}

	public DBItemInventoryConfiguration getConfigurationDbItem() {
		return configurationDbItem;
	}

	public String getTargetFolder() {
		return targetFolder;
	}

}
