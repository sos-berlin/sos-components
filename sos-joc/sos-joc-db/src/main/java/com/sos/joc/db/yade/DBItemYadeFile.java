package com.sos.joc.db.yade;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.YadeConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_YADE_FILES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TRANSFER_ID]", "[SOURCE_PATH]" }) })
@Proxy(lazy = false)
public class DBItemYadeFile extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_YADE_FILES_SEQUENCE)
    private Long id;

    /** Foreign key - TABLE YADE_TRANSFERS.ID, KEY */
    @Column(name = "[TRANSFER_ID]", nullable = false)
    private Long transferId;

    @Column(name = "[SOURCE_PATH]", nullable = false)
    private String sourcePath;

    @Column(name = "[TARGET_PATH]", nullable = true)
    private String targetPath;

    @Column(name = "[SIZE]", nullable = true)
    private Long size;

    @Column(name = "[MODIFICATION_DATE]", nullable = true)
    private Date modificationDate;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[INTEGRITY_HASH]", nullable = true)
    private String integrityHash;

    @Column(name = "[ERROR_MESSAGE]", nullable = true)
    private String errorMessage;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemYadeFile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long val) {
        transferId = val;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String val) {
        sourcePath = val;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String val) {
        targetPath = val;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long val) {
        size = val;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date val) {
        modificationDate = val;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer val) {
        state = val;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    public void setIntegrityHash(String val) {
        integrityHash = val;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String val) {
        errorMessage = normalizeErrorText(val);
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, YadeConstants.MAX_LEN_ERROR_MESSAGE);
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}