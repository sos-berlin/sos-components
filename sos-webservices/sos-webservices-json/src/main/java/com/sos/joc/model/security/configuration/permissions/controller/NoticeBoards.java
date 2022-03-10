
package com.sos.joc.model.security.configuration.permissions.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "post",
    "delete"
})
public class NoticeBoards {

    /**
     * show resource tab 'notice boards'
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show resource tab 'notice boards'")
    private Boolean view = true;
    /**
     * post notice
     * 
     */
    @JsonProperty("post")
    @JsonPropertyDescription("post notice")
    private Boolean post = false;
    /**
     * delete notice
     * 
     */
    @JsonProperty("delete")
    @JsonPropertyDescription("delete notice")
    private Boolean delete = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NoticeBoards() {
    }

    /**
     * 
     * @param view
     * @param post
     * @param delete
     */
    public NoticeBoards(Boolean view, Boolean post, Boolean delete) {
        super();
        this.view = view;
        this.post = post;
        this.delete = delete;
    }

    /**
     * show resource tab 'notice boards'
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show resource tab 'notice boards'
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * post notice
     * 
     */
    @JsonProperty("post")
    public Boolean getPost() {
        return post;
    }

    /**
     * post notice
     * 
     */
    @JsonProperty("post")
    public void setPost(Boolean post) {
        this.post = post;
    }

    /**
     * delete notice
     * 
     */
    @JsonProperty("delete")
    public Boolean getDelete() {
        return delete;
    }

    /**
     * delete notice
     * 
     */
    @JsonProperty("delete")
    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("post", post).append("delete", delete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(post).append(delete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NoticeBoards) == false) {
            return false;
        }
        NoticeBoards rhs = ((NoticeBoards) other);
        return new EqualsBuilder().append(view, rhs.view).append(post, rhs.post).append(delete, rhs.delete).isEquals();
    }

}
