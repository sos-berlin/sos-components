package com.sos.joc.db.audit;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.model.audit.AuditLogFilter;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;

public class AuditLogDBFilter {

	private Date createdFrom;
	private Date createdTo;
	private Collection<String> controllerIds;
	private Collection<CategoryType> categories;
	private Collection<Folder> folders;
	private String ticketLink;
	private String account;
	private String reason;

	public AuditLogDBFilter() {
		super();
	}

	public AuditLogDBFilter(AuditLogFilter auditLogFilter, Collection<String> controllerIds, Collection<CategoryType> categories) throws ControllerInvalidResponseDataException {
	    this.controllerIds = controllerIds;
	    this.categories = categories;
	    this.folders = auditLogFilter.getFolders();
		this.ticketLink = auditLogFilter.getTicketLink();
		this.account = auditLogFilter.getAccount();

		this.createdFrom = JobSchedulerDate.getDateFrom(auditLogFilter.getDateFrom(), auditLogFilter.getTimeZone());
		this.createdTo = JobSchedulerDate.getDateTo(auditLogFilter.getDateTo(), auditLogFilter.getTimeZone());
		this.reason = auditLogFilter.getComment();
	}

	public String getTicketLink() {
		return ticketLink != null ? ticketLink.replace('*', '%') : null;
	}

	public String getAccount() {
		return account != null ? account.replace('*', '%') : null;
	}

	public String getReason() {
		return reason != null ? reason.replace('*', '%') : null;
	}

	public Collection<String> getControllerIds() {
		return controllerIds == null ? Collections.emptySet() : controllerIds;
	}
	
	public Collection<CategoryType> getCategories() {
        return categories == null ? Collections.emptySet() : categories;
    }
	
	public Collection<Integer> getCategoryIntValues() {
        return categories == null ? Collections.emptySet() : categories.stream().map(CategoryType::intValue).collect(Collectors.toSet());
    }

	public Date getCreatedFrom() {
		return createdFrom;
	}

	public Date getCreatedTo() {
		return createdTo;
	}
 
	public Collection<Folder> getFolders() {
		return folders == null ? Collections.emptySet() : folders;
	}

	public Collection<String> getFolderPaths() {
	    return getFolders().stream().map(Folder::getFolder).collect(Collectors.toSet());
	}

	 
}