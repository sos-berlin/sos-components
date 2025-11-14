package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.common.Dependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.inventory.dependencies.util.DependencyUtils;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.GetDependenciesResponse;
import com.sos.joc.model.inventory.dependencies.RequestItem;
import com.sos.joc.model.inventory.dependencies.get.RequestedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/dependencies")
public class GetDependenciesImpl extends JOCResourceImpl implements IGetDependencies {
    
    private static final String API_CALL = "./inventory/dependencies";
    private static final List<ConfigurationType> referencesType = Arrays.asList(
            ConfigurationType.WORKFLOW, ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE, 
            ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.JOBTEMPLATE);
    private static final List<ConfigurationType> referencedByType = Arrays.asList(
            ConfigurationType.WORKFLOW, ConfigurationType.JOBRESOURCE, ConfigurationType.LOCK, ConfigurationType.NOTICEBOARD,
            ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.INCLUDESCRIPT);
    
    @Override
    public JOCDefaultResponse postGetDependencies(String xAccessToken, byte[] dependencyFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            dependencyFilter = initLogging(API_CALL, dependencyFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(dependencyFilter, GetDependenciesRequest.class);
            GetDependenciesRequest filter = Globals.objectMapper.readValue(dependencyFilter, GetDependenciesRequest.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(xAccessToken);
            DBLayerDependencies dblayer = new DBLayerDependencies(hibernateSession);
            GetDependenciesResponse response  = new GetDependenciesResponse();
            response.setDeliveryDate(Date.from(Instant.now()));
            response.setDependencies(getResponse(filter, dblayer));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    
//    private static void resolveReferencesRecursively(Map <Long, DBItemInventoryConfiguration> allUniqueItems, Set<Long> referencedIds,
//            OperationType type, InventoryDBLayer dblayer, DBLayerDependencies depDbLayer) {
//        if(!referencedIds.isEmpty()) {
//            Set<DBItemInventoryConfiguration> referencedInvItems = referencedIds.stream().map(id -> {
//                try {
//                    return dblayer.getConfiguration(id);
//                } catch (SOSHibernateException e) {
//                    throw new JocSosHibernateException(e);
//                }
//            }).filter(Objects::nonNull).collect(Collectors.toSet());
//            if(!referencedInvItems.isEmpty()) {
//                Map<Long, DBItemInventoryConfiguration> currentUniqueItems = referencedInvItems.stream()
//                        .collect(Collectors.toMap(item -> item.getId(), Function.identity()));
//                Map<Long, DBItemInventoryConfiguration> currentUniqueReferencesItems = new HashMap<Long, DBItemInventoryConfiguration>();
//                Map<Long, DBItemInventoryConfiguration> currentUniqueReferencedByItems = new HashMap<Long, DBItemInventoryConfiguration>();
//                if (type != null) {
//                    switch (type) {
//                    case DEPLOY: 
//                        // filter: remove  all already released/deployed from further processing
//                        // filter: only referencesTypes for recursive references processing
//                        currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> !entry.getValue().getDeployed() && !entry.getValue().getReleased())
//                                .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> !entry.getValue().getDeployed() && !entry.getValue().getReleased())
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case RELEASE:
//                        // filter: remove  all already released/deployed from further processing
//                        // filter: only referencesTypes for recursive references processing
//                        currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> !entry.getValue().getDeployed() && !entry.getValue().getReleased())
//                                .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> !entry.getValue().getDeployed() && !entry.getValue().getReleased())
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case REVOKE:
//                        // filter: remove  all drafts from further processing
//                        // filter: referencesTypes not processed, empty map
//                        currentUniqueReferencesItems = Collections.emptyMap();
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> entry.getValue().getDeployed() || entry.getValue().getReleased())
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case RECALL:
//                        // filter: remove  all drafts from further processing
//                        // filter: referencesTypes not processed, empty map
//                        currentUniqueReferencesItems = Collections.emptyMap();
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> entry.getValue().getReleased())
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case REMOVE:
//                        // filter: remove  all drafts from further processing
//                        // filter: referencesTypes not processed, empty map
//                        currentUniqueReferencesItems = Collections.emptyMap();
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case EXPORT:
//                        // filter: remove  all drafts from further processing
//                        // filter: only referencesTypes for recursive references processing
//                        currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    case GIT:
//                        // filter: remove  all drafts from further processing
//                        // filter: only referencesTypes for recursive references processing
//                        currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    default:
//                        // filter: remove  all drafts from further processing
//                        // filter: only referencesTypes for recursive references processing
//                        currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        // filter: only referencedByTypes for recursive referencedBy processing
//                        currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                                .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                        break;
//                    }
//                } else {
//                    // filter: remove  all drafts from further processing
//                    // filter: only referencesTypes for recursive references processing
//                    currentUniqueReferencesItems = currentUniqueItems.entrySet().stream()
//                            .filter(entry -> referencesType.contains(entry.getValue().getTypeAsEnum()))
//                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                    // filter: only referencedByTypes for recursive referencedBy processing
//                    currentUniqueReferencedByItems = currentUniqueItems.entrySet().stream()
//                            .filter(entry -> referencedByType.contains(entry.getValue().getTypeAsEnum()))
//                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//                }
//
//                Set<Long> innerRefItems = new HashSet<Long>();
//                innerRefItems.addAll(currentUniqueReferencesItems.keySet().stream().map(id -> {
//                    try {
//                        return depDbLayer.getReferencesIds(id);
//                    } catch (SOSHibernateException e) {
//                        throw new JocSosHibernateException(e);
//                    }
//                }).flatMap(Set::stream).filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet()));
//                innerRefItems.addAll(currentUniqueReferencedByItems.keySet().stream().map(id -> {
//                    try {
//                        return depDbLayer.getReferencesByIds(id);
//                    } catch (SOSHibernateException e) {
//                        throw new JocSosHibernateException(e);
//                    }
//                }).flatMap(Set::stream).filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet()));
//                
//                allUniqueItems.putAll(currentUniqueItems);
//                // recursion
//                if(!innerRefItems.isEmpty()) {
//                    resolveReferencesRecursively(allUniqueItems, innerRefItems, type, dblayer, depDbLayer);
//                }
//            }
//        }
//    }
    
    private static ResponseItem getResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Set<RequestItem> requestItems = new HashSet<RequestItem>(filter.getConfigurations());
        List<DBItemInventoryExtendedDependency> dependencies = DependencyUtils.getAllDependencies(dbLayer.getSession()).stream()
                .filter(item -> requestItems.contains(item.getInvRequestItem()) || requestItems.contains(item.getDepRequestItem()))
                .collect(Collectors.toList());
        Map<Dependency, Set<Dependency>> itemsWithReferences = DependencyUtils.resolveReferences(dependencies);
        Map<Dependency, Set<Dependency>> itemsReferencedBy = DependencyUtils.resolveReferencedBy(dependencies);
        
        Map<RequestItem, Dependency> reqDeps = Stream.concat(itemsWithReferences.keySet().stream(), itemsReferencedBy.keySet().stream()).distinct()
                .collect(Collectors.toMap(Dependency::getRequestItem, Function.identity(), (K1,K2) -> K1));
        
        ResponseItem response = new ResponseItem();
        response.setRequestedItems(requestItems.stream().map(item -> reqDeps.get(item)).filter(Objects::nonNull).map(dependency -> {
                    RequestedResponseItem rri = new RequestedResponseItem();
                    ConfigurationObject cfg = new ConfigurationObject();
                    cfg.setObjectType(dependency.getType());
                    cfg.setName(dependency.getName());
                    cfg.setPath(DependencyUtils.resolvePath(dependency.getFolder(), dependency.getName()));
                    cfg.setValid(dependency.getValid());
                    cfg.setDeployed(dependency.getDeployed());
                    cfg.setReleased(dependency.getReleased());
                    rri.setType(dependency.getType());
                    rri.setName(dependency.getName());
                    rri.setConfiguration(cfg);
                    rri.setReferences(DependencyUtils.convert(itemsWithReferences.get(dependency)));
                    rri.setReferencedBy(DependencyUtils.convert(itemsReferencedBy.get(dependency)));
                    return rri;
                }).collect(Collectors.toSet()));

        if(response.getAffectedItems().isEmpty()) {
            response.setAffectedItems(null);
        }
        return response;
    }

}
