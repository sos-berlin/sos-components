package com.sos.jobscheduler.db.documentation;

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

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DOCUMENTATION,
       uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]","[PATH]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_DOCUMENTATION_SEQUENCE,
		sequenceName = DBLayer.TABLE_DOCUMENTATION_SEQUENCE,
		allocationSize = 1)
public class DBItemDocumentation extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;
    
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;
    
    @Column(name = "[NAME]", nullable = false)
    private String name;
    
    @Column(name = "[DIRECTORY]", nullable = false)
    private String directory;
    
    @Column(name = "[PATH]", nullable = false)
    private String path;
    
    @Column(name = "[TYPE]", nullable = false)
    private String type;    
    
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
    
    private byte[] image;
    private boolean hasImage = false;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long val) {
        this.id = val;
    }
    
    public String getSchedulerId() {
        return schedulerId;
    }
    
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String val) {
        this.name = val;
    }
    
    public String getDirectory() {
        return directory;
    }
    
    public void setDirectory(String val) {
        this.directory = val;
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
    
    @Transient
    public byte[] image() {
        return image;
    }
    
    @Transient
    public void setImage(byte[] image) {
        this.image = image;
    }
    
    @Transient
    public boolean hasImage() {
        return hasImage;
    }
    
    @Transient
    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
    
}
