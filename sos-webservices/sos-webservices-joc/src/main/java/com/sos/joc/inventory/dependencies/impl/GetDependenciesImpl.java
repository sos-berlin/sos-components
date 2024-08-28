package com.sos.joc.inventory.dependencies.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.board.Board;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.jobresource.JobResource;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.controller.model.lock.Lock;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.GetDependenciesResponse;
import com.sos.joc.model.inventory.references.ResponseItems;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/dependencies")
public class GetDependenciesImpl extends JOCResourceImpl implements IGetDependencies {
    
    private static final String API_CALL = "./inventory/dependencies";
    
    @Override
    public JOCDefaultResponse postGetDependencies(String xAccessToken, byte[] dependencyFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, dependencyFilter, xAccessToken);
            JsonValidator.validate(dependencyFilter, GetDependenciesRequest.class);
            GetDependenciesRequest filter = Globals.objectMapper.readValue(dependencyFilter, GetDependenciesRequest.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(xAccessToken);
            InventoryDBLayer dblayer = new InventoryDBLayer(hibernateSession);
            DBItemInventoryConfiguration inventoryDbItem = dblayer.getConfigurationByName(filter.getName(), ConfigurationType.fromValue(filter.getType()).intValue()).get(0);
//            ResponseItems references = getResponseItems(
//                    DependencyResolver.convert(hibernateSession, 
//                            DependencyResolver.getStoredDependencies(hibernateSession, inventoryDbItem)), hibernateSession);
//            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(references)
//                    .replaceAll(",\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"|\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"\\s*,", ""));
            
            ReferencedDbItem reference = DependencyResolver.convert(hibernateSession, 
                    DependencyResolver.getStoredDependencies(hibernateSession, inventoryDbItem));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(getResponse(reference, hibernateSession))
                    /*.replaceAll(",\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"|\\s*\"TYPE\"\\s*:\\s*\"[^\"]*\"\\s*,", "")*/);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private ResponseItems getResponseItems (ReferencedDbItem referencedDbItem, SOSHibernateSession session) throws SOSHibernateException {
        ResponseItems dependencyItems = new ResponseItems();
        referencedDbItem.getReferencedBy().stream().forEach(item -> {
            try {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    dependencyItems.getWorkflows().add(WorkflowConverter.convertInventoryWorkflow(item.getContent(), Workflow.class));
                    break;
                case FILEORDERSOURCE:
                    dependencyItems.getFileOrderSources().add(JocInventory.convertFileOrderSource(item.getContent(), FileOrderSource.class));
                    break;
                case JOBTEMPLATE:
                    dependencyItems.getJobTemplates().add(JocInventory.convertJobTemplate(item.getContent(), JobTemplate.class));
                    break;
                case JOBRESOURCE:
                    dependencyItems.getJobResources().add(JocInventory.convertDefault(item.getContent(), JobResource.class));
                    break;
                case NOTICEBOARD:
                    dependencyItems.getBoards().add(JocInventory.convertDefault(item.getContent(), Board.class));
                    break;
                case LOCK:
                    dependencyItems.getLocks().add(JocInventory.convertDefault(item.getContent(), Lock.class));
                    break;
                case SCHEDULE:
                    dependencyItems.getSchedules().add(JocInventory.convertSchedule(item.getContent(), Schedule.class));
                    break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    dependencyItems.getCalendars().add(JocInventory.convertDefault(item.getContent(), Calendar.class));
                    break;
                default:
                    break;
                }
            } catch (IOException e) {
                throw new JocConfigurationException(e);
            }
        });
        InventoryDBLayer dblayer = new InventoryDBLayer(session);
        dependencyItems.setIsRenamed(dblayer.isRenamed(referencedDbItem.getName(), referencedDbItem.getType()));
        dependencyItems.setDeliveryDate(Date.from(Instant.now()));
        return dependencyItems;
    }
    
    private GetDependenciesResponse getResponse(ReferencedDbItem referencedDbItem, SOSHibernateSession session) {
        GetDependenciesResponse response = new GetDependenciesResponse();
        response.setName(referencedDbItem.getName());
        response.setType(referencedDbItem.getType().value());
        response.setReferencedBy(referencedDbItem.getReferencedBy().stream()
                .map(item -> {
                    try {
                        return JocInventory.convert(item, session);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    } catch (IOException e) {
                        throw new JocException(e);
                    }
                }).collect(Collectors.toList()));

//        response.setReferencedBy(referencedDbItem.getReferencedBy().stream().map(item -> {
//            try {
//                switch(item.getTypeAsEnum()) {
//                case WORKFLOW:
//                    return WorkflowConverter.convertInventoryWorkflow(item.getContent(), Workflow.class);
//                case FILEORDERSOURCE:
//                    return JocInventory.convertFileOrderSource(item.getContent(), FileOrderSource.class);
//                case JOBTEMPLATE:
//                    return JocInventory.convertJobTemplate(item.getContent(), JobTemplate.class);
//                case JOBRESOURCE:
//                    return JocInventory.convertDefault(item.getContent(), JobResource.class);
//                case NOTICEBOARD:
//                    return JocInventory.convertDefault(item.getContent(), Board.class);
//                case LOCK:
//                    return JocInventory.convertDefault(item.getContent(), Lock.class);
//                case SCHEDULE:
//                    return JocInventory.convertSchedule(item.getContent(), Schedule.class);
//                case WORKINGDAYSCALENDAR:
//                case NONWORKINGDAYSCALENDAR:
//                    return JocInventory.convertDefault(item.getContent(), Calendar.class);
//                default:
//                    return null;
//                }
//            } catch (IOException e) {
//                throw new JocConfigurationException(e);
//            }
//        }).filter(Objects::nonNull).collect(Collectors.toList()));
        
        return response;
    }
}
