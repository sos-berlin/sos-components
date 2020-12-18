package com.sos.joc.db.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_LOGS)
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_LOGS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_LOGS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryLog extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_LOGS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private Long mainOrderId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private Long orderId;

    @Column(name = "[ORDER_STEP_ID]", nullable = false)
    private Long orderStepId;

    @Column(name = "[COMPRESSED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean compressed;

    @Column(name = "[FILE_BASENAME]", nullable = false)
    private String fileBasename;

    @Column(name = "[FILE_SIZE_UNCOMPRESSED]", nullable = false)
    private Long fileSizeUncompressed;

    @Column(name = "[FILE_LINES_UNCOMPRESSED]", nullable = false)
    private Long fileLinesUncompressed;

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "[FILE_CONTENT]", nullable = false)
    private byte[] fileContent;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public Long geMainOrdertId() {
        return mainOrderId;
    }

    public void setMainOrderId(Long val) {
        mainOrderId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
    }

    public Long getOrderStepId() {
        return orderStepId;
    }

    public void setOrderStepId(Long val) {
        orderStepId = val;
    }

    public void setCompressed(boolean val) {
        compressed = val;
    }

    public boolean getCompressed() {
        return compressed;
    }

    public String getFileBasename() {
        return fileBasename;
    }

    public void setFileBasename(String val) {
        fileBasename = val;
    }

    public Long getFileSizeUncomressed() {
        return fileSizeUncompressed;
    }

    public void setFileSizeUncomressed(Long val) {
        fileSizeUncompressed = val;
    }

    public Long getFileLinesUncomressed() {
        return fileLinesUncompressed;
    }

    public void setFileLinesUncomressed(Long val) {
        fileLinesUncompressed = val;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] val) {
        fileContent = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    @Transient
    public boolean fileContentIsNull() {
        return fileContent == null;
    }
}
