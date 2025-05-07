package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseSearch;
import com.sos.joc.model.inventory.search.ResponseSearchItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class SearchResourceImpl extends JOCResourceImpl implements ISearchResource {

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestSearchFilter.class);
            RequestSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestSearchFilter.class);

            JOCDefaultResponse response = checkPermissions(accessToken, in, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }

            ResponseSearch answer = new ResponseSearch();
            answer.setResults(getSearchResult(in, folderPermissions));
            answer.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    public static List<ResponseSearchItem> getSearchResult(final RequestSearchFilter in, SOSAuthFolderPermissions folderPermissions)
            throws Exception {
        return SOSReflection.isEmpty(in.getAdvanced()) ? getBasicSearch(in, folderPermissions) : getAdvancedSearch(in, folderPermissions);
    }

    private static List<ResponseSearchItem> getBasicSearch(final RequestSearchFilter in, SOSAuthFolderPermissions folderPermissions) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
            // tags only for Workflows
            List<String> tags = RequestSearchReturnType.WORKFLOW.equals(in.getReturnType()) ? in.getTags() : null;

            List<InventorySearchItem> items = null;
            if (in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue()) {
                items = dbLayer.getBasicSearchDeployedOrReleasedConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), tags, in
                        .getControllerId());
            } else {
                items = dbLayer.getBasicSearchInventoryConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), tags, in
                        .getUndeployedOrUnreleased(), in.getValid());
            }

            List<ResponseSearchItem> r = Collections.emptyList();
            if (items != null) {
                r = items.stream().map(item -> toResponseSearchItem(item, folderPermissions)).sorted(Comparator.comparing(
                        ResponseSearchItem::getPath)).collect(Collectors.toList());
            }
            return r;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static List<ResponseSearchItem> getAdvancedSearch(final RequestSearchFilter in, SOSAuthFolderPermissions folderPermissions) throws Exception {
        SOSHibernateSession session = null;
        try {
            adjustAdvanced(in);

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
            // tags only for Workflows
            List<String> tags = RequestSearchReturnType.WORKFLOW.equals(in.getReturnType()) ? in.getTags() : null;

            List<InventorySearchItem> items = null;
            boolean deployedOrReleased = in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue();
            if (deployedOrReleased) {
                items = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), tags, in
                        .getAdvanced(), in.getControllerId());
            } else {
                items = dbLayer.getAdvancedSearchInventoryConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), tags, in
                        .getUndeployedOrUnreleased(), in.getValid(), in.getAdvanced());
            }

            List<ResponseSearchItem> r = new ArrayList<>();
            if (items != null) {
                List<InventorySearchItem> sorted = items.stream().sorted(Comparator.comparing(InventorySearchItem::getPath)).collect(Collectors
                        .toList());
                RequestSearchAdvancedItem workflowAdvanced = cloneAdvanced4WorkflowSearch(in);
                RequestSearchAdvancedItem jobResourceAdvanced = setAdvanced4JobResourceSearch(workflowAdvanced, in.getReturnType());
                boolean checkworkflowAdvanced = !SOSReflection.isEmpty(workflowAdvanced) || !SOSString.isEmpty(in.getAdvanced().getWorkflow());
                boolean checkjobResourceAdvanced = !SOSReflection.isEmpty(jobResourceAdvanced);
                for (InventorySearchItem item : sorted) {
                    boolean checkWorkflow = false;
                    boolean checkjobResource = false;
                    switch (in.getReturnType()) {
                    case JOBRESOURCE:
                        workflowAdvanced.setJobResource(item.getName());
                        checkWorkflow = checkworkflowAdvanced;
                        checkjobResource = checkjobResourceAdvanced;
                        break;
                    case NOTICEBOARD:
                        workflowAdvanced.setNoticeBoard(item.getName());
                        checkWorkflow = checkworkflowAdvanced;
                        break;
                    case LOCK:
                        workflowAdvanced.setLock(item.getName());
                        checkWorkflow = checkworkflowAdvanced;
                        break;
                    default:
                        break;
                    }
                    if (checkWorkflow) {
                        List<InventorySearchItem> wi = null;
                        if (deployedOrReleased) {
                            wi = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(RequestSearchReturnType.WORKFLOW, in.getAdvanced()
                                    .getWorkflow(), null, null, workflowAdvanced, in.getControllerId());
                        } else {
                            wi = dbLayer.getAdvancedSearchInventoryConfigurations(RequestSearchReturnType.WORKFLOW, in.getAdvanced().getWorkflow(),
                                    workflowAdvanced);
                        }
                        if (wi == null || wi.size() == 0) {
                            continue;
                        }
                    }
                    if (checkjobResource) {
                        String content = null;
                        if (deployedOrReleased) {
                            content = dbLayer.getDeployedConfigurationsContent(item.getId(), in.getControllerId());
                        } else {
                            content = dbLayer.getInventoryConfigurationsContent(item.getId());
                        }
                        if (content == null) {
                            continue;
                        }
                        JobResource jr = (JobResource) JocInventory.content2IJSObject(content, ConfigurationType.JOBRESOURCE);
                        if (jr == null) {
                            continue;
                        }
                        if (!SOSString.isEmpty(jobResourceAdvanced.getArgumentName())) {
                            if (jr.getArguments() == null || jr.getArguments().getAdditionalProperties() == null) {
                                continue;
                            }
                            if (jr.getArguments().getAdditionalProperties().keySet().stream().map(String::toLowerCase).noneMatch(k -> k.contains(
                                    jobResourceAdvanced.getArgumentName().toLowerCase()))) {
                                continue;
                            }
                        }
                        if (!SOSString.isEmpty(jobResourceAdvanced.getArgumentValue())) {
                            if (jr.getArguments() == null || jr.getArguments().getAdditionalProperties() == null) {
                                continue;
                            }
                            if (jr.getArguments().getAdditionalProperties().values().stream().map(String::toLowerCase).noneMatch(k -> k.contains(
                                    jobResourceAdvanced.getArgumentValue().toLowerCase()))) {
                                continue;
                            }
                        }
                        if (!SOSString.isEmpty(jobResourceAdvanced.getEnvName())) {
                            if (jr.getEnv() == null || jr.getEnv().getAdditionalProperties() == null) {
                                continue;
                            }
                            if (jr.getEnv().getAdditionalProperties().keySet().stream().map(String::toLowerCase).noneMatch(k -> k.contains(
                                    jobResourceAdvanced.getEnvName().toLowerCase()))) {
                                continue;
                            }
                        }
                        if (!SOSString.isEmpty(jobResourceAdvanced.getEnvValue())) {
                            if (jr.getEnv() == null || jr.getEnv().getAdditionalProperties() == null) {
                                continue;
                            }
                            if (jr.getEnv().getAdditionalProperties().values().stream().map(String::toLowerCase).noneMatch(k -> k.contains(
                                    jobResourceAdvanced.getEnvValue().toLowerCase()))) {
                                continue;
                            }
                        }
                    }

                    ResponseSearchItem ri = toResponseSearchItem(item, folderPermissions);
                    r.add(ri);
                }
            }
            return r;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static void adjustAdvanced(final RequestSearchFilter in) {
        if (in.getAdvanced() == null) {
            return;
        }
        switch (in.getReturnType()) {
        case WORKFLOW:
            in.getAdvanced().setWorkflow(null);
            break;
        case FILEORDERSOURCE:
            in.getAdvanced().setFileOrderSource(null);
            break;
        case JOBRESOURCE:
            in.getAdvanced().setJobResource(null);
            break;
        case NOTICEBOARD:
            in.getAdvanced().setNoticeBoard(null);
            break;
        case LOCK:
            in.getAdvanced().setLock(null);
            break;
        case SCHEDULE:
            in.getAdvanced().setSchedule(null);
            break;
        case INCLUDESCRIPT:
            in.getAdvanced().setIncludeScript(null);
            break;
        case CALENDAR:
            in.getAdvanced().setCalendar(null);
            break;
        case JOBTEMPLATE:
            in.getAdvanced().setJobTemplate(null);
            break;
        case REPORT:
            // TODO advanced?
            break;
        }
    }

    private static RequestSearchAdvancedItem cloneAdvanced4WorkflowSearch(final RequestSearchFilter in) {
        if (in.getAdvanced() == null) {
            return null;
        }
        RequestSearchAdvancedItem item = new RequestSearchAdvancedItem();
        item.setAgentName(in.getAdvanced().getAgentName());
        item.setArgumentName(in.getAdvanced().getArgumentName());
        item.setArgumentValue(in.getAdvanced().getArgumentValue());
        item.setEnvName(in.getAdvanced().getEnvName());
        item.setEnvValue(in.getAdvanced().getEnvValue());
        item.setNoticeBoard(in.getAdvanced().getNoticeBoard());
        item.setFileOrderSource(in.getAdvanced().getFileOrderSource());
        item.setJobCountFrom(in.getAdvanced().getJobCountFrom());
        item.setJobCountTo(in.getAdvanced().getJobCountTo());
        item.setJobCriticality(in.getAdvanced().getJobCriticality());
        item.setJobName(in.getAdvanced().getJobName());
        if (SOSString.isEmpty(in.getAdvanced().getJobName()) || in.getAdvanced().getJobNameExactMatch() != Boolean.TRUE) {
            item.setJobNameExactMatch(null);
        } else {
            item.setJobNameExactMatch(true);
        }
        item.setJobResource(in.getAdvanced().getJobResource());
        item.setLock(in.getAdvanced().getLock());
        item.setSchedule(in.getAdvanced().getSchedule());
        item.setIncludeScript(in.getAdvanced().getIncludeScript());
        item.setCalendar(in.getAdvanced().getCalendar());
        item.setJobTemplate(in.getAdvanced().getJobTemplate());
        item.setWorkflow(null);
        return item;
    }
    
    private static RequestSearchAdvancedItem setAdvanced4JobResourceSearch(RequestSearchAdvancedItem wAdvanched, RequestSearchReturnType returnType) {
        if (wAdvanched == null) {
            return null;
        }
        if (!RequestSearchReturnType.JOBRESOURCE.equals(returnType)) {
            return null;
        }
        RequestSearchAdvancedItem item = new RequestSearchAdvancedItem();
        item.setArgumentName(wAdvanched.getArgumentName());
        item.setArgumentValue(wAdvanched.getArgumentValue());
        item.setEnvName(wAdvanched.getEnvName());
        item.setEnvValue(wAdvanched.getEnvValue());
        item.setJobNameExactMatch(null);
        wAdvanched.setArgumentName(null);
        wAdvanched.setArgumentValue(null);
        wAdvanched.setEnvName(null);
        wAdvanched.setEnvValue(null);
        return item;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestSearchFilter in, boolean permission) {
        JOCDefaultResponse response = initPermissions(in.getControllerId(), permission);
        if (response == null) {
            if (in.getFolders() != null) {
                for (String folder : in.getFolders()) {
                    if (!folderPermissions.isPermittedForFolder(folder)) {
                        throw new JocFolderPermissionsException(folder);
                    }
                }
            }
        }
        return response;
    }
    
    private static ResponseSearchItem toResponseSearchItem(InventorySearchItem item, SOSAuthFolderPermissions folderPermissions) {
        ResponseSearchItem ri = new ResponseSearchItem();
        ri.setId(item.getId());
        ri.setPath(item.getPath());
        ri.setName(item.getName());
        ri.setObjectType(item.getTypeAsEnum());
        ri.setTitle(item.getTitle());
        ri.setControllerId(item.getControllerId());
        ri.setValid(item.isValid());
        ri.setDeleted(item.isDeleted());
        ri.setDeployed(item.isDeployed());
        ri.setReleased(item.isReleased());
        ri.setHasDeployments(item.getCountDeployed().intValue() > 0);
        ri.setHasReleases(item.getCountReleased().intValue() > 0);
        ri.setPermitted(folderPermissions.isPermittedForFolder(item.getFolder()));
        return ri;
    }
}
