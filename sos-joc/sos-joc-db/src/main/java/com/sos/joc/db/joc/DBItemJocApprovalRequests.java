package com.sos.joc.db.joc;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.RequestorState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_JOC_APPROVAL_REQUESTS)
@Proxy(lazy = false)
public class DBItemJocApprovalRequests extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_APPROVAL_REQUESTS_SEQUENCE)
    private Long id;

    @Column(name = "[REQUESTOR]", nullable = false)
    private String requestor;

    @Column(name = "[REQUEST]", nullable = false)
    private String request;

    @Column(name = "[PARAMETERS]", nullable = true)
    private String parameters;

    @Column(name = "[REQUESTOR_STATUS]", nullable = false)
    private Integer requestorState;

    @Column(name = "[APPROVER]", nullable = true)
    private String approver;

    @Column(name = "[APPROVER_STATUS]", nullable = false)
    private Integer approverState;
    
    @Column(name = "[CATEGORY]", nullable = false)
    private Integer category;
    
    @Column(name = "[ACTION]", nullable = false)
    private String action;
    
    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private Integer objectType;
    
    @Column(name = "[OBJECT_NAME]", nullable = true)
    private String objectName;

    @Column(name = "[NUM_OF_OBJECTS]", nullable = true)
    private Integer numOfObjects;

    @Column(name = "[TITLE]", nullable = false)
    private String title;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public void setRequestor(String val) {
        requestor = val;
    }

    public String getRequestor() {
        return requestor;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String val) {
        approver = val;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String val) {
        request = val;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String val) {
        parameters = val;
    }

    public Integer getRequestorState() {
        return requestorState;
    }

    @Transient
    public RequestorState getRequestorStateAsEnum() {
        try {
            return RequestorState.fromValue(requestorState);
        } catch (Exception e) {
            return RequestorState.REQUESTED;
        }
    }

    public void setRequestorState(Integer val) {
        requestorState = val;
    }
    
    public Integer getApproverState() {
        return approverState;
    }

    @Transient
    public ApproverState getApproverStateAsEnum() {
        try {
            return ApproverState.fromValue(approverState);
        } catch (Exception e) {
            return ApproverState.OPEN;
        }
    }

    public void setApproverState(Integer val) {
        approverState = val;
    }
    
    public Integer getCategory() {
        return category;
    }

    @Transient
    public CategoryType getTypeAsEnum() {
        try {
            return CategoryType.fromValue(category);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCategory(Integer val) {
        category = val;
    }
    
    public String getAction() {
        return action;
    }

    public void setAction(String val) {
        action = val;
    }

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer val) {
        objectType = val;
    }
    
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String val) {
        objectName = val;
    }
    
    public Integer getNumOfObjects() {
        return numOfObjects;
    }

    public void setNumOfObjects(Integer val) {
        numOfObjects = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String val) {
        comment = val;
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
}