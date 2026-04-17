package com.sos.joc.db.authentication;

import java.time.Instant;

import com.sos.commons.hibernate.annotations.SOSCreationTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_IAM_FIDO2_REQUESTS)
public class DBItemIamFido2Requests {

    @Id
    @Column(name = "[ID]")
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_IAM_FIDO2_REQUESTS_SEQUENCE)
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]", nullable = false)
    private Long identityServiceId;

    @Column(name = "[CHALLENGE]", nullable = false)
    private String challenge;

    @Column(name = "[REQUEST_ID]", nullable = false)
    private String requestId;

    @Column(name = "[CREATED]", nullable = false)
    @SOSCreationTimestampUtc
    private Instant created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Instant getCreated() {
        return created;
    }

}
