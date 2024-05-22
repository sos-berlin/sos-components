package com.sos.joc.db.documentation;

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES)
@Proxy(lazy = false)
public class DBItemDocumentationImage extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE)
    private Long id;

    @Column(name = "[IMAGE]", nullable = false)
    @JdbcTypeCode(Types.BINARY)
    private byte[] image;

    @Column(name = "[MD5_HASH]", nullable = false)
    private String md5Hash;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

}
