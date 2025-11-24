package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.inventory.dependencies.util.DependencyUtils;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.GetDependenciesResponse;
import com.sos.joc.model.inventory.dependencies.RequestItem;
import com.sos.joc.model.inventory.dependencies.get.EnforcedConfigurationObject;
import com.sos.joc.model.inventory.dependencies.get.RequestedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.Response;
import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
import com.sos.joc.model.inventory.dependencies.get.ResponseObject;
import com.sos.joc.model.inventory.dependencies.get.ResponseObjects;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory")
public class GetDependenciesImpl extends JOCResourceImpl implements IGetDependencies {
    
    private static final String API_CALL = "./inventory/dependencies";
    private static final String API_CALL2 = "./inventory/dependencies2";
    private Map<Dependency, Set<Dependency>> allItemsWithReferences = new HashMap<>();
    private Map<Dependency, Set<Dependency>> allItemsReferencedBy = new HashMap<>();
    
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
    
    @Override
    public JOCDefaultResponse postGetDependenciesNew(String xAccessToken, byte[] dependencyFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            dependencyFilter = initLogging(API_CALL2, dependencyFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(dependencyFilter, GetDependenciesRequest.class);
            GetDependenciesRequest filter = Globals.objectMapper.readValue(dependencyFilter, GetDependenciesRequest.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(xAccessToken);
            DBLayerDependencies dblayer = new DBLayerDependencies(hibernateSession);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(createResponse(filter, dblayer)));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private void next(Set<Long> dependencyIds, List<DBItemInventoryExtendedDependency> dbDependencies) {
        if (dependencyIds == null || dependencyIds.isEmpty()) {
            return;
        }

        Set<DBItemInventoryExtendedDependency> deps = dbDependencies.stream().filter(Objects::nonNull).filter(item -> dependencyIds.contains(item
                .getInvId()) || dependencyIds.contains(item.getDepId())).collect(Collectors.toSet());

        Map<Dependency, Set<Dependency>> itemsWithReferences = DependencyUtils.resolveReferences(deps);
        Map<Dependency, Set<Dependency>> itemsReferencedBy = DependencyUtils.resolveReferencedBy(deps);
        itemsWithReferences.keySet().removeIf(d -> !dependencyIds.contains(d.getId()));
        itemsReferencedBy.keySet().removeIf(d -> !dependencyIds.contains(d.getId()));
        
        allItemsWithReferences.putAll(itemsWithReferences);
        allItemsReferencedBy.putAll(itemsReferencedBy);

        Set<Long> nextDependencyIds = Stream.concat(allItemsWithReferences.values().stream().flatMap(Set::stream), allItemsReferencedBy.values()
                .stream().flatMap(Set::stream)).filter(d1 -> !allItemsWithReferences.keySet().contains(d1)).filter(d1 -> !allItemsReferencedBy
                        .keySet().contains(d1)).map(Dependency::getId).collect(Collectors.toSet());

        next(nextDependencyIds, dbDependencies);
    }
    
    private Response createResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Response response = new Response();
        Set<RequestItem> requestItems = filter.getConfigurations().stream().collect(Collectors.toSet());
        List<DBItemInventoryExtendedDependency> dbDependencies = DependencyUtils.getAllDependencies(dbLayer.getSession());
        Set<DBItemInventoryExtendedDependency> dependencies = dbDependencies.stream().filter(Objects::nonNull).filter(item -> requestItems.contains(
                item.getInvRequestItem()) || requestItems.contains(item.getDepRequestItem())).collect(Collectors.toSet());
        Map<Dependency, Set<Dependency>> itemsWithReferences = DependencyUtils.resolveReferences(dependencies);
        Map<Dependency, Set<Dependency>> itemsReferencedBy = DependencyUtils.resolveReferencedBy(dependencies);
        itemsWithReferences.keySet().retainAll(requestItems);
        itemsReferencedBy.keySet().retainAll(requestItems);

        allItemsWithReferences.putAll(itemsWithReferences);
        allItemsReferencedBy.putAll(itemsReferencedBy);

        Set<Long> nextDependencyIds = Stream.concat(allItemsWithReferences.values().stream().flatMap(Set::stream), allItemsReferencedBy.values()
                .stream().flatMap(Set::stream)).filter(d1 -> !allItemsWithReferences.keySet().contains(d1)).filter(d1 -> !allItemsReferencedBy
                        .keySet().contains(d1)).map(Dependency::getId).collect(Collectors.toSet());

        next(nextDependencyIds, dbDependencies);

        Map<RequestItem, Long> reqDeps = Stream.concat(itemsWithReferences.keySet().stream(), itemsReferencedBy.keySet().stream()).distinct()
                .collect(Collectors.toMap(Function.identity(), Dependency::getId, (K1, K2) -> K1));

        ResponseObjects objects = new ResponseObjects();
        Map<Boolean, Set<RequestItem>> m = requestItems.stream().collect(Collectors.groupingBy(reqDeps::containsKey, Collectors.toSet()));
        Set<Long> requestedIds = m.getOrDefault(true, Collections.emptySet()).stream().map(reqDeps::get).collect(Collectors.toSet());
        m.getOrDefault(false, Collections.emptySet()).forEach(i -> {
            dbLayer.getResponseObject(i.getName(), i.getType()).ifPresent(ro -> {
                ro.setDeployments(null);
                objects.setAdditionalProperty(ro.getId().toString(), ro);
                requestedIds.add(ro.getId());
            });
        });
        
        response.setRequestedItems(requestedIds);
        
        Stream.concat(allItemsWithReferences.keySet().stream(), allItemsReferencedBy.keySet().stream()).distinct().map(d -> map(d,
                allItemsWithReferences.get(d), allItemsReferencedBy.get(d))).forEach(ro -> {
                    objects.setAdditionalProperty(ro.getId().toString(), ro);
                });
        
        response.setObjects(objects);
        response.setDeliveryDate(Date.from(Instant.now()));
        return response;
    }
    
    private ResponseObject map(Dependency dependency, Set<Dependency> references, Set<Dependency> referencedBys) {
        ResponseObject cfg = new ResponseObject();
        cfg.setId(dependency.getId());
        cfg.setObjectType(dependency.getType());
        cfg.setName(dependency.getName());
        cfg.setPath(dependency.getPath());
        cfg.setValid(dependency.getValid());
        cfg.setDeployed(dependency.getDeployed());
        cfg.setReleased(dependency.getReleased());
        cfg.setDeployments(null);
        if(references != null) {
            Map<Boolean,Set<Long>> refs = references.stream()
                    .collect(Collectors.groupingBy(Dependency::getEnforce, Collectors.mapping(Dependency::getId, Collectors.toSet())));
            cfg.setReferences(refs.getOrDefault(false, Collections.emptySet()));
            cfg.setEnforcedReferences(refs.getOrDefault(true, Collections.emptySet()));
        }
        if(referencedBys != null) {
            Map<Boolean,Set<Long>> refs = referencedBys.stream()
                    .collect(Collectors.groupingBy(Dependency::getEnforce, Collectors.mapping(Dependency::getId, Collectors.toSet())));
            cfg.setReferencedBy(refs.getOrDefault(false, Collections.emptySet()));
            cfg.setEnforcedReferencedBy(refs.getOrDefault(true, Collections.emptySet()));
        }
        return cfg;
    }
    
    private static ResponseItem getResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Set<RequestItem> requestItems = new HashSet<RequestItem>(filter.getConfigurations());
        List<DBItemInventoryExtendedDependency> dependencies = DependencyUtils.getAllDependencies(dbLayer.getSession());
        dependencies = dependencies.stream().filter(Objects::nonNull)
                .filter(item -> requestItems.contains(item.getInvRequestItem()) || requestItems.contains(item.getDepRequestItem())).collect(Collectors.toList());
        Map<Dependency, Set<Dependency>> itemsWithReferences = DependencyUtils.resolveReferences(dependencies);
        Map<Dependency, Set<Dependency>> itemsReferencedBy = DependencyUtils.resolveReferencedBy(dependencies);
        
        Map<RequestItem, Dependency> reqDeps = Stream.concat(itemsWithReferences.keySet().stream(), itemsReferencedBy.keySet().stream()).distinct()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (K1,K2) -> K1));
        
        ResponseItem response = new ResponseItem();
        response.setRequestedItems(requestItems.stream().map(item -> reqDeps.get(item)).filter(Objects::nonNull).map(dependency -> {
                    RequestedResponseItem rri = new RequestedResponseItem();
                    EnforcedConfigurationObject cfg = new EnforcedConfigurationObject();
                    cfg.setId(dependency.getId());
                    cfg.setObjectType(dependency.getType());
                    cfg.setName(dependency.getName());
                    cfg.setPath(dependency.getPath());
                    cfg.setValid(dependency.getValid());
                    cfg.setDeployed(dependency.getDeployed());
                    cfg.setReleased(dependency.getReleased());
                    rri.setType(dependency.getType());
                    rri.setName(dependency.getName());
                    rri.setConfiguration(cfg);
                    rri.setReferences(setDeployments(dbLayer.getSession(), DependencyUtils.convert(itemsWithReferences.get(dependency))));
                    rri.setReferencedBy(setDeployments(dbLayer.getSession(), DependencyUtils.convert(itemsReferencedBy.get(dependency))));
                    return rri;
                }).collect(Collectors.toSet()));

        if(response.getAffectedItems().isEmpty()) {
            response.setAffectedItems(null);
        }
        return response;
    }
    
    private static Set<EnforcedConfigurationObject> setDeployments(SOSHibernateSession session, Set<EnforcedConfigurationObject> configs) {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        return configs.stream().peek(item -> {
            List<InventoryDeploymentItem> deployments;
            try {
                deployments = dbLayer.getDeploymentHistory(item.getId());
                setDeployments(item, deployments);
            } catch (SOSHibernateException e) {
                item.setDeployments(null);
            }
        }).collect(Collectors.toSet());
    }
    
    private static void setDeployments(EnforcedConfigurationObject cfg, List<InventoryDeploymentItem> deployments) {
        cfg.setDeployments(deployments.stream().map(dep -> {
            ResponseDeployableVersion answer = new ResponseDeployableVersion();
            answer.setId(dep.getId());
            answer.setCommitId(dep.getCommitId());
            answer.setDeploymentOperation(OperationType.fromValue(dep.getOperation()).name());
            answer.setVersionDate(dep.getDeploymentDate());
            ResponseItemDeployment v = new ResponseItemDeployment();
            v.setControllerId(dep.getControllerId());
            answer.setVersions(Collections.singleton(v));
            return answer;
        }).collect(Collectors.toSet()));
    }
    
}
