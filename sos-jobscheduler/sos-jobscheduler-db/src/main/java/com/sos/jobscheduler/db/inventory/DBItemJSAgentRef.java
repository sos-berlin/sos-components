package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_AGENT_REF, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[URI]", "[PATH]", "[VERSION]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_JS_AGENT_REF_SEQUENCE, 
		sequenceName = DBLayer.TABLE_JS_AGENT_REF_SEQUENCE, 
		allocationSize = 1)
public class DBItemJSAgentRef extends DBItem {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JS_AGENT_REF_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[VERSION]", nullable = false)
    private Integer version;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(uri).append(path).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemJSAgentRef)) {
            return false;
        }
        DBItemJSAgentRef rhs = ((DBItemJSAgentRef) other);
        return new EqualsBuilder().append(uri, rhs.uri).append(path, rhs.path).append(version, rhs.version).isEquals();
    }

}
