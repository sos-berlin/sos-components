package com.sos.joc.db.inventory.items;

public class InventorySearchItem {

    private Long id;
    private String path;
    private String folder;
    private String name;
    private String title;
    private String controllerId;
    private boolean valid;
    private boolean deleted;
    private boolean deployed;
    private boolean released;
    private Number countDeployed;
    private Number countReleased;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean val) {
        deleted = val;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean val) {
        released = val;
    }

    public Number getCountDeployed() {
        return countDeployed;
    }

    public void setCountDeployed(Number val) {
        countDeployed = val;
    }

    public Number getCountReleased() {
        return countReleased;
    }

    public void setCountReleased(Number val) {
        countReleased = val;
    }
}
