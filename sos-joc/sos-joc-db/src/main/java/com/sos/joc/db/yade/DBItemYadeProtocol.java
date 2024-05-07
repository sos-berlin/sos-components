package com.sos.joc.db.yade;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_YADE_PROTOCOLS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[HOSTNAME]", "[PORT]", "[PROTOCOL]",
        "[ACCOUNT]" }) })
@SequenceGenerator(name = DBLayer.TABLE_YADE_PROTOCOLS_SEQUENCE, sequenceName = DBLayer.TABLE_YADE_PROTOCOLS_SEQUENCE, allocationSize = 1)
public class DBItemYadeProtocol extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_YADE_PROTOCOLS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[HOSTNAME]", nullable = false)
    private String hostname;

    @Column(name = "[PORT]", nullable = true)
    private Integer port;

    @Column(name = "[PROTOCOL]", nullable = false)
    private Integer protocol;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemYadeProtocol() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String val) {
        hostname = val;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer val) {
        port = val;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer val) {
        protocol = val;
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
