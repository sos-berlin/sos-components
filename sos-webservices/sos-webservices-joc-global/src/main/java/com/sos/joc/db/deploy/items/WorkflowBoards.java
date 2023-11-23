
package com.sos.joc.db.deploy.items;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.controller.model.workflow.WorkflowId;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowBoards
    extends WorkflowId
{

    @JsonProperty("postNotices")
    private List<String> postNotices;
    
    @JsonProperty("expectNotices")
    private List<String> expectNotices;

    @JsonProperty("consumeNotices")
    private List<String> consumeNotices;

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
    private int hasPostNotice() {
        return postNotices != null && !postNotices.isEmpty() ? 1 : 0;
    }
    
    @JsonIgnore
    private int hasExpectNotice() {
        return expectNotices != null && !expectNotices.isEmpty() ? 2 : 0;
    }
    
    @JsonIgnore
    private int hasConsumeNotice() {
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
