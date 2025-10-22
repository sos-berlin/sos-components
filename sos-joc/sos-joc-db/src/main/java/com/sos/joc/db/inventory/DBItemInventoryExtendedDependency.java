package com.sos.joc.db.inventory;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.Dependency;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_EXTENDED_DEPENDENCIES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[INV_ID]", "[DEP_ID]" }) })
@Proxy(lazy = false)
public class DBItemInventoryExtendedDependency extends DBItem {
/*
 * inva.ID AS INV_ID, inva.NAME AS INV_NAME, inva.FOLDER AS INV_FOLDER, inva.TYPE AS INV_TYPE, 
 * inva.VALID AS INV_VALID, inva.DEPLOYED AS INV_DEPLOYED, inva.RELEASED AS INV_RELEASED,
 * 
 * invb.ID AS DEP_ID, invb.NAME AS DEP_NAME, invb.FOLDER AS DEP_FOLDER, invb.TYPE AS DEP_TYPE, 
 * invb.VALID AS DEP_VALID, invb.DEPLOYED AS DEP_DEPLOYED, invb.RELEASED AS DEP_RELEASED,
 * */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[INV_ID]", nullable = false)
    private Long invId;
    @Column(name = "[INV_NAME]", nullable = false)
    private String invName;
    @Column(name = "[INV_FOLDER]", nullable = false)
    private String invFolder;
    @Column(name = "[INV_TYPE]", nullable = false)
    private Integer invType;
    @Column(name = "[INV_VALID]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean invValid;
    @Column(name = "[INV_DEPLOYED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean invDeployed;
    @Column(name = "[INV_RELEASED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean invReleased;
    
    @Column(name = "[DEP_ID]", nullable = true)
    private Long depId;
    @Column(name = "[DEP_NAME]", nullable = true)
    private String depName;
    @Column(name = "[DEP_FOLDER]", nullable = true)
    private String depFolder;
    @Column(name = "[DEP_TYPE]", nullable = true)
    private Integer depType;
    @Column(name = "[DEP_VALID]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean depValid;
    @Column(name = "[DEP_DEPLOYED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean depDeployed;
    @Column(name = "[DEP_RELEASED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean depReleased;
    
    public Long getInvId() {
        return invId;
    }
    public void setInvId(Long invId) {
        this.invId = invId;
    }
    
    public String getInvName() {
        return invName;
    }
    public void setInvName(String invName) {
        this.invName = invName;
    }
    
    public String getInvFolder() {
        return invFolder;
    }
    public void setInvFolder(String invFolder) {
        this.invFolder = invFolder;
    }
    
    public Integer getInvType() {
        return invType;
    }
    public void setInvType(Integer invType) {
        this.invType = invType;
    }
    
    public Boolean isInvValid() {
        return invValid;
    }
    public void setInvValid(Boolean invValid) {
        this.invValid = invValid;
    }
    
    public Boolean isInvDeployed() {
        return invDeployed;
    }
    public void setInvDeployed(Boolean invDeployed) {
        this.invDeployed = invDeployed;
    }
    
    public Boolean isInvReleased() {
        return invReleased;
    }
    public void setInvReleased(Boolean invReleased) {
        this.invReleased = invReleased;
    }
    
    public Long getDepId() {
        return depId;
    }
    public void setDepId(Long depId) {
        this.depId = depId;
    }
    
    public String getDepName() {
        return depName;
    }
    public void setDepName(String depName) {
        this.depName = depName;
    }
    
    public String getDepFolder() {
        return depFolder;
    }
    public void setDepFolder(String depFolder) {
        this.depFolder = depFolder;
    }
    
    public Integer getDepType() {
        return depType;
    }
    public void setDepType(Integer depType) {
        this.depType = depType;
    }
    
    public Boolean isDepValid() {
        return depValid;
    }
    public void setDepValid(Boolean depValid) {
        this.depValid = depValid;
    }
    
    public Boolean isDepDeployed() {
        return depDeployed;
    }
    public void setDepDeployed(Boolean depDeployed) {
        this.depDeployed = depDeployed;
    }
    
    public Boolean isDepReleased() {
        return depReleased;
    }
    public void setDepReleased(Boolean depReleased) {
        this.depReleased = depReleased;
    }
    
    
    @Transient
    public String getInvPathAsString() {
        Path path = getInvPath();
        if(path != null) {
            return path.toString().replace('\\', '/');
        } else {
            return "";
        }
    }
    
    @Transient
    public Path getInvPath() {
        Path path = null;
        if(invFolder != null) {
            path = Paths.get(invFolder);
        }
        if(invName != null) {
            if(path != null) {
                path = path.resolve(invName);
            } else {
                path = Paths.get(invName);
            }
        }
        return path;
    }
    
    @Transient
    public String getDepPath() {
        return invFolder;
    }
    
    @Transient
    public Dependency getDependency() {
        return new Dependency(invId, invName, getTypeAsEnum(invType), invFolder, invValid, invDeployed, invReleased);
    }
    
    @Transient
    public Dependency getReference() {
        return new Dependency(depId, depName, getTypeAsEnum(depType), depFolder, depValid, depDeployed, depReleased);
    }
    
    @Transient
    public ConfigurationType getTypeAsEnum(int type) {
        try {
            return ConfigurationType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    @Transient
    public void setInvType(ConfigurationType val) {
        setInvType(val == null ? null : val.intValue());
    }
    
    @Transient
    public void setDepType(ConfigurationType val) {
        setDepType(val == null ? null : val.intValue());
    }

    
}
