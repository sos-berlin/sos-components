
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.inventory.model.deploy.DeployType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
    "notices"
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
    @JsonProperty("notices")
    private List<Notice> notices = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Board() {
    }

    /**
     * 
     * @param notices
     * @param path
     * @param postOrderToNoticeId
     * @param expectOrderToNoticeId
     * @param state
     * @param documentationName
     * @param title
     * @param versionDate
     * @param version
     * @param endOfLife
     */
    public Board(String path, Date versionDate, SyncState state, List<Notice> notices, DeployType tYPE, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId, String version, String title, String documentationName) {
        super(postOrderToNoticeId, endOfLife, expectOrderToNoticeId, version, title, documentationName);
        this.path = path;
        this.versionDate = versionDate;
        this.state = state;
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

    @JsonProperty("notices")
    public List<Notice> getNotices() {
        return notices;
    }

    @JsonProperty("notices")
    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("versionDate", versionDate).append("state", state).append("notices", notices).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(notices).append(path).append(state).append(versionDate).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(notices, rhs.notices).append(path, rhs.path).append(state, rhs.state).append(versionDate, rhs.versionDate).isEquals();
    }

}
