
package com.sos.joc.model.docu;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Documentations filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentations",
    "folders",
    "types",
    "onlyWithAssignReference",
    "auditLog"
})
public class DocumentationsFilter {

    @JsonProperty("documentations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> documentations = new LinkedHashSet<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("types")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> types = new LinkedHashSet<String>();
    @JsonProperty("onlyWithAssignReference")
    private Boolean onlyWithAssignReference = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("documentations")
    public Set<String> getDocumentations() {
        return documentations;
    }

    @JsonProperty("documentations")
    public void setDocumentations(Set<String> documentations) {
        this.documentations = documentations;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @JsonProperty("types")
    public Set<String> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(Set<String> types) {
        this.types = types;
    }

    @JsonProperty("onlyWithAssignReference")
    public Boolean getOnlyWithAssignReference() {
        return onlyWithAssignReference;
    }

    @JsonProperty("onlyWithAssignReference")
    public void setOnlyWithAssignReference(Boolean onlyWithAssignReference) {
        this.onlyWithAssignReference = onlyWithAssignReference;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("documentations", documentations).append("folders", folders).append("types", types).append("onlyWithAssignReference", onlyWithAssignReference).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(folders).append(auditLog).append(documentations).append(onlyWithAssignReference).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DocumentationsFilter) == false) {
            return false;
        }
        DocumentationsFilter rhs = ((DocumentationsFilter) other);
        return new EqualsBuilder().append(types, rhs.types).append(folders, rhs.folders).append(auditLog, rhs.auditLog).append(documentations, rhs.documentations).append(onlyWithAssignReference, rhs.onlyWithAssignReference).isEquals();
    }

}
