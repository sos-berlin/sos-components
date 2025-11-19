package com.sos.joc.db.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.RequestItem;

public class Dependency {

    Long id;
    String name;
    ConfigurationType type;
    String folder;
    private Boolean valid;
    private Boolean deployed;
    private Boolean released;
    private Boolean enforce = false;
    
    public Dependency(RequestItem requestItem) {
        this.id = null;
        this.name = requestItem.getName();
        this.type = requestItem.getType();
        this.folder = null;
        this.valid = null;
        this.deployed = null;
        this.released = null;
    }
    public Dependency(Long id, String name, ConfigurationType type, String folder, Boolean valid, Boolean deployed, Boolean released, Boolean enforce) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.folder = folder;
        this.valid = valid;
        this.deployed = deployed;
        this.released = released;
        this.enforce = enforce;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public ConfigurationType getType() {
        return type;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public Boolean getValid() {
        return valid;
    }
    
    public Boolean getDeployed() {
        return deployed;
    }
    
    public Boolean getReleased() {
        return released;
    }
    
    public Boolean getEnforce() {
        return enforce;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }
    
    public RequestItem getRequestItem () {
        RequestItem item = new RequestItem();
        item.setName(name);
        item.setType(type);
        return item;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dependency) == false) {
            return false;
        }
        Dependency rhs = ((Dependency) other);
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }
    
    public boolean equalsWithoutId(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dependency) == false) {
            return false;
        }
        Dependency rhs = ((Dependency) other);
        return new EqualsBuilder().append(name, rhs.name).append(type, rhs.type).isEquals();
    }
}
