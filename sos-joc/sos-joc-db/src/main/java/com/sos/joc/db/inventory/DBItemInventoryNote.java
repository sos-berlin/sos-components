package com.sos.joc.db.inventory;

import java.time.Instant;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

import com.sos.commons.hibernate.annotations.SOSCreationTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.note.common.Severity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_NOTES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID]" }) })
public class DBItemInventoryNote extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_INV_NOTES_SEQUENCE)
    private Long id;

    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[CREATED]", nullable = false)
    @SOSCreationTimestampUtc
    private Instant created;

    @Column(name = "[MODIFIED]", nullable = false)
    @SOSCurrentTimestampUtc
    private Instant modified;

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
        try {
            return Severity.fromValue(severity);
        } catch (Exception e) {
            return Severity.NORMAL;
        }
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public Instant getModified() {
        return modified;
    }

    public Instant getCreated() {
        return created;
    }

    @Transient
    public Stream<String> getMentionedUsers() {
        return Pattern.compile("@\\[[^\\]]+\\]|@[^\\s\"]+").matcher(content).results().map(MatchResult::group).map(s -> s.replaceFirst("^@\\[?\\s*",
                "")).map(s -> s.replaceFirst("\\s*]?$", "")).map(StringEscapeUtils::unescapeHtml4).distinct();
    }

}
