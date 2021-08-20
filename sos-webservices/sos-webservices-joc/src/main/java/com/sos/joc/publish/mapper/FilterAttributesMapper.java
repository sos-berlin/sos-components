package com.sos.joc.publish.mapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.criterion.MatchMode;

import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DepHistoryCompactFilter;
import com.sos.joc.model.publish.DepHistoryDetailFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;

public abstract class FilterAttributesMapper {

    public static Set<String> getDefaultAttributesFromFilter (DepHistoryDetailFilter filter, Collection<String> allowedControllers) {
        Set<String> filterAttributes = new HashSet<String>();
        if (filter.getAccount() != null) {
            filterAttributes.add("account");
        }
        if (filter.getPath() != null) {
            filterAttributes.add("path");
        }
        if (filter.getFolder() != null) {
            filterAttributes.add("folder");
        }
        if (filter.getDeployType() != null) {
            filterAttributes.add("type");
        }
        if (!allowedControllers.isEmpty()) {
            filterAttributes.add("controllerIds");
        }
//        if (filter.getControllerId() != null) {
//            filterAttributes.add("controllerId");
//        }
        if (filter.getCommitId() != null) {
            filterAttributes.add("commitId");
        }
        if (filter.getAuditlogId() != null) {
            filterAttributes.add("auditlogId");
        }
        if (filter.getVersion() != null) {
            filterAttributes.add("version");
        }
        if (filter.getOperation() != null) {
            filterAttributes.add("operation");
        }
        if (filter.getState() != null) {
            filterAttributes.add("state");
        }
        if (filter.getDeploymentDate() != null) {
            filterAttributes.add("deploymentDate");
        }
        if (filter.getDeleteDate() != null) {
            filterAttributes.add("deleteDate");
        }
        if (filter.getFrom() != null) {
            filterAttributes.add("from");
        }
        if (filter.getTo() != null) {
            filterAttributes.add("to");
        }
        if (filter.getLimit() != null) {
            filterAttributes.add("limit");
        }
        return filterAttributes;
    }

    public static Set<String> getDefaultAttributesFromFilter (DepHistoryCompactFilter filter) {
        Set<String> filterAttributes = new HashSet<String>();
        if (filter.getAccount() != null) {
            filterAttributes.add("account");
        }
        if (filter.getFolder() != null) {
            filterAttributes.add("folder");
        }
        if (filter.getControllerId() != null) {
            filterAttributes.add("controllerId");
        }
        if (filter.getDeploymentDate() != null) {
            filterAttributes.add("deploymentDate");
        }
        if (filter.getDeleteDate() != null) {
            filterAttributes.add("deleteDate");
        }
        if (filter.getFrom() != null) {
            filterAttributes.add("from");
        }
        if (filter.getTo() != null) {
            filterAttributes.add("to");
        }
        if (filter.getLimit() != null) {
            filterAttributes.add("limit");
        }
        return filterAttributes;
    }

    public static Object getValueByFilterAttribute (DepHistoryDetailFilter filter, String attribute) {
        switch(attribute) {
            case "account":
                return filter.getAccount();
            case "path":
                return filter.getPath();
            case "folder":
                return filter.getFolder();
            case "type":
                try {
                    return DeployType.fromValue(filter.getDeployType()).intValue();
                } catch(Exception e) {
                    return ConfigurationType.fromValue(filter.getDeployType()).intValue();
                } 
            case "controllerId":
                return filter.getControllerId();
            case "commitId":
                return filter.getCommitId();
            case "version":
                return filter.getVersion();
            case "operation":
                return OperationType.valueOf(filter.getOperation()).value();
            case "state":
                return DeploymentState.valueOf(filter.getState()).value();
            case "deploymentDate":
                return filter.getDeploymentDate();
            case "deleteDate":
                return filter.getDeleteDate();
            case "from":
                return JobSchedulerDate.getDateFrom(filter.getFrom(), filter.getTimeZone());
            case "to":
                return JobSchedulerDate.getDateTo(filter.getTo(), filter.getTimeZone());
            case "timeZone":
                return filter.getTimeZone();
            case "limit":
                return filter.getLimit();
        }
        return null;
    }

    public static Object getValueByFilterAttribute (DepHistoryCompactFilter filter, String attribute) {
        switch(attribute) {
            case "account":
                return filter.getAccount();
            case "folder":
                return filter.getFolder();
            case "controllerId":
                return filter.getControllerId();
            case "deploymentDate":
                return filter.getDeploymentDate();
            case "deleteDate":
                return filter.getDeleteDate();
            case "from":
                return JobSchedulerDate.getDateFrom(filter.getFrom(), filter.getTimeZone());
            case "to":
                return JobSchedulerDate.getDateTo(filter.getTo(), filter.getTimeZone());
            case "timeZone":
                return filter.getTimeZone();
            case "limit":
                return filter.getLimit();
        }
        return null;
    }

    public static Set<String> getDefaultAttributesFromFilter(RedeployFilter filter) {
        Set<String> filterAttributes = new HashSet<String>();
        if (filter.getControllerId() != null) {
            filterAttributes.add("controllerId");
        }
        if (filter.getFolder() != null) {
            filterAttributes.add("folder");
            filterAttributes.add("likeFolder");
        }
        return filterAttributes;
    }
    
    public static Object getValueByFilterAttribute (RedeployFilter filter, String attribute) {
        switch(attribute) {
        case "controllerId":
            return filter.getControllerId();
        case "folder":
            return filter.getFolder();
        case "likeFolder":
            return MatchMode.START.toMatchString(filter.getFolder());
        }
        return null;
    }
    
    public static Set<String> getAttributesfromObject(Configuration depCfgToDelete) {
        Set<String> filterAttributes = new HashSet<String>();
        if (depCfgToDelete.getPath() != null) {
            filterAttributes.add("path");
        }
        if (depCfgToDelete.getObjectType() != null) {
            filterAttributes.add("type");
        }
        return filterAttributes;
    }
    
    public static Object getValueByObjectAttribute (Configuration depCfgToDelete, String attribute) {
        switch(attribute) {
        case "path":
            return depCfgToDelete.getPath();
        case "type":
            return depCfgToDelete.getObjectType().intValue();
        }
        return null;
    }
}
