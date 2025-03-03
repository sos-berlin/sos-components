
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
import com.sos.controller.model.workflow.WorkflowIdAndTags;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowBoards
    extends WorkflowIdAndTags
{

    @JsonProperty("postNotices")
    private List<String> postNotices;
    
    @JsonProperty("expectNotices")
    private List<String> expectNotices;

    @JsonProperty("consumeNotices")
    private List<String> consumeNotices;
    
    @JsonProperty("noticeBoardNames")
    private List<String> noticeBoardNames;
    
    /**
     * top level positions of Expect-/ConsumeNotices instructions
     * position -> list of board names
     * 
     */
    @JsonIgnore
    private Map<Integer, Set<String>> topLevelPositions;

    @JsonProperty("postNotices")
    public List<String> getPostNotices() {
        return postNotices;
    }

    @JsonProperty("postNotices")
    public void setPostNotices(List<String> postNotices) {
        this.postNotices = postNotices;
    }
    
    @JsonProperty("expectNotices")
    public List<String> getExpectNotices() {
        return expectNotices;
    }

    @JsonProperty("expectNotices")
    public void setExpectNotices(List<String> expectNotices) {
        this.expectNotices = expectNotices;
    }
    
    @JsonProperty("consumeNotices")
    public List<String> getConsumeNotices() {
        return consumeNotices;
    }

    @JsonProperty("consumeNotices")
    public void setConsumeNotices(List<String> consumeNotices) {
        this.consumeNotices = consumeNotices;
    }
    
    @JsonProperty("noticeBoardNames")
    public List<String> getNoticeBoardNames() {
        return noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(List<String> noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
    }
    
    @JsonIgnore
    public Map<Integer, Set<String>> getTopLevelPositions() {
        return topLevelPositions;
    }

    @JsonIgnore
    public void setTopLevelPositions(Map<Integer, Set<String>> topLevelPositions) {
        this.topLevelPositions = topLevelPositions;
    }
    
    @JsonIgnore
    public boolean hasPostNotice(String boardName) {
        return postNotices != null && postNotices.contains(boardName);
    }
    
    @JsonIgnore
    public boolean hasExpectNotice(String boardName) {
        return expectNotices != null && expectNotices.contains(boardName);
    }
    
    @JsonIgnore
    public boolean hasConsumeNotice(String boardName) {
        return consumeNotices != null && consumeNotices.contains(boardName);
    }
    
    @JsonIgnore
    public int hasPostNotice() {
        return postNotices != null && !postNotices.isEmpty() ? 1 : 0;
    }
    
    @JsonIgnore
    public int hasExpectNotice() {
        return expectNotices != null && !expectNotices.isEmpty() ? 2 : 0;
    }
    
    @JsonIgnore
    public int hasConsumeNotice() {
        return consumeNotices != null && !consumeNotices.isEmpty() ? 4 : 0;
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
