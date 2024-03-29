package com.sos.joc.db.documentation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES)
@SequenceGenerator(name = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE, allocationSize = 1)
public class DBItemDocumentationImage extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[IMAGE]", nullable = false)
    @Type(type = "org.hibernate.type.BinaryType")
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
