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
    
    private static Map<Dependency, Set<Dependency>> resolveReferencedBy(List<DBItemInventoryExtendedDependency> dependencies) {
        if(dependencies != null && !dependencies.isEmpty()) {
            return dependencies.stream()
                    .collect(Collectors.groupingBy(DBItemInventoryExtendedDependency::getDependency, 
                            Collectors.mapping(DBItemInventoryExtendedDependency::getReference, Collectors.toSet())));
        } else {
            return Collections.emptyMap();
        }
    }
    
    private static Map<Dependency, Set<Dependency>> resolveReferences(List<DBItemInventoryExtendedDependency> dependencies) {
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
            req.setType(dep.getType());
            return !requestItems.contains(req);
        });
        
        Map<Dependency, Set<Dependency>> itemsReferencedBy = resolveReferencedBy(dependencies);
        ResponseItem response = new ResponseItem();
        Stream<RequestedResponseItem> refs = itemsWithReferences.entrySet().stream().map(entry -> {
            RequestedResponseItem item = new RequestedResponseItem();
            ConfigurationObject cfg = new ConfigurationObject();
            Dependency requested = entry.getKey();
            cfg.setObjectType(requested.getType());
            cfg.setName(requested.getName());
            cfg.setPath(resolvePath(requested.getFolder(), requested.getName()));
            cfg.setValid(requested.getValid());
            cfg.setDeployed(requested.getDeployed());
            cfg.setReleased(requested.getReleased());
            item.setType(requested.getType());
            item.setName(requested.getName());
            item.setConfiguration(cfg);
            item.getReferences().addAll(entry.getValue().stream().map(GetDependenciesImpl::convert).collect(Collectors.toSet()));
            if(itemsReferencedBy.get(entry.getKey()) != null) {
                item.getReferencedBy().addAll(itemsReferencedBy.get(entry.getKey()).stream()
                        .map(GetDependenciesImpl::convert).collect(Collectors.toSet()));
            }
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
        return response;
    }

    private static ConfigurationObject convert(Dependency dependency) {
        ConfigurationObject config = new ConfigurationObject();
        if(dependency.getType() != null) {
            config.setObjectType(dependency.getType());
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
