
package com.sos.joc.model.board;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notice boards request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "planSchemaIds",
    "noticeSpaceKeys",
    "noticeBoardPaths",
    "folders",
    "compact",
    "limit"
})
public class BoardsFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("planSchemaIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> planSchemaIds = new LinkedHashSet<String>();
    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Will be ignored for global schema because it has no plan keys")
    @JsonAlias({
        "planKeys"
    })
    private Set<String> noticeSpaceKeys = new LinkedHashSet<String>();
    @JsonProperty("noticeBoardPaths")
    private List<String> noticeBoardPaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("-1=unlimited")
    private Integer limit = 10000;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("planSchemaIds")
    public Set<String> getPlanSchemaIds() {
        return planSchemaIds;
    }

    @JsonProperty("planSchemaIds")
    public void setPlanSchemaIds(Set<String> planSchemaIds) {
        this.planSchemaIds = planSchemaIds;
    }

    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    public Set<String> getNoticeSpaceKeys() {
        return noticeSpaceKeys;
    }

    /**
     * Will be ignored for global schema because it has no plan keys
     * 
     */
    @JsonProperty("noticeSpaceKeys")
    public void setNoticeSpaceKeys(Set<String> noticeSpaceKeys) {
        this.noticeSpaceKeys = noticeSpaceKeys;
    }

    @JsonProperty("noticeBoardPaths")
    public List<String> getNoticeBoardPaths() {
        return noticeBoardPaths;
    }

    @JsonProperty("noticeBoardPaths")
    public void setNoticeBoardPaths(List<String> noticeBoardPaths) {
        this.noticeBoardPaths = noticeBoardPaths;
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

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("planSchemaIds", planSchemaIds).append("noticeSpaceKeys", noticeSpaceKeys).append("noticeBoardPaths", noticeBoardPaths).append("folders", folders).append("compact", compact).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planSchemaIds).append(folders).append(controllerId).append(compact).append(noticeBoardPaths).append(limit).append(noticeSpaceKeys).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoardsFilter) == false) {
            return false;
        }
        BoardsFilter rhs = ((BoardsFilter) other);
        return new EqualsBuilder().append(planSchemaIds, rhs.planSchemaIds).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(noticeBoardPaths, rhs.noticeBoardPaths).append(limit, rhs.limit).append(noticeSpaceKeys, rhs.noticeSpaceKeys).isEquals();
    }

}
