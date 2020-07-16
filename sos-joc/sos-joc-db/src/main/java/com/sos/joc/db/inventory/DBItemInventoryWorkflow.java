package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOWS)
public class DBItemInventoryWorkflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CONFIG_ID]", nullable = false)
    private Long configId;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[CONTENT_SIGNED]", nullable = true)
    private String contentSigned;

    @Column(name = "[CONTENT_JOC]", nullable = false)
    private String contentJoc;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long val) {
        configId = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }

    public String getContentSigned() {
        return contentSigned;
    }

    public void setContentSigned(String val) {
        contentSigned = val;
    }

    public String getContentJoc() {
        return contentJoc;
    }

    public void setContentJoc(String val) {
        contentJoc = val;
    }
}
