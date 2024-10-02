package com.sos.joc.inventory.dependencies.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import com.sos.joc.inventory.dependencies.resource.IGetDependencies;
import com.sos.joc.model.inventory.ConfigurationObject;
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
            // TODO: resolve complete DependencyTree 
            List<ResponseItem> dependencies = process(filter, hibernateSession);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(
                    getResponse(dependencies)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    private List<ResponseItem> process(GetDependenciesRequest filter, SOSHibernateSession session) throws SOSHibernateException,
            JsonMappingException, JsonProcessingException {
        InventoryDBLayer dblayer = new InventoryDBLayer(session);
        List<ResponseItem> items = new ArrayList<ResponseItem>();
        // read all dependency items from DB
        List<DBItemInventoryDependency> allDependencies = dblayer.getAllDependencies();
        
        // get all inventory Ids for all items and their dependencies
        List<Long> invIds = allDependencies.stream().map(dep -> Arrays.asList(dep.getInvId(), dep.getInvDependencyId()))
                .flatMap(List::stream).distinct().collect(Collectors.toList());
        // get all inventory objects from db
        Map<Long, DBItemInventoryConfiguration> cfgs = dblayer.getConfigurations(invIds).stream()
                .collect(Collectors.toMap(DBItemInventoryConfiguration::getId, Function.identity()));

        Map<Long, ConfigurationObject> cfgObjs = dblayer.getConfigurations(invIds).stream()
                .map(item -> DependencyResolver.convert(item)).collect(Collectors.toMap(ConfigurationObject::getId, Function.identity()));
        // group dependencies: <inventoryid of object, List of its referencedBy inventoryIds>
        Map<Long, List<Long>> groupedDependencyIds = allDependencies.stream()
                .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
                        Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
        // map 
        Map<ConfigurationObject, ResponseItem> dependencyToIdMap = Stream.concat(
                groupedDependencyIds.keySet().stream(), 
                groupedDependencyIds.values().stream().flatMap(List::stream))
                .distinct().map(id -> cfgObjs.get(id)).filter(Objects::nonNull).map(ResponseItem::new)
                .collect(Collectors.toMap(ResponseItem::getDependency, Function.identity()));
        
        
        Map<ConfigurationObject, ResponseItem> configurationMap = 
                new HashMap<ConfigurationObject, ResponseItem>();
        for(Map.Entry<Long, List<Long>> entry : groupedDependencyIds.entrySet()) {
            ResponseItem dep = dependencyToIdMap.get(cfgObjs.get(entry.getKey()));
            if(dep != null) {
                Set<ResponseItem> referencedBy = dep.getReferencedBy();
                entry.getValue().stream().map(id -> dependencyToIdMap.get(cfgObjs.get(id))).forEach(depCfg -> referencedBy.add(depCfg));
                configurationMap.put(cfgObjs.get(entry.getKey()), dep);
            }
        }
        resolveReferences(session, configurationMap, cfgObjs);
        
        for(RequestItem item : filter.getConfigurations()) {
            Optional<DBItemInventoryConfiguration> inventoryDbItemOptional = cfgs.values().stream()
                    .filter(cfg -> cfg.getName().equals(item.getName()) 
                            && cfg.getTypeAsEnum().equals(ConfigurationType.fromValue(item.getType()))).findFirst();
            if(inventoryDbItemOptional.isPresent()) {
                DBItemInventoryConfiguration inventoryDbItem = inventoryDbItemOptional.get();
                ResponseItem depCfg = configurationMap.get(cfgObjs.get(inventoryDbItem.getId()));
                if(depCfg != null) {
                    items.add(depCfg);
                }
            }
        }
        return items;
    }
 
    public static List<ResponseItem> resolveReferences(SOSHibernateSession session, 
            Map<ConfigurationObject, ResponseItem> dependencyConfigurationMap, Map<Long, ConfigurationObject> cfgObjs)
                    throws SOSHibernateException,
                JsonMappingException, JsonProcessingException {
        // this method is in use
        List<ResponseItem> resolvedDependencies = new ArrayList<ResponseItem>();
        for(Map.Entry<ConfigurationObject, ResponseItem> entry : dependencyConfigurationMap.entrySet()) {
            ResponseItem newInventoryDependency = new ResponseItem(entry.getKey());
            newInventoryDependency.getReferencedBy().add(entry.getValue());
            resolveReferences(newInventoryDependency, cfgObjs, session);
            resolvedDependencies.add(newInventoryDependency);
        }
        return resolvedDependencies;
    }
    
    private static void resolveReferences(ResponseItem item, Map <Long, ConfigurationObject> cfgObjs, SOSHibernateSession session)
            throws SOSHibernateException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        Set<Long> referencesIds = dbLayer.getReferencesIds(item.getDependency().getId());
        if(!referencesIds.isEmpty()) {
            for(Long id : referencesIds) {
                item.getReferences().add(new ResponseItem(cfgObjs.get(id)));
            }
        }
    }

    private GetDependenciesResponse getResponse(List<ResponseItem> items)
            throws JsonParseException, JsonMappingException, SOSHibernateException, IOException {
        GetDependenciesResponse dependenciesResponse = new GetDependenciesResponse();
        dependenciesResponse.setDeliveryDate(Date.from(Instant.now()));
        for(ResponseItem item : items) {
            dependenciesResponse.getDependencies().add(item);
        }
        return dependenciesResponse;
    }

}
