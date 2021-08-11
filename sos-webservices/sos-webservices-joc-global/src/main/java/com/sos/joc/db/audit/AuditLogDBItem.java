package com.sos.joc.db.audit;

import java.util.Date;

import com.sos.joc.model.audit.AuditLogItem;
import com.sos.joc.model.audit.CategoryType;

public class AuditLogDBItem {

    private Long id;

    private String controllerId;

    private String account;

    private String request;

    private String parameters;

    private Integer category;

    private String comment;

    private String ticketLink;

    private Integer timeSpent;

    private String commitId;

    private Date created;

    public AuditLogItem toAuditLogItem() {
        AuditLogItem item = new AuditLogItem();
        item.setId(id);
        item.setAccount(account);
        item.setRequest(request);
        item.setCreated(created);
        item.setControllerId(controllerId);
        item.setComment(comment);
        item.setParameters(parameters);
        item.setTimeSpent(timeSpent);
        item.setTicketLink(ticketLink);
        item.setCommitId(commitId);
        try {
            item.setCategory(CategoryType.fromValue(category));
        } catch (Throwable e) {
        }
        return item;
    }

}
