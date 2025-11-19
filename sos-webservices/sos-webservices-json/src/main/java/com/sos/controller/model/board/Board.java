
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.inventory.model.board.BoardType;
import com.sos.inventory.model.deploy.DeployType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notice board
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionDate",
    "state",
    "numOfNotices",
    "numOfAnnouncements",
    "numOfPostedNotices",
    "numOfExpectedNotices",
    "numOfExpectingOrders",
    "notices",
    "hasNote"
})
public class Board
    extends com.sos.inventory.model.board.Board
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date versionDate;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private SyncState state;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNotices")
    private Integer numOfNotices;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAnnouncements")
    private Integer numOfAnnouncements;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPostedNotices")
    private Integer numOfPostedNotices;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectedNotices")
    private Integer numOfExpectedNotices;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    private Integer numOfExpectingOrders;
    @JsonProperty("notices")
    private List<Notice> notices = null;
    @JsonProperty("hasNote")
    private Boolean hasNote;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Board() {
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
     * @param postOrderToNoticeId
     * @param state
     * @param documentationName
     * @param numOfAnnouncements
     */
    public Board(String path, Date versionDate, SyncState state, Integer numOfNotices, Integer numOfAnnouncements, Integer numOfPostedNotices, Integer numOfExpectedNotices, Integer numOfExpectingOrders, List<Notice> notices, BoardType boardType, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId, String version, String title, String documentationName) {
        super( boardType, postOrderToNoticeId, endOfLife, expectOrderToNoticeId, version, title, documentationName);
        this.path = path;
        this.versionDate = versionDate;
        this.state = state;
        this.numOfNotices = numOfNotices;
        this.numOfAnnouncements = numOfAnnouncements;
        this.numOfPostedNotices = numOfPostedNotices;
        this.numOfExpectedNotices = numOfExpectedNotices;
        this.numOfExpectingOrders = numOfExpectingOrders;
        this.notices = notices;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public Date getVersionDate() {
        return versionDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public SyncState getState() {
        return state;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(SyncState state) {
        this.state = state;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNotices")
    public Integer getNumOfNotices() {
        return numOfNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNotices")
    public void setNumOfNotices(Integer numOfNotices) {
        this.numOfNotices = numOfNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAnnouncements")
    public Integer getNumOfAnnouncements() {
        return numOfAnnouncements;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAnnouncements")
    public void setNumOfAnnouncements(Integer numOfAnnouncements) {
        this.numOfAnnouncements = numOfAnnouncements;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPostedNotices")
    public Integer getNumOfPostedNotices() {
        return numOfPostedNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfPostedNotices")
    public void setNumOfPostedNotices(Integer numOfPostedNotices) {
        this.numOfPostedNotices = numOfPostedNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectedNotices")
    public Integer getNumOfExpectedNotices() {
        return numOfExpectedNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectedNotices")
    public void setNumOfExpectedNotices(Integer numOfExpectedNotices) {
        this.numOfExpectedNotices = numOfExpectedNotices;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    public Integer getNumOfExpectingOrders() {
        return numOfExpectingOrders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfExpectingOrders")
    public void setNumOfExpectingOrders(Integer numOfExpectingOrders) {
        this.numOfExpectingOrders = numOfExpectingOrders;
    }

    @JsonProperty("notices")
    public List<Notice> getNotices() {
        return notices;
    }

    @JsonProperty("notices")
    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    @JsonProperty("hasNote")
    public Boolean getHasNote() {
        return hasNote;
    }

    @JsonProperty("hasNote")
    public void setHasNote(Boolean hasNote) {
        this.hasNote = hasNote;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("versionDate", versionDate).append("state", state).append("numOfNotices", numOfNotices).append("numOfAnnouncements", numOfAnnouncements).append("numOfPostedNotices", numOfPostedNotices).append("numOfExpectedNotices", numOfExpectedNotices).append("numOfExpectingOrders", numOfExpectingOrders).append("notices", notices).append("hasNote", hasNote).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(notices).append(path).append(numOfExpectedNotices).append(numOfExpectingOrders).append(numOfNotices).append(numOfPostedNotices).append(state).append(hasNote).append(versionDate).append(numOfAnnouncements).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Board) == false) {
            return false;
        }
        Board rhs = ((Board) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(notices, rhs.notices).append(path, rhs.path).append(numOfExpectedNotices, rhs.numOfExpectedNotices).append(numOfExpectingOrders, rhs.numOfExpectingOrders).append(numOfNotices, rhs.numOfNotices).append(numOfPostedNotices, rhs.numOfPostedNotices).append(state, rhs.state).append(hasNote, rhs.hasNote).append(versionDate, rhs.versionDate).append(numOfAnnouncements, rhs.numOfAnnouncements).isEquals();
    }

}
