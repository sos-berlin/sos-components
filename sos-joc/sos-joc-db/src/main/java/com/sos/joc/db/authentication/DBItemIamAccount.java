package com.sos.joc.db.authentication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_ACCOUNTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_ID]", "[ACCOUNT_NAME]" }) })
		
@SequenceGenerator(name = DBLayer.TABLE_IAM_ACCOUNTS_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_ACCOUNTS_SEQUENCE, allocationSize = 1)

public class DBItemIamAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_ACCOUNTS_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]", nullable = false)
    private Long identityServiceId;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Column(name = "[ACCOUNT_PASSWORD]", nullable = false)
    private String accountPassword;

    @Column(name = "[FORCE_PASSWORD_CHANGE]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean forcePasswordChange;

    @Column(name = "[DISABLED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean disabled;

    public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public DBItemIamAccount() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

}
