
package com.sos.joc.db.deploy.items;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowBoards
    extends com.sos.controller.model.workflow.WorkflowBoardsV
{

    @JsonProperty("noticeBoardNames")
    private List<String> noticeBoardNames;
    
    /**
     * top level positions of Expect-/ConsumeNotices instructions
     * position -> list of board names
     * 
     */
    @JsonIgnore
    private Map<String, Set<String>> topLevelPositions;
    
    @JsonIgnore
    private Map<String, Set<String>> postPositions;
    @JsonIgnore
    private Map<String, Set<String>> expectPositions;
    @JsonIgnore
    private Map<String, Set<String>> consumePositions;

    @JsonProperty("noticeBoardNames")
    public List<String> getNoticeBoardNames() {
        return noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(List<String> noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
    }
    
    @JsonIgnore
    public Map<String, Set<String>> getTopLevelPositions() {
        return topLevelPositions;
    }

    @JsonIgnore
    public void setTopLevelPositions(Map<String, Set<String>> topLevelPositions) {
        this.topLevelPositions = topLevelPositions;
    }
    
    @JsonIgnore
    public Map<String, Set<String>> getPostPositions() {
        return postPositions;
    }

    @JsonIgnore
    public void setPostPositions(Map<String, Set<String>> postPositions) {
        this.postPositions = postPositions;
    }
    
    @JsonIgnore
    public Map<String, Set<String>> getExpectPositions() {
        return expectPositions;
    }

    @JsonIgnore
    public void setExpectPositions(Map<String, Set<String>> expectPositions) {
        this.expectPositions = expectPositions;
    }
    
    @JsonIgnore
    public Map<String, Set<String>> getConsumePositions() {
        return consumePositions;
    }

    @JsonIgnore
    public void setConsumePositions(Map<String, Set<String>> consumePositions) {
        this.consumePositions = consumePositions;
    }
    
    @JsonIgnore
    public boolean hasPostNotice(String boardName) {
        return getPostNotices() != null && getPostNotices().contains(boardName);
    }
    
    @JsonIgnore
    public boolean hasExpectNotice(String boardName) {
        return getExpectNotices() != null && getExpectNotices().contains(boardName);
    }
    
    @JsonIgnore
    public boolean hasConsumeNotice(String boardName) {
        return getConsumeNotices() != null && getConsumeNotices().contains(boardName);
    }
    
    @JsonIgnore
    public int hasPostNotice() {
        return getPostNotices() != null && !getPostNotices().isEmpty() ? 1 : 0;
    }
    
    @JsonIgnore
    public int hasExpectNotice() {
        return getExpectNotices() != null && !getExpectNotices().isEmpty() ? 2 : 0;
    }
    
    @JsonIgnore
    public int hasConsumeNotice() {
        return getConsumeNotices() != null && !getConsumeNotices().isEmpty() ? 4 : 0;
    }
    
    @JsonIgnore
    public int grouping() {
        return hasPostNotice() + hasExpectNotice() + hasConsumeNotice();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowBoards) == false) {
            return false;
        }
        WorkflowBoards rhs = ((WorkflowBoards) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
