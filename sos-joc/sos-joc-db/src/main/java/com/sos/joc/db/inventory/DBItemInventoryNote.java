package com.sos.joc.db.inventory;

import java.util.Date;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.note.common.Severity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_NOTES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID]" }) })
@Proxy(lazy = false)
public class DBItemInventoryNote extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_NOTES_SEQUENCE)
    private Long id;

    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

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

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }
    
    public Integer getSeverity() {
        return severity;
    }

    @Transient
    public Severity getSeverityAsEnum() {
        return Severity.fromValue(severity);
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }
    
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }
    
    @Transient
    public Stream<String> getMentionedUsers() {
        return Pattern.compile("@\\[[^\\]]+\\]|@[^\\s\"]+").matcher(content).results().map(MatchResult::group).map(s -> s.replaceFirst("^@\\[?\\s*",
                "")).map(s -> s.replaceFirst("\\s*]?$", "")).map(StringEscapeUtils::unescapeHtml4).distinct();
    }

}
