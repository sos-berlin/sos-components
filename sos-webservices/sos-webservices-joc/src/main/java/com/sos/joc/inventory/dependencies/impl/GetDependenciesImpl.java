package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
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
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.inventory.dependencies.wrapper.DependencyItem;
import com.sos.joc.inventory.dependencies.wrapper.DependencyItems;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.dependencies.GetDependenciesRequest;
import com.sos.joc.model.inventory.dependencies.GetDependenciesResponse;
import com.sos.joc.model.inventory.dependencies.RequestItem;
import com.sos.joc.model.inventory.dependencies.get.AffectedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.RequestedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
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
            DependencyItems depItems = getRelatedDependencyItems(filter, dblayer);
            GetDependenciesResponse response  = new GetDependenciesResponse();
            response.setDeliveryDate(Date.from(Instant.now()));
            response.setDependencies(resolve(depItems, dblayer));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(response));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private static DependencyItems getRelatedDependencyItems(GetDependenciesRequest filter, InventoryDBLayer dblayer) throws SOSHibernateException {
        List<RequestItem> requestItems = filter.getConfigurations();
        // get all inventory objects from request 
        List<DBItemInventoryConfiguration> reqDbItems = requestItems.stream().map(item -> {
            return dblayer.getConfigurationByName(item.getName(), ConfigurationType.fromValue(item.getType()).intValue());
        }).flatMap(List::stream).distinct().collect(Collectors.toList());
        if (!reqDbItems.isEmpty()) {
            // get all dependencies for requested inventory objects
            Map<Long, DBItemInventoryConfiguration> allUniqueItems = reqDbItems.stream()
                    .collect(Collectors.toMap(item -> item.getId(), Function.identity()));
            DependencyItems resultItems = new DependencyItems(allUniqueItems);
            
            for(DBItemInventoryConfiguration requestedItem : reqDbItems) {
                DependencyItem item = new DependencyItem(requestedItem);
                List<DBItemInventoryDependency> unsortedDependencies = dblayer.getRequestedDependencies(requestedItem);
                // Map<Long: id of starting object, List<Long: id referencedBy objects>>
                Map<Long, List<Long>> groupedDepInventoryIds = unsortedDependencies.stream()
                        .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
                                Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
                // add all primary referenced by Ids
                Set<Long> referencedByIds = groupedDepInventoryIds.values().stream().flatMap(List::stream)
                        .filter(id -> !id.equals(item.getRequestedItem().getId())).collect(Collectors.toSet());
                item.getReferencedByIds().addAll(referencedByIds);
                
                // add all primary references Ids
                Set<Long> referencesIds = groupedDepInventoryIds.entrySet().stream().map(entry -> {
                    if(entry.getValue().contains(item.getRequestedItem().getId())) {
                        return entry.getKey();
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toSet());
                item.getReferencesIds().addAll(referencesIds);
                resultItems.getRequesteditems().add(item);
                // all current referenced ids which are not already processed to check for further references
                Set<Long> allCurrentIds = Stream.concat(referencedByIds.stream(),referencesIds.stream())
                        .filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet());
                // add all items once to allUniqueItems, recursively 
                resolveReferencesRecursively(allUniqueItems, allCurrentIds, dblayer);
                
            }
            resultItems.setAllUniqueItems(allUniqueItems);
            return resultItems;
        }
        return new DependencyItems(Collections.emptyMap());
    }
    
    private static void resolveReferencesRecursively(Map <Long, DBItemInventoryConfiguration> allUniqueItems, Set<Long> referencedIds,
            InventoryDBLayer dblayer) {
        if(!referencedIds.isEmpty()) {
            Set<DBItemInventoryConfiguration> referencedInvItems = referencedIds.stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            if(!referencedInvItems.isEmpty()) {
                Map<Long, DBItemInventoryConfiguration> currentUniqueItems = referencedInvItems.stream()
                        .collect(Collectors.toMap(item -> item.getId(), Function.identity()));
                allUniqueItems.putAll(currentUniqueItems);
                Set<Long> innerRefItems = currentUniqueItems.keySet().stream().map(id -> {
                    try {
                        return dblayer.getReferencesByIds(id);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }).flatMap(Set::stream).filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet());
                // recursion
                if(!innerRefItems.isEmpty()) {
                    resolveReferencesRecursively(allUniqueItems, innerRefItems, dblayer);
                }
            }
        }
    }
    
    private ResponseItem resolve(DependencyItems items, InventoryDBLayer dblayer) {
        ResponseItem newResponseItem = new ResponseItem();
        Map <Long, DBItemInventoryConfiguration> allUniqueItems =  items.getAllUniqueItems();
        Set <DependencyItem> requested = items.getRequesteditems();
        for(DependencyItem requestedItem : requested) {
            RequestedResponseItem reqRes = new RequestedResponseItem();
            DBItemInventoryConfiguration reqItem =  requestedItem.getRequestedItem();
            if(reqItem != null) {
                reqRes.setConfiguration(DependencyResolver.convert(reqItem));
                reqRes.setName(reqItem.getName());
                reqRes.setType(reqItem.getTypeAsEnum().value());
            }
            reqRes.setReferencedBy(requestedItem.getReferencedByIds().stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).filter(Objects::nonNull).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
            
            reqRes.setReferences(requestedItem.getReferencesIds().stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).filter(Objects::nonNull).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
            newResponseItem.getRequestedItems().add(reqRes);
        }
        newResponseItem.setAffectedItems(allUniqueItems.values().stream().filter(Objects::nonNull)
                .map(item -> convert(item)).collect(Collectors.toSet()));
        return newResponseItem;
    }
    
    public static AffectedResponseItem convert(DBItemInventoryConfiguration item) {
        AffectedResponseItem responseItem = new AffectedResponseItem();
        responseItem.setItem(DependencyResolver.convert(item));
        responseItem.setDraft(!(item.getDeployed() || item.getReleased()));
        return responseItem;
    }

}
