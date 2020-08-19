package com.sos.joc.db.inventory.items;

public class InventoryTreeFolderItem {

    private Long id;
    private Integer type;
    private String name;
    private String title;
    private boolean valide;
    private boolean deleted;
    private boolean deployed;
    private Long countDeployments;

    public InventoryTreeFolderItem(Long id, Integer type, String name, String title, boolean valide, boolean deleted, boolean deployed,
            Long countDeployments) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.title = title;
        this.valide = valide;
        this.deleted = deleted;
        this.deployed = deployed;
        this.countDeployments = countDeployments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
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

    public boolean getValide() {
        return valide;
    }

    public void setValide(boolean val) {
        valide = val;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean val) {
        deleted = val;
    }

    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public Long getCountDeployments() {
        return countDeployments;
    }

    public void setCountDeployments(Long val) {
        countDeployments = val;
    }

}
