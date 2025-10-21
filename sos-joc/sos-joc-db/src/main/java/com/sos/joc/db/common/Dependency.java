package com.sos.joc.db.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;

public class Dependency {

    Long id;
    String name;
    Integer type;
    String folder;
    private Boolean valid;
    private Boolean deployed;
    private Boolean released;
    
    public Dependency(Long id, String name, Integer type, String folder, Boolean valid, Boolean deployed, Boolean released) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.folder = folder;
        this.valid = valid;
        this.deployed = deployed;
        this.released = released;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getType() {
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
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
    
}
