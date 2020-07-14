package com.sos.joc.db.joc;

import java.beans.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_LOCKS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[RANGE]", "[FOLDER]" }) })
@SequenceGenerator(name = DBLayer.TABLE_JOC_LOCKS_SEQUENCE, sequenceName = DBLayer.TABLE_JOC_LOCKS_SEQUENCE, allocationSize = 1)
public class DBItemJocLock extends DBItem {

    private static final long serialVersionUID = 1L;

    public enum LockRange {
        INVENTORY(1L);

        private final Long value;
        private final static Map<Long, LockRange> CONSTANTS = new HashMap<Long, LockRange>();

        static {
            for (LockRange c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LockRange(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static LockRange fromValue(Long value) {
            if (value == null) {
                return null;
            }
            LockRange constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_LOCKS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[RANGE]", nullable = false)
    private Long range;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getRange() {
        return range;
    }

    @Transient
    public LockRange getRangeAsEnum() {
        return LockRange.fromValue(range);
    }

    public void setRange(Long val) {
        range = val;
    }

    @Transient
    public void setRange(LockRange val) {
        setRange(val == null ? null : val.value());
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        account = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}