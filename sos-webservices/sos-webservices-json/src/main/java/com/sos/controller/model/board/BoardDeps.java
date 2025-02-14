
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.workflow.WorkflowIdAndTags;
import com.sos.inventory.model.board.BoardType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * board with dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "postingWorkflows",
    "expectingWorkflows",
    "consumingWorkflows"
})
public class BoardDeps
    extends Board
{

    @JsonProperty("postingWorkflows")
    private List<WorkflowIdAndTags> postingWorkflows = null;
    @JsonProperty("expectingWorkflows")
    private List<WorkflowIdAndTags> expectingWorkflows = null;
    @JsonProperty("consumingWorkflows")
    private List<WorkflowIdAndTags> consumingWorkflows = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BoardDeps() {
    }

    /**
     * 
     * @param numOfExpectedNotices
     * @param boardType
     * @param postingWorkflows
     * @param numOfNotices
     * @param numOfPostedNotices
     * @param expectOrderToNoticeId
     * @param title
     * @param consumingWorkflows
     * @param versionDate
     * @param version
     * @param endOfLife
     * @param notices
     * @param path
     * @param numOfExpectingOrders
     * @param expectingWorkflows
     * @param postOrderToNoticeId
     * @param state
     * @param documentationName
     * @param numOfAnnouncements
     */
    public BoardDeps(List<WorkflowIdAndTags> postingWorkflows, List<WorkflowIdAndTags> expectingWorkflows, List<WorkflowIdAndTags> consumingWorkflows, String path, Date versionDate, SyncState state, Integer numOfNotices, Integer numOfAnnouncements, Integer numOfPostedNotices, Integer numOfExpectedNotices, Integer numOfExpectingOrders, List<Notice> notices, BoardType boardType, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId, String version, String title, String documentationName) {
        super(path, versionDate, state, numOfNotices, numOfAnnouncements, numOfPostedNotices, numOfExpectedNotices, numOfExpectingOrders, notices, boardType, postOrderToNoticeId, endOfLife, expectOrderToNoticeId, version, title, documentationName);
        this.postingWorkflows = postingWorkflows;
        this.expectingWorkflows = expectingWorkflows;
        this.consumingWorkflows = consumingWorkflows;
    }

    @JsonProperty("postingWorkflows")
    public List<WorkflowIdAndTags> getPostingWorkflows() {
        return postingWorkflows;
    }

    @JsonProperty("postingWorkflows")
    public void setPostingWorkflows(List<WorkflowIdAndTags> postingWorkflows) {
        this.postingWorkflows = postingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public List<WorkflowIdAndTags> getExpectingWorkflows() {
        return expectingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public void setExpectingWorkflows(List<WorkflowIdAndTags> expectingWorkflows) {
        this.expectingWorkflows = expectingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public List<WorkflowIdAndTags> getConsumingWorkflows() {
        return consumingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public void setConsumingWorkflows(List<WorkflowIdAndTags> consumingWorkflows) {
        this.consumingWorkflows = consumingWorkflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("postingWorkflows", postingWorkflows).append("expectingWorkflows", expectingWorkflows).append("consumingWorkflows", consumingWorkflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectingWorkflows).append(consumingWorkflows).append(postingWorkflows).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoardDeps) == false) {
            return false;
        }
        BoardDeps rhs = ((BoardDeps) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectingWorkflows, rhs.expectingWorkflows).append(consumingWorkflows, rhs.consumingWorkflows).append(postingWorkflows, rhs.postingWorkflows).isEquals();
    }

}
