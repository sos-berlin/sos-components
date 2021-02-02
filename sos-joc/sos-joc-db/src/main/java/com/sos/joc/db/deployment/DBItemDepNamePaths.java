package com.sos.joc.db.deployment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_NAMEPATHS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[TYPE]", "[PATH]" }) })

public class DBItemDepNamePaths extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;
    
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public String getControllerId() {
        return controllerId;
    }
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

}
