package com.sos.joc.db.inventory;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.favorite.FavoriteType;

@Entity
@Table(name = DBLayer.TABLE_INV_FAVORITES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TYPE]", "[NAME]", "[ACCOUNT]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_FAVORITES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_FAVORITES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryFavorite extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_FAVORITES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[FAVORITE]", nullable = true)
    private String favorite;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[SHARED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean shared;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public FavoriteType getTypeAsEnum() {
        try {
            return FavoriteType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(FavoriteType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String val) {
        favorite = val;
    }
    
    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        ordering = val;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        account = val;
    }
    
    public boolean getShared() {
        return shared;
    }

    public void setShared(boolean val) {
        shared = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

}
