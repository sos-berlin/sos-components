package com.sos.joc.publish.mapper;

import java.util.Map;
import java.util.Set;

import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.model.tag.ExportedTags;

public class ArchiveValues {

  private Set<ConfigurationObject> configurations;
  private ExportedTags tags;
  private Map<ControllerObject, SignaturePath> configurationWithSignatures;
  
  public Set<ConfigurationObject> getConfigurations() {
    return configurations;
  }
  
  public void setConfigurations(Set<ConfigurationObject> configurations) {
    this.configurations = configurations;
  }
  
  public ExportedTags getTags() {
    return tags;
  }
  
  public void setTags(ExportedTags tags) {
    this.tags = tags;
  }

  
  public Map<ControllerObject, SignaturePath> getConfigurationWithSignatures() {
    return configurationWithSignatures;
  }

  
  public void setConfigurationWithSignatures(Map<ControllerObject, SignaturePath> configurationWithSignatures) {
    this.configurationWithSignatures = configurationWithSignatures;
  }
  
}
