package com.sos.joc.cleanup.db;

public class DeploymentVersion {

    private Long maxId;
    private Long countVersions;
    private Long inventoryId;
    private String controllerId;

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long val) {
        maxId = val;
    }

    public Long getCountVersions() {
        return countVersions;
    }

    public void setCountVersions(Long val) {
        countVersions = val;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long val) {
        inventoryId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }
}
