package com.sos.joc.db.documentation;

import java.sql.Types;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES)
@SequenceGenerator(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE, allocationSize = 1)
public class DBItemDocumentationImage extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[IMAGE]", nullable = false)
    // TODO 6.4.5.Final
    // @Type(type = "org.hibernate.type.BinaryType")
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
