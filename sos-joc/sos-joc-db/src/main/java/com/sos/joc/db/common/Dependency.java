package com.sos.joc.db.common;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.RequestItem;

public class Dependency extends RequestItem {

    Long id;
    String folder;
    private Boolean valid;
    private Boolean deployed;
    private Boolean released;
    private Boolean enforce = false;
    
    public Dependency() {
        //
    }
    
    public Dependency(Long id, String name, ConfigurationType type, String folder, Boolean valid, Boolean deployed, Boolean released, Boolean enforce) {
        this.id = id;
        setName(name);
        setType(type);
        this.folder = folder;
        this.valid = valid;
        this.deployed = deployed;
        this.released = released;
        this.enforce = enforce;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public String getPath() {
        if (getName() == null || folder == null) {
           return null; 
        }
        return (folder + "/" + getName()).replace("//", "/");
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
    
//    @Override
//    public int hashCode() {
//        //return new HashCodeBuilder().append(id).toHashCode();
//        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
//    }
    
//    @Override
//    public boolean equals(Object other) {
//        if (other == this) {
//            return true;
//        }
//        if ((other instanceof Dependency) == false) {
//            return false;
//        }
//        Dependency rhs = ((Dependency) other);
//        //return new EqualsBuilder().append(id, rhs.id).isEquals();
//        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
//    }
    
//    public boolean equalsWithoutId(Object other) {
//        if (other == this) {
//            return true;
//        }
//        if ((other instanceof Dependency) == false) {
//            return false;
//        }
//        Dependency rhs = ((Dependency) other);
//        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
//    }
}
