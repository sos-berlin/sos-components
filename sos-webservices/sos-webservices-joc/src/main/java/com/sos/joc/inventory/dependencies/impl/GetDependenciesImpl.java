package com.sos.joc.inventory.dependencies.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.common.Dependency;
import com.sos.joc.db.inventory.DBItemInventoryExtendedDependency;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
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
//            DependencyItems depItems = getRelatedDependencyItems(filter, dblayer);
            GetDependenciesResponse response  = new GetDependenciesResponse();
            response.setDeliveryDate(Date.from(Instant.now()));
//            response.setDependencies(resolve(depItems, dblayer));
            response.setDependencies(getResponse(filter, dblayer));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
//    private static DependencyItems getRelatedDependencyItems(GetDependenciesRequest filter, InventoryDBLayer dblayer) throws SOSHibernateException {
//        List<RequestItem> requestItems = filter.getConfigurations();
//        DBLayerDependencies depDbLayer = new DBLayerDependencies(dblayer.getSession());
//        
//        // get all inventory objects from request 
//        List<DBItemInventoryConfiguration> reqDbItems = requestItems.stream().map(item -> {
//            return dblayer.getConfigurationByName(item.getName(), ConfigurationType.fromValue(item.getType()).intValue());
//        }).flatMap(List::stream).distinct().collect(Collectors.toList());
//        if (!reqDbItems.isEmpty()) {
//            // get all dependencies for requested inventory objects
//            Map<Long, DBItemInventoryConfiguration> allUniqueItems = reqDbItems.stream()
//                    .collect(Collectors.toMap(item -> item.getId(), Function.identity()));
//            DependencyItems resultItems = new DependencyItems(allUniqueItems);
//            
//            for(DBItemInventoryConfiguration requestedItem : reqDbItems) {
//                DependencyItem item = new DependencyItem(requestedItem);
//                List<DBItemInventoryDependency> unsortedDependencies = depDbLayer.getRequestedDependencies(requestedItem);
//                // Map<Long: id of starting object, List<Long: id referencedBy objects>>
//                Map<Long, List<Long>> groupedDepInventoryIds = unsortedDependencies.stream()
//                        .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
//                                Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
//                // add all primary referenced by Ids
//                Set<Long> referencedByIds = groupedDepInventoryIds.values().stream().flatMap(List::stream)
//                        .filter(id -> !id.equals(item.getRequestedItem().getId())).collect(Collectors.toSet());
//                item.getReferencedByIds().addAll(referencedByIds);
//                
//                // add all primary references Ids
//                Set<Long> referencesIds = groupedDepInventoryIds.entrySet().stream().map(entry -> {
//                    if(entry.getValue().contains(item.getRequestedItem().getId())) {
//                        return entry.getKey();
//                    } else {
//                        return null;
//                    }
//                }).filter(Objects::nonNull).collect(Collectors.toSet());
//                item.getReferencesIds().addAll(referencesIds);
//                resultItems.getRequesteditems().add(item);
//                // all current referenced ids which are not already processed to check for further references
//                Set<Long> allCurrentIds = Stream.concat(referencedByIds.stream(),referencesIds.stream())
//                        .filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet());
//                OperationType type = filter.getOperationType();
//                // add all items once to allUniqueItems, recursively 
//                resolveReferencesRecursively(allUniqueItems, allCurrentIds, type, dblayer, depDbLayer);
//                
//            }
//            resultItems.setAllUniqueItems(allUniqueItems);
//            return resultItems;
//        }
//        return new DependencyItems(Collections.emptyMap());
//    }
    
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
    
//    private ResponseItem resolve(DependencyItems items, InventoryDBLayer dblayer) {
//        ResponseItem newResponseItem = new ResponseItem();
//        Map <Long, DBItemInventoryConfiguration> allUniqueItems =  items.getAllUniqueItems();
//        Set <DependencyItem> requested = items.getRequesteditems();
//        for(DependencyItem requestedItem : requested) {
//            RequestedResponseItem reqRes = new RequestedResponseItem();
//            DBItemInventoryConfiguration reqItem =  requestedItem.getRequestedItem();
//            if(reqItem != null) {
//                reqRes.setConfiguration(DependencyResolver.convert(reqItem));
//                reqRes.setName(reqItem.getName());
//                reqRes.setType(reqItem.getTypeAsEnum().value());
//                allUniqueItems.remove(reqItem.getId());
//            }
//            reqRes.setReferencedBy(requestedItem.getReferencedByIds().stream().map(id -> {
//                try {
//                    return dblayer.getConfiguration(id);
//                } catch (SOSHibernateException e) {
//                    throw new JocSosHibernateException(e);
//                }
//            }).filter(Objects::nonNull).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
//            
//            reqRes.setReferences(requestedItem.getReferencesIds().stream().map(id -> {
//                try {
//                    return dblayer.getConfiguration(id);
//                } catch (SOSHibernateException e) {
//                    throw new JocSosHibernateException(e);
//                }
//            }).filter(Objects::nonNull).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
//            newResponseItem.getRequestedItems().add(reqRes);
//            // remove all referencedBy and all references items already present from the affectedItems
//            requestedItem.getReferencedByIds().forEach(key -> allUniqueItems.remove(key));
//            requestedItem.getReferencesIds().forEach(key -> allUniqueItems.remove(key));
//        }
//        newResponseItem.setAffectedItems(allUniqueItems.values().stream().filter(Objects::nonNull)
//                .map(item -> convert(item)).collect(Collectors.toSet()));
//        return newResponseItem;
//    }
    
//    public static AffectedResponseItem convert(DBItemInventoryConfiguration item) {
//        AffectedResponseItem responseItem = new AffectedResponseItem();
//        responseItem.setItem(DependencyResolver.convert(item));
//        responseItem.setDraft(!(item.getDeployed() || item.getReleased()));
//        return responseItem;
//    }

    private static Map<Dependency, Set<Dependency>> resolveReferences(List<DBItemInventoryExtendedDependency> dependencies) {
        if(dependencies != null && !dependencies.isEmpty()) {
            return dependencies.stream()
                    .collect(Collectors.groupingBy(DBItemInventoryExtendedDependency::getDependency, 
                            Collectors.mapping(DBItemInventoryExtendedDependency::getReference, Collectors.toSet())));
        } else {
            return Collections.emptyMap();
        }
    }
    
    private static Map<Dependency, Set<Dependency>> resolveReferencedBy(List<DBItemInventoryExtendedDependency> dependencies) {
        if(dependencies != null && !dependencies.isEmpty()) {
            return dependencies.stream()
                    .collect(Collectors.groupingBy(DBItemInventoryExtendedDependency::getReference, 
                            Collectors.mapping(DBItemInventoryExtendedDependency::getDependency, Collectors.toSet())));
        } else {
            return Collections.emptyMap();
        }
    }
    
    private static ResponseItem getResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        List<DBItemInventoryExtendedDependency> dependencies = dbLayer.getAllExtendedDependencies();
        List<RequestItem> requestItems = filter.getConfigurations();
        Map<Dependency, Set<Dependency>> itemsWithReferences = resolveReferences(dependencies);
        // remove all keys that are not present in the filter
        itemsWithReferences.keySet().removeIf(dep -> {
            RequestItem req = new RequestItem();
            req.setName(dep.getName());
            req.setType(ConfigurationType.fromValue(dep.getType()).name());
            if(requestItems.contains(req)) {
                return false;
            } else {
                return true;
            }
        });
        Map<Dependency, Set<Dependency>> itemsReferencedBy = resolveReferencedBy(dependencies);
        ResponseItem response = new ResponseItem();
        response.setRequestedItems(itemsWithReferences.entrySet().stream().map(entry -> {
            RequestedResponseItem item = new RequestedResponseItem();
            ConfigurationObject cfg = new ConfigurationObject();
            Dependency requested = entry.getKey();
            cfg.setObjectType(ConfigurationType.fromValue(requested.getType()));
            cfg.setName(requested.getName());
            cfg.setPath(resolvePath(requested.getFolder(), requested.getName()));
            cfg.setValid(requested.getValid());
            cfg.setDeployed(requested.getDeployed());
            cfg.setReleased(requested.getReleased());
            item.setType(ConfigurationType.fromValue(requested.getType()).name());
            item.setName(requested.getName());
            item.setConfiguration(cfg);
            item.getReferences().addAll(entry.getValue().stream().map(GetDependenciesImpl::convert).collect(Collectors.toSet()));
            if(itemsReferencedBy.get(entry.getKey()) != null) {
                item.getReferencedBy().addAll(itemsReferencedBy.get(entry.getKey()).stream()
                        .map(GetDependenciesImpl::convert).collect(Collectors.toSet()));
            }
            return item;
        }).collect(Collectors.toSet()));
        if(response.getAffectedItems().isEmpty()) {
            response.setAffectedItems(null);
        }
        return response;
    }

    private static ConfigurationObject convert(Dependency dependency) {
        ConfigurationObject config = new ConfigurationObject();
        if(dependency.getType() != null) {
            config.setObjectType(ConfigurationType.fromValue(dependency.getType()));
            config.setName(dependency.getName());
            config.setPath(resolvePath(dependency.getFolder(), dependency.getName()));
            config.setValid(dependency.getValid());
            config.setDeployed(dependency.getDeployed());
            config.setReleased(dependency.getReleased());
        }
        return config;
    }
    
    private static String resolvePath(String folder, String name) {
        if("/".equals(folder)) {
            return name != null ? folder + name : folder;
        } else {
            return Paths.get(folder).resolve(name).toString().replace('\\', '/');
        }
    }
}
