package com.sos.joc.db.documentation;

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

@Entity
@Table(name = DBLayer.TABLE_DOCUMENTATION, uniqueConstraints = { @UniqueConstraint(columnNames = { "[PATH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_DOCUMENTATION_SEQUENCE, sequenceName = DBLayer.TABLE_DOCUMENTATION_SEQUENCE, allocationSize = 1)
public class DBItemDocumentation extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[TYPE]", nullable = false)
    private String type;

    @Column(name = "[DOC_REF]", nullable = true)
    private String docRef;

    @Column(name = "[IS_REF]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isRef;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[IMAGE_ID]", nullable = true)
    private Long imageId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    @Transient
    private byte[] image;

    @Transient
    private boolean hasImage = false;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        this.id = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        this.name = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        this.folder = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        this.path = val;
    }

    public String getType() {
        return type;
    }

    public void setType(String val) {
        this.type = val;
    }
    
    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String val) {
        this.docRef = val;
    }
    
    public boolean getIsRef() {
        return isRef;
    }

    public void setIsRef(boolean val) {
        isRef = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        this.content = val;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long val) {
        this.imageId = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        this.modified = val;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

}
