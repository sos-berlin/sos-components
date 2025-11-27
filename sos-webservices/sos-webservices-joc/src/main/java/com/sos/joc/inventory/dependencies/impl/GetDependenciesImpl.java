package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.RequestItem;
import com.sos.joc.model.inventory.dependencies.get.Response;
import com.sos.joc.model.inventory.dependencies.get.ResponseObject;
import com.sos.joc.model.inventory.dependencies.get.ResponseObjects;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory")
public class GetDependenciesImpl extends JOCResourceImpl implements IGetDependencies {
    
    private static final String API_CALL = "./inventory/dependencies";
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
        switch(filter.getOperationType()) {
        case DEPLOY:
        case RELEASE:
        case EXPORT:
        case GIT:
            return createPublishResponse(filter, dbLayer);
        case RECALL:
        case REVOKE:
        case REMOVE:
            return createRemoveResponse(filter, dbLayer);
        default: 
            return createPublishResponse(filter, dbLayer);
        }
    }
    
    private Response createPublishResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Response response = initResponse(filter, dbLayer);
        ResponseObjects objects = response.getObjects();

        Stream.concat(allItemsWithReferences.keySet().stream(), allItemsReferencedBy.keySet().stream()).distinct().map(d -> map(d,
                allItemsWithReferences.get(d), allItemsReferencedBy.get(d))).forEach(ro -> {
                    objects.setAdditionalProperty(ro.getId().toString(), ro);
                });

        response.setObjects(objects);
        response.setDeliveryDate(Date.from(Instant.now()));
        return response;
    }

    private Response createRemoveResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Response response = initResponse(filter, dbLayer);
        ResponseObjects objects = response.getObjects();

        Set<Long> publishedInvIds = dbLayer.checkPublished(allItemsReferencedBy.values().stream().flatMap(Set::stream).distinct().map(
                Dependency::getId).toList());

        Stream.concat(allItemsWithReferences.keySet().stream(), allItemsReferencedBy.keySet().stream()).distinct().map(d -> map(d,
                allItemsWithReferences.get(d), allItemsReferencedBy.get(d), publishedInvIds)).forEach(ro -> {
                    objects.setAdditionalProperty(ro.getId().toString(), ro);
                });

        response.setObjects(objects);
        response.setDeliveryDate(Date.from(Instant.now()));
        return response;
    }
    
    private Response initResponse(GetDependenciesRequest filter, DBLayerDependencies dbLayer) throws SOSHibernateException {
        Set<RequestItem> requestItems = filter.getConfigurations().stream().collect(Collectors.toSet());
        Map<RequestItem, Long> reqDeps = fillReferences(requestItems, dbLayer);

        Response response = new Response();
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
        response.setObjects(objects);
        return response;
    }

    private Map<RequestItem, Long> fillReferences(Set<RequestItem> requestItems, DBLayerDependencies dbLayer) throws SOSHibernateException {
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

        return Stream.concat(itemsWithReferences.keySet().stream(), itemsReferencedBy.keySet().stream()).distinct().collect(Collectors.toMap(Function
                .identity(), Dependency::getId, (K1, K2) -> K1));
    }

    private ResponseObject map(Dependency dependency, Set<Dependency> references, Set<Dependency> referencedBys) {
        ResponseObject cfg = map(dependency);
        if (references != null) {
            Map<Boolean, Set<Long>> refs = references.stream().collect(Collectors.groupingBy(Dependency::getEnforce, Collectors.mapping(
                    Dependency::getId, Collectors.toSet())));
            cfg.setReferences(refs.getOrDefault(false, Collections.emptySet()));
            cfg.setEnforcedReferences(refs.getOrDefault(true, Collections.emptySet()));
        }
        if (referencedBys != null) {
            Map<Boolean, Set<Long>> refs = referencedBys.stream().collect(Collectors.groupingBy(Dependency::getEnforce, Collectors.mapping(
                    Dependency::getId, Collectors.toSet())));
            cfg.setReferencedBy(refs.getOrDefault(false, Collections.emptySet()));
            cfg.setEnforcedReferencedBy(refs.getOrDefault(true, Collections.emptySet()));
        }
        return cfg;
    }

    private ResponseObject map(Dependency dependency, Set<Dependency> references, Set<Dependency> referencedBys, Set<Long> publishedInvIds) {
        ResponseObject cfg = map(dependency);
        if (references != null) {
            cfg.setReferences(references.stream().map(Dependency::getId).collect(Collectors.toSet()));
            cfg.setEnforcedReferences(Collections.emptySet());
        }
        if (referencedBys != null) {
            Map<Boolean, Set<Long>> published = referencedBys.stream().map(Dependency::getId).collect(Collectors.groupingBy(publishedInvIds::contains,
                    Collectors.toSet()));
            cfg.setReferencedBy(published.getOrDefault(false, Collections.emptySet()));
            cfg.setEnforcedReferencedBy(published.getOrDefault(true, Collections.emptySet()));
        }
        return cfg;
    }
    
    private ResponseObject map(Dependency dependency) {
        ResponseObject cfg = new ResponseObject();
        cfg.setId(dependency.getId());
        cfg.setObjectType(dependency.getType());
        cfg.setName(dependency.getName());
        cfg.setPath(dependency.getPath());
        cfg.setValid(dependency.getValid());
        cfg.setDeployed(dependency.getDeployed());
        cfg.setReleased(dependency.getReleased());
        cfg.setDeployments(null);
        return cfg;
    }
    
}
