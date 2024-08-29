package com.sos.joc.inventory.dependencies.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.GetDependenciesResponse;
import com.sos.joc.model.inventory.dependencies.RequestItem;
import com.sos.joc.model.inventory.dependencies.ResponseItem;
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
            List<ReferencedDbItem> items = new ArrayList<ReferencedDbItem>();
            for(RequestItem item : filter.getConfigurations()) {
                DBItemInventoryConfiguration inventoryDbItem = dblayer.getConfigurationByName(item.getName(), ConfigurationType.fromValue(item.getType()).intValue()).get(0);
                ReferencedDbItem reference = DependencyResolver.convert(hibernateSession, inventoryDbItem,
                        DependencyResolver.getStoredDependencies(hibernateSession, inventoryDbItem));
                if(reference != null) {
                    items.add(reference);
                }
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(getResponse(items, hibernateSession)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private GetDependenciesResponse getResponse(List<ReferencedDbItem> items, SOSHibernateSession session) {
        
        GetDependenciesResponse dependenciesResponse = new GetDependenciesResponse();
        dependenciesResponse.setDeliveryDate(Date.from(Instant.now()));
        for(ReferencedDbItem referencedDbItem : items) {
            ResponseItem response = new ResponseItem();
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
            response.setReferences(referencedDbItem.getReferences().stream().map(item -> {
                        try {
                            return JocInventory.convert(item, session);
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        } catch (IOException e) {
                            throw new JocException(e);
                        }
                    }).collect(Collectors.toList()));
            dependenciesResponse.getDependencies().add(response);
        }
        return dependenciesResponse;
    }

//    private ResponseItems getResponseItems (ReferencedDbItem referencedDbItem, SOSHibernateSession session) throws SOSHibernateException {
//        ResponseItems dependencyItems = new ResponseItems();
//        referencedDbItem.getReferencedBy().stream().forEach(item -> {
//            try {
//                switch(item.getTypeAsEnum()) {
//                case WORKFLOW:
//                    dependencyItems.getWorkflows().add(WorkflowConverter.convertInventoryWorkflow(item.getContent(), Workflow.class));
//                    break;
//                case FILEORDERSOURCE:
//                    dependencyItems.getFileOrderSources().add(JocInventory.convertFileOrderSource(item.getContent(), FileOrderSource.class));
//                    break;
//                case JOBTEMPLATE:
//                    dependencyItems.getJobTemplates().add(JocInventory.convertJobTemplate(item.getContent(), JobTemplate.class));
//                    break;
//                case JOBRESOURCE:
//                    dependencyItems.getJobResources().add(JocInventory.convertDefault(item.getContent(), JobResource.class));
//                    break;
//                case NOTICEBOARD:
//                    dependencyItems.getBoards().add(JocInventory.convertDefault(item.getContent(), Board.class));
//                    break;
//                case LOCK:
//                    dependencyItems.getLocks().add(JocInventory.convertDefault(item.getContent(), Lock.class));
//                    break;
//                case SCHEDULE:
//                    dependencyItems.getSchedules().add(JocInventory.convertSchedule(item.getContent(), Schedule.class));
//                    break;
//                case WORKINGDAYSCALENDAR:
//                case NONWORKINGDAYSCALENDAR:
//                    dependencyItems.getCalendars().add(JocInventory.convertDefault(item.getContent(), Calendar.class));
//                    break;
//                default:
//                    break;
//                }
//            } catch (IOException e) {
//                throw new JocConfigurationException(e);
//            }
//        });
//        InventoryDBLayer dblayer = new InventoryDBLayer(session);
//        dependencyItems.setIsRenamed(dblayer.isRenamed(referencedDbItem.getName(), referencedDbItem.getType()));
//        dependencyItems.setDeliveryDate(Date.from(Instant.now()));
//        return dependencyItems;
//    }
    
}
