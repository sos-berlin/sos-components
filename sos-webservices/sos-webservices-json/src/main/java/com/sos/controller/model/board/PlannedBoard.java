
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.inventory.model.board.BoardType;
import com.sos.joc.model.plan.PlanId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * planned board
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "planId"
})
public class PlannedBoard
    extends Board
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    private PlanId planId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PlannedBoard() {
    }

    /**
     * 
     * @param numOfExpectedNotices
     * @param boardType
     * @param numOfNotices
     * @param numOfPostedNotices
     * @param expectOrderToNoticeId
     * @param title
     * @param versionDate
     * @param version
     * @param endOfLife
     * @param notices
     * @param path
     * @param numOfExpectingOrders
     * @param name
     * @param postOrderToNoticeId
     * @param planId
     * @param state
     * @param documentationName
     * @param numOfAnnouncements
     */
    public PlannedBoard(String name, PlanId planId, String path, Date versionDate, SyncState state, Integer numOfNotices, Integer numOfAnnouncements, Integer numOfPostedNotices, Integer numOfExpectedNotices, Integer numOfExpectingOrders, List<Notice> notices, BoardType boardType, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId, String version, String title, String documentationName) {
        super(path, versionDate, state, numOfNotices, numOfAnnouncements, numOfPostedNotices, numOfExpectedNotices, numOfExpectingOrders, notices, boardType, postOrderToNoticeId, endOfLife, expectOrderToNoticeId, version, title, documentationName);
        this.name = name;
        this.planId = planId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public PlanId getPlanId() {
        return planId;
    }

    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public void setPlanId(PlanId planId) {
        this.planId = planId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("name", name).append("planId", planId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(name).append(planId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlannedBoard) == false) {
            return false;
        }
        PlannedBoard rhs = ((PlannedBoard) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(name, rhs.name).append(planId, rhs.planId).isEquals();
    }

}
