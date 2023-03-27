package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReferenceResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.references.RequestFilter;
import com.sos.joc.model.inventory.references.ResponseItems;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class ReferenceResourceImpl extends JOCResourceImpl implements IReferenceResource {

    // private static final Logger LOGGER = LoggerFactory.getLogger(ValidateResourceImpl.class);

    @Override
    public JOCDefaultResponse post(final String accessToken, String objectType, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class, true);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }
            checkRequiredParameter("objectType", objectType);

            ResponseItems entity = new ResponseItems();
            SOSHibernateSession session = null;
            String name = JocInventory.pathToName(in.getPath());
            try {
                if (objectType.toUpperCase().equals("CALENDAR")) {
                    objectType = ConfigurationType.WORKINGDAYSCALENDAR.value();
                }
                ConfigurationType type = ConfigurationType.fromValue(objectType.toUpperCase());
                if (ConfigurationType.FOLDER.equals(type)) {
                    throw new JocBadRequestException("Unsupported objectType:" + objectType);
                }
                if (!ConfigurationType.WORKFLOW.equals(type)) { // TODO later support other references
                    throw new JocBadRequestException("Unsupported objectType:" + objectType);
                }
                switch (type) {
                case WORKFLOW:
                    if (in.getObjectTypes() == null || in.getObjectTypes().isEmpty()) {
                        in.setObjectTypes(Arrays.asList(ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE, ConfigurationType.WORKFLOW));
                    }
                    session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                    InventoryDBLayer dbLayer = new InventoryDBLayer(session);

                    if (in.getObjectTypes().contains(ConfigurationType.FILEORDERSOURCE)) {
                        List<DBItemInventoryConfiguration> fileOrderSources = dbLayer.getUsedFileOrderSourcesByWorkflowName(name);
                        if (fileOrderSources != null) {
                            entity.setFileOrderSources(fileOrderSources.stream().map(dbItem -> {
                                try {
                                    FileOrderSource fos = JocInventory.convertFileOrderSource(dbItem.getContent(), FileOrderSource.class);
                                    fos.setPath(dbItem.getPath());
                                    // reduce information
                                    fos.setDocumentationName(null);
                                    fos.setDelay(null);
                                    fos.setState(null);
                                    fos.setTimeZone(null);
                                    fos.setWorkflowName(null);
                                    fos.setTitle(null);
                                    fos.setVersion(null);
                                    fos.setVersionDate(null);
                                    return fos;
                                } catch (Exception e) {
                                    return null;
                                }
                            }).filter(Objects::nonNull).collect(Collectors.toList()));
                        }
                    }
                    if (in.getObjectTypes().contains(ConfigurationType.SCHEDULE)) {
                        List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(name);
                        if (schedules != null) {
                            entity.setSchedules(schedules.stream().map(dbItem -> {
                                try {
                                    Schedule s = (Schedule) JocInventory.content2IJSObject(dbItem.getContent(), ConfigurationType.SCHEDULE);
                                    s.setPath(dbItem.getPath());
                                    // reduce information
                                    s.setDocumentationName(null);
                                    s.setOrderParameterisations(null);
                                    s.setPlanOrderAutomatically(null);
                                    s.setSubmitOrderToControllerWhenPlanned(null);
                                    s.setWorkflowNames(null);
                                    s.setTitle(null);
                                    s.setVersion(null);
                                    return s;
                                } catch (Exception e) {
                                    return null;
                                }
                            }).filter(Objects::nonNull).collect(Collectors.toList()));
                        }
                    }
                    if (in.getObjectTypes().contains(ConfigurationType.WORKFLOW)) {
                        List<DBItemInventoryConfiguration> workflows = dbLayer.getAddOrderWorkflowsByWorkflowName(name);
                        if (workflows != null) {
                            entity.setWorkflows(workflows.stream().map(dbItem -> {
                                try {
                                    Workflow w = new Workflow(); //WorkflowConverter.convertInventoryWorkflow(dbItem.getContent(), Workflow.class);
                                    w.setPath(dbItem.getPath());
                                    // reduce information
                                    w.setDocumentationName(null);
                                    w.setInstructions(null);
                                    w.setJobs(null);
                                    w.setOrderPreparation(null);
                                    w.setState(null);
                                    w.setTimeZone(null);
                                    w.setTitle(null);
                                    w.setVersion(null);
                                    w.setVersionDate(null);
                                    w.setIsCurrentVersion(null);
                                    w.setSuspended(null);
//                                    Workflow w = new Workflow();
//                                    w.setPath(dbItem.getPath());
                                    return w;
                                } catch (Exception e) {
                                    return null;
                                }
                            }).filter(Objects::nonNull).collect(Collectors.toList()));
                        }
                    }
                    break;
                default:
                    break;
                }

            } catch (IllegalArgumentException e) {
                throw new JocBadRequestException("Unsupported objectType:" + objectType);
            } finally {
                Globals.disconnect(session);
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(entity).replaceAll(
                    ",\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"|\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"\\s*,", ""));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
