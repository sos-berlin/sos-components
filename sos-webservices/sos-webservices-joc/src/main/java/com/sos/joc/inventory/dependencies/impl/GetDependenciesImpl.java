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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sos.joc.model.inventory.dependencies.get.RequestedResponseItem;
import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/dependencies")
public class GetDependenciesImpl extends JOCResourceImpl implements IGetDependencies {
    
    private static final String API_CALL = "./inventory/dependencies";
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDependenciesImpl.class);
    
    @Override
    public JOCDefaultResponse postGetDependencies(String xAccessToken, byte[] dependencyFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, dependencyFilter, xAccessToken);
            JsonValidator.validate(dependencyFilter, GetDependenciesRequest.class);
            GetDependenciesRequest filter = Globals.objectMapper.readValue(dependencyFilter, GetDependenciesRequest.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(xAccessToken);
            // TODO: resolve complete DependencyTree 
            InventoryDBLayer dblayer = new InventoryDBLayer(hibernateSession);
            DependencyItems depItems = getRelatedDependencyItems(filter, dblayer);
            if(!depItems.getAllUniqueItems().isEmpty()) {
                depItems.getAllUniqueItems().entrySet().forEach(entry -> 
                LOGGER.info(entry.getKey() + " : " + entry.getValue().getName() + "::" + entry.getValue().getTypeAsEnum().value()));
            }
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
                item.getReferencedByIds().addAll(groupedDepInventoryIds.entrySet().stream().map(entry -> entry.getValue())
                        .flatMap(List::stream).filter(id -> !id.equals(item.getRequestedItem().getId()))
                        .collect(Collectors.toSet()));
                // add all primary references Ids
                item.getReferencesIds().addAll(groupedDepInventoryIds.keySet().stream().map(id -> {
                    try {
                        return dblayer.getReferencesIds(id);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }).flatMap(Set::stream).collect(Collectors.toSet()));
                
                resultItems.getRequesteditems().add(item);
                // all current referenced by ids which are not already processed
                Set<Long> currentReferencedByIds = groupedDepInventoryIds.values().stream().flatMap(List::stream)
                        .filter(id -> !allUniqueItems.keySet().contains(id)).collect(Collectors.toSet());
                // add all items once to allUniqueItems, recursively 
                resolveRecursively(allUniqueItems, currentReferencedByIds, dblayer);
            }
            resultItems.setAllUniqueItems(allUniqueItems);
            return resultItems;
        }
        return new DependencyItems(Collections.emptyMap());
    }
    
    private static void resolveRecursively(Map <Long, DBItemInventoryConfiguration> allUniqueItems, Set<Long> referencedByIds, InventoryDBLayer dblayer) {
        if(!referencedByIds.isEmpty()) {
            Set<DBItemInventoryConfiguration> referencedByInvItems = referencedByIds.stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            if(!referencedByInvItems.isEmpty()) {
                Map<Long, DBItemInventoryConfiguration> currentUniqueRefByItems = referencedByInvItems.stream()
                        .collect(Collectors.toMap(item -> item.getId(), Function.identity()));
                allUniqueItems.putAll(currentUniqueRefByItems);
                Set<Long> innerRefByItems = currentUniqueRefByItems.keySet().stream().map(id -> {
                    try {
                        return dblayer.getReferencesByIds(id);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }).flatMap(Set::stream).collect(Collectors.toSet());
                // recursion
                if(!innerRefByItems.isEmpty()) {
                    resolveRecursively(allUniqueItems, innerRefByItems, dblayer);
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
            reqRes.setConfiguration(DependencyResolver.convert(reqItem));
            reqRes.setName(reqItem.getName());
            reqRes.setType(reqItem.getTypeAsEnum().value());

            reqRes.setReferencedBy(requestedItem.getReferencedByIds().stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
            
            reqRes.setReferences(requestedItem.getReferencesIds().stream().map(id -> {
                try {
                    return dblayer.getConfiguration(id);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).map(dbItem -> DependencyResolver.convert(dbItem)).collect(Collectors.toSet()));
            newResponseItem.getRequestedItems().add(reqRes);
        }
        newResponseItem.setAffectedItems(allUniqueItems.values().stream().map(item -> DependencyResolver.convert(item))
                .collect(Collectors.toSet()));
        return newResponseItem;
    }
    
//    private List<ResponseItem> process(GetDependenciesRequest filter, SOSHibernateSession session) throws SOSHibernateException,
//            JsonMappingException, JsonProcessingException {
//        InventoryDBLayer dblayer = new InventoryDBLayer(session);
//        List<ResponseItem> items = new ArrayList<ResponseItem>();
//        // read all dependency items from DB
//        List<DBItemInventoryDependency> allDependencies = dblayer.getAllDependencies();
//        
//        
//        // get all inventory Ids for all items and their dependencies
//        List<Long> invIds = allDependencies.stream().map(dep -> Arrays.asList(dep.getInvId(), dep.getInvDependencyId()))
//                .flatMap(List::stream).distinct().collect(Collectors.toList());
//        // get all inventory objects from db
//        List<DBItemInventoryConfiguration> invDbItems = dblayer.getConfigurations(invIds);
//        Map<Long, DBItemInventoryConfiguration> cfgs = invDbItems.stream()
//                .collect(Collectors.toMap(DBItemInventoryConfiguration::getId, Function.identity()));
//
//        Map<Long, ConfigurationObject> cfgObjs = invDbItems.stream()
//                .map(item -> DependencyResolver.convert(item)).collect(Collectors.toMap(ConfigurationObject::getId, Function.identity()));
//        // group dependencies: <inventoryid of object, List of its referencedBy inventoryIds>
//        Map<Long, List<Long>> groupedDependencyIds = allDependencies.stream()
//                .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
//                        Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
//        // map 
//        Map<Long, ResponseItem> dependencyToIdMap = Stream.concat(
//                groupedDependencyIds.keySet().stream(), 
//                groupedDependencyIds.values().stream().flatMap(List::stream))
//                .distinct().map(id -> cfgObjs.get(id)).filter(Objects::nonNull).map(ResponseItem::new)
//                .collect(Collectors.toMap(item -> item.getDependency().getId(), Function.identity()));
//        
//        
//        Map<ConfigurationObject, ResponseItem> configurationMap = 
//                new HashMap<ConfigurationObject, ResponseItem>();
//        for(Map.Entry<Long, List<Long>> entry : groupedDependencyIds.entrySet()) {
//            ResponseItem dep = dependencyToIdMap.get(entry.getKey());
//            if(dep != null) {
//                Set<ResponseItem> referencedBy = dep.getReferencedBy();
//                entry.getValue().stream().map(id -> dependencyToIdMap.get(id)).forEach(depCfg -> {
//                    if(depCfg.getDependency().getId() != entry.getKey()) {
//                        referencedBy.add(depCfg);
//                    }
//                });
//                configurationMap.put(cfgObjs.get(entry.getKey()), dep);
//            }
//        }
//        resolveReferences(session, configurationMap, dependencyToIdMap);
//        
//        for(RequestItem item : filter.getConfigurations()) {
//            Optional<DBItemInventoryConfiguration> inventoryDbItemOptional = cfgs.values().stream()
//                    .filter(cfg -> cfg.getName().equals(item.getName()) 
//                            && cfg.getTypeAsEnum().equals(ConfigurationType.fromValue(item.getType()))).findFirst();
//            if(inventoryDbItemOptional.isPresent()) {
//                DBItemInventoryConfiguration inventoryDbItem = inventoryDbItemOptional.get();
//                ResponseItem depCfg = configurationMap.get(cfgObjs.get(inventoryDbItem.getId()));
//                if(depCfg != null) {
//                    items.add(depCfg);
//                }
//            }
//        }
//        return items;
//    }
// 
//    public static List<ResponseItem> resolveReferences(SOSHibernateSession session, 
//            Map<ConfigurationObject, ResponseItem> dependencyConfigurationMap, Map<Long, ResponseItem> cfgObjs)
//                    throws SOSHibernateException,
//                JsonMappingException, JsonProcessingException {
//        // this method is in use
//        List<ResponseItem> resolvedDependencies = new ArrayList<ResponseItem>();
//        for(Map.Entry<ConfigurationObject, ResponseItem> entry : dependencyConfigurationMap.entrySet()) {
//            ResponseItem newInventoryDependency = cfgObjs.get(entry.getKey().getId());
////            newInventoryDependency.getReferencedBy().add(entry.getValue());
//            resolveReferences(newInventoryDependency, cfgObjs, session);
//            resolvedDependencies.add(newInventoryDependency);
//        }
//        return resolvedDependencies;
//    }
//    
//    private static void resolveReferences(ResponseItem item, Map<Long, ResponseItem> cfgObjs, SOSHibernateSession session)
//            throws SOSHibernateException {
//        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
//        Set<Long> referencesIds = dbLayer.getReferencesIds(item.getDependency().getId());
//        if(!referencesIds.isEmpty()) {
//            for(Long id : referencesIds) {
//                if(id != item.getDependency().getId()) {
//                    item.getReferences().add(cfgObjs.get(id));
//                }
//            }
//        }
//    }

}
