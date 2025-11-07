package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<DBItemInventoryExtendedDependency> dependencies = DependencyUtils.getAllDependencies(dbLayer.getSession());
        List<RequestItem> requestItems = filter.getConfigurations();
        Map<Dependency, Set<Dependency>> itemsWithReferences = DependencyUtils.resolveReferences(dependencies);
        // remove all keys that are not present in the filter
//        itemsWithReferences.keySet().removeIf(dep -> {
//            RequestItem req = new RequestItem();
//            req.setName(dep.getName());
//            req.setType(dep.getType());
//            return !requestItems.contains(req);
//        });
        Map<Dependency, Set<Dependency>> requestedItemsWithReferences = itemsWithReferences.entrySet().stream().filter(entry -> {
            RequestItem req = new RequestItem();
            req.setName(entry.getKey().getName());
            req.setType(entry.getKey().getType());
            return requestItems.contains(req);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); 
        Map<Dependency, Set<Dependency>> itemsReferencedBy = DependencyUtils.resolveReferencedBy(dependencies);
        Map<Dependency, Set<Dependency>> requestedByItemsWithReferences = itemsReferencedBy.entrySet().stream().filter(entry -> {
            RequestItem req = new RequestItem();
            req.setName(entry.getKey().getName());
            req.setType(entry.getKey().getType());
            return requestItems.contains(req);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); 
        ResponseItem response = new ResponseItem();
        if(!requestedItemsWithReferences.isEmpty()) {
            Stream<RequestedResponseItem> refs = requestedItemsWithReferences.entrySet().stream().map(entry -> {
                RequestedResponseItem item = new RequestedResponseItem();
                ConfigurationObject cfg = new ConfigurationObject();
                Dependency requested = entry.getKey();
                cfg.setObjectType(requested.getType());
                cfg.setName(requested.getName());
                cfg.setPath(DependencyUtils.resolvePath(requested.getFolder(), requested.getName()));
                cfg.setValid(requested.getValid());
                cfg.setDeployed(requested.getDeployed());
                cfg.setReleased(requested.getReleased());
                item.setType(requested.getType());
                item.setName(requested.getName());
                item.setConfiguration(cfg);
                item.getReferences().addAll(entry.getValue().stream().map(DependencyUtils::convert).collect(Collectors.toSet()));
                if(itemsReferencedBy.get(entry.getKey()) != null) {
                    item.getReferencedBy().addAll(itemsReferencedBy.get(entry.getKey()).stream()
                            .map(DependencyUtils::convert).collect(Collectors.toSet()));
                    response.getAffectedItems().addAll(itemsReferencedBy.get(entry.getKey()).stream()
                            .filter(referencedBy -> itemsWithReferences.containsKey(referencedBy))
                            .map(DependencyUtils::getAffectedResponseItem).collect(Collectors.toSet()));
                }
                response.getAffectedItems().addAll(entry.getValue().stream().filter(reference -> itemsWithReferences.containsKey(reference))
                        .map(DependencyUtils::getAffectedResponseItem).collect(Collectors.toSet()));
                return item;
            });
            Stream<RequestedResponseItem> reqs = requestItems.stream().map(requested -> {
                RequestedResponseItem item = new RequestedResponseItem();
                item.setType(requested.getType());
                item.setName(requested.getName());
                return item;
            });
            response.setRequestedItems(Stream.concat(refs, reqs).collect(Collectors.toSet()));
            if(response.getAffectedItems().isEmpty()) {
                response.setAffectedItems(null);
            }
        }
        if (!requestedByItemsWithReferences.isEmpty()) {
            Stream<RequestedResponseItem> refsBy = requestedByItemsWithReferences.entrySet().stream().map(entry -> {
                RequestedResponseItem item = new RequestedResponseItem();
                ConfigurationObject cfg = new ConfigurationObject();
                Dependency requested = entry.getKey();
                cfg.setObjectType(requested.getType());
                cfg.setName(requested.getName());
                cfg.setPath(DependencyUtils.resolvePath(requested.getFolder(), requested.getName()));
                cfg.setValid(requested.getValid());
                cfg.setDeployed(requested.getDeployed());
                cfg.setReleased(requested.getReleased());
                item.setType(requested.getType());
                item.setName(requested.getName());
                item.setConfiguration(cfg);
                item.getReferencedBy().addAll(entry.getValue().stream().map(DependencyUtils::convert).collect(Collectors.toSet()));
                if(itemsWithReferences.get(entry.getKey()) != null) {
                    item.getReferences().addAll(itemsWithReferences.get(entry.getKey()).stream()
                            .map(DependencyUtils::convert).collect(Collectors.toSet()));
                    response.getAffectedItems().addAll(itemsWithReferences.get(entry.getKey()).stream()
                            .filter(references -> itemsReferencedBy.containsKey(references))
                            .map(DependencyUtils::getAffectedResponseItem).collect(Collectors.toSet()));
                }
                response.getAffectedItems().addAll(entry.getValue().stream().filter(referencedBy -> itemsReferencedBy.containsKey(referencedBy))
                        .map(DependencyUtils::getAffectedResponseItem).collect(Collectors.toSet()));
                return item;
            });
            Stream<RequestedResponseItem> reqs = requestItems.stream().map(requested -> {
                RequestedResponseItem item = new RequestedResponseItem();
                item.setType(requested.getType());
                item.setName(requested.getName());
                return item;
            });
            response.setRequestedItems(Stream.concat(refsBy, reqs).collect(Collectors.toSet()));
            if(response.getAffectedItems().isEmpty()) {
                response.setAffectedItems(null);
            }
        }
        return response;
    }

}
