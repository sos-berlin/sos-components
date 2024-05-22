package com.sos.joc.db.joc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

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
@Table(name = DBLayer.TABLE_JOC_LOCKS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[RANGE]", "[FOLDER]" }) })
@Proxy(lazy = false)
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
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_LOCKS_SEQUENCE)
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