package com.sos.joc.db.pgp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_KEYS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ACCOUNT]" }) })
@SequenceGenerator(name = DBLayer.TABLE_DEP_KEYS_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_KEYS_SEQUENCE, allocationSize = 1)
public class DBItemDepKeys extends DBItem {

    private static final long serialVersionUID = 5376577176035147194L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_KEYS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /* 0=default(privKey), 1=private, 2=public */
    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[KEY]", nullable = false)
    private String key;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
