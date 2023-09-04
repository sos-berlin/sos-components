package com.sos.joc.db.dailyplan;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DPL_PROJECTIONS)
public class DBItemDailyPlanProjection extends DBItem {

    private static final long serialVersionUID = 1L;

    public static final Long METADATEN_ID = 0L;

    @Id
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}