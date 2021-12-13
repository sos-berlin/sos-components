package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.ISubAgentStoreResource;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.StoreSubAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import js7.data_for_java.item.JUpdateItemOperation;

@Path("agent")
public class SubAgentStoreResourceImpl extends JOCResourceImpl implements ISubAgentStoreResource {

    private static String API_CALL_REMOVE = "./agent/subagents/store";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);

            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                throw new JocMissingLicenseException("missing license for Agent cluster");
            }

            JsonValidator.validateFailFast(filterBytes, StoreSubAgents.class);
            StoreSubAgents subAgentsParam = Globals.objectMapper.readValue(filterBytes, StoreSubAgents.class);

            String controllerId = subAgentsParam.getControllerId();
            String agentId = subAgentsParam.getAgentId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(subAgentsParam.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            DBItemInventoryAgentInstance dbAgent = dbLayer.getAgentInstance(agentId);
            if (dbAgent == null) {
                throw new JocBadRequestException("Cluster Agent '" + agentId + "' doesn't exist");
            }
            
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(controllerId));
            
//            List<JUpdateItemOperation> subAgentsToController = saveOrUpdate(dbLayer, dbAgent, dbSubAgents, subAgentsMap);
            saveOrUpdate(dbLayer, dbAgent, dbSubAgents, subAgentsParam.getSubagents());
            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            
//            if (!subAgentsToController.isEmpty()) {
//                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(subAgentsToController)).thenAccept(e -> ProblemHelper
//                        .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
//            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    public static List<JUpdateItemOperation> saveOrUpdate(InventoryAgentInstancesDBLayer dbLayer, DBItemInventoryAgentInstance dbAgent,
            Collection<DBItemInventorySubAgentInstance> dbSubAgents, Collection<SubAgent> subAgents) throws SOSHibernateException {
        
        subAgents.stream().collect(Collectors.groupingBy(SubAgent::getSubagentId, Collectors.counting())).entrySet().stream().filter(e -> e
                .getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sId -> {
                    throw new JocBadRequestException("Subagent ID '" + sId + "' must be unique per contoller");
                });
        
     // TODO URL has to be unique?
//      subAgents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(
//              e -> e.getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sUrl -> {
//                  throw new JocBadRequestException("URL '" + sUrl + "' must be unique");
//              });

        String agentId = dbAgent.getAgentId();
        
        Map<Boolean, List<DBItemInventorySubAgentInstance>> mapOfAgentIds = dbSubAgents.stream().collect(Collectors.groupingBy(s -> s.getAgentId()
                .equals(agentId)));
        dbSubAgents = null;
        
        Set<String> subAgentIds = subAgents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());

        // checks if subagentId from request is used in other agentIds
        mapOfAgentIds.getOrDefault(false, Collections.emptyList()).parallelStream().filter(s -> subAgentIds.contains(s.getSubAgentId()))
                .findAny().ifPresent(s -> {
                    throw new JocBadRequestException("subagentId has to be unique per controller: '" + s.getSubAgentId()
                            + "' is already used in Agent '" + s.getAgentId() + "'");
                });
        
        // checks java name rules of SubagentIds
        subAgentIds.forEach(id -> {
            CheckJavaVariableName.test("Subagent ID", id);
        });
        
        // checks that director and standby director can only exist once
        List<SubagentDirectorType> direcs = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.SECONDARY_DIRECTOR);
        Predicate<SubAgent> isDirector = s -> direcs.contains(s.getIsDirector());
        subAgents.stream().filter(isDirector).collect(Collectors.groupingBy(SubAgent::getIsDirector, Collectors.counting())).forEach((k,
                v) -> {
            String directorType = k.equals(SubagentDirectorType.PRIMARY_DIRECTOR) ? "primary" : "standby";
            if (v > 1L) {
                throw new JocBadRequestException("At most one SubAgent can be a " + directorType + " director");
            }
        });

        // TODO check URL uniqueness?
        
        DBItemInventorySubAgentInstance primaryDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.PRIMARY_DIRECTOR.intValue());
        DBItemInventorySubAgentInstance standbyDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.SECONDARY_DIRECTOR.intValue());
        
        boolean primaryDirectorIsChanged = false;
        boolean standbyDirectorIsChanged = false;
        Date now = Date.from(Instant.now());
//        List<JUpdateItemOperation> subAgentsToController = new ArrayList<>();
        List<JUpdateItemOperation> subAgentsToController = Collections.emptyList();
        
        Map<String, DBItemInventorySubAgentInstance> subAgentsOfCurAgent = mapOfAgentIds.getOrDefault(true, Collections.emptyList()).stream().
                collect(Collectors.toMap(DBItemInventorySubAgentInstance::getSubAgentId, Function.identity()));
        
        List<DBItemInventorySubAgentInstance> curDbSubAgents = mapOfAgentIds.getOrDefault(true, Collections.emptyList()).stream().sorted(Comparator
                .comparingInt(DBItemInventorySubAgentInstance::getOrdering)).collect(Collectors.toList());
        mapOfAgentIds = null;

        for (SubAgent subAgent : subAgents.stream().peek(s -> {
            if (s.getPosition() == null) {
                s.setPosition(Integer.MIN_VALUE);
            }
        }).sorted(Comparator.comparingInt(SubAgent::getPosition)).collect(Collectors.toList())) {
            DBItemInventorySubAgentInstance dbSubAgent = subAgentsOfCurAgent.get(subAgent.getSubagentId());
            if (dbSubAgent == null) {
                //save
                if (primaryDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR)) {
                    primaryDirectorIsChanged = true;
                }
                if (standbyDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.SECONDARY_DIRECTOR)) {
                    standbyDirectorIsChanged = true;
                }
                DBItemInventorySubAgentInstance newDbSubAgent = new DBItemInventorySubAgentInstance();
                newDbSubAgent.setId(null);
                newDbSubAgent.setAgentId(agentId);
                newDbSubAgent.setSubAgentId(subAgent.getSubagentId());
                newDbSubAgent.setIsDirector(subAgent.getIsDirector());
                newDbSubAgent.setOrdering(subAgent.getPosition());
                newDbSubAgent.setUri(subAgent.getUrl());
                newDbSubAgent.setIsWatcher(false);
                newDbSubAgent.setOsId(0L);
                newDbSubAgent.setTransaction("save");
                if (subAgent.getPosition() == Integer.MIN_VALUE || curDbSubAgents.size() < subAgent.getPosition()) {
                    curDbSubAgents.add(newDbSubAgent);
                } else {
                    curDbSubAgents.add(subAgent.getPosition() - 1, newDbSubAgent);
                }
                
//              subAgentsToController.add(JUpdateItemOperation.addOrChangeSimple(JSubagentRef.of(SubagentId.of(subAgent.getSubagentId()), AgentPath.of(
//              agentId), Uri.of(subAgent.getUrl()))));
                
            } else {
                // update (if subagent and dbSubangent unequal)
                if (dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector()) && dbSubAgent.getUri().equals(subAgent.getUrl()) && dbSubAgent
                        .getOrdering().equals(subAgent.getPosition())) {
                    continue;
                }
                if (primaryDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR) && !subAgent.getSubagentId()
                        .equals(primaryDirector.getSubAgentId())) {
                    if (primaryDirectorIsChanged) {
                        throw new JocBadRequestException("At most one SubAgent can be a director");
                    } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                        primaryDirectorIsChanged = true;
                    }
                }
                if (standbyDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.SECONDARY_DIRECTOR) && !subAgent.getSubagentId()
                        .equals(standbyDirector.getSubAgentId())) {
                    if (standbyDirectorIsChanged) {
                        throw new JocBadRequestException("At most one SubAgent can be a standby director");
                    } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                        standbyDirectorIsChanged = true;
                    }
                }
//                if (!dbSubAgent.getUri().equals(subAgent.getUrl())) {
//                    subAgentsToController.add(JUpdateItemOperation.addOrChangeSimple(JSubagentRef.of(SubagentId.of(subAgent.getSubagentId()),
//                            AgentPath.of(agentId), Uri.of(subAgent.getUrl()))));
//                }
                dbSubAgent.setIsDirector(subAgent.getIsDirector());
                dbSubAgent.setUri(subAgent.getUrl());
                dbSubAgent.setTransaction("update");
                if (subAgent.getPosition() != Integer.MIN_VALUE) {
                    dbSubAgent.setOrdering(subAgent.getPosition());
                    curDbSubAgents.remove(dbSubAgent);
                    if (curDbSubAgents.size() < subAgent.getPosition()) {
                        curDbSubAgents.add(dbSubAgent);
                    } else {
                        curDbSubAgents.add(subAgent.getPosition() - 1, dbSubAgent);
                    }
                } else {
                    int i = curDbSubAgents.indexOf(dbSubAgent);
                    curDbSubAgents.set(i, dbSubAgent);
                }
            }
        }
        
        if (primaryDirectorIsChanged || standbyDirectorIsChanged) {
            if (primaryDirectorIsChanged && primaryDirector != null) {
                int i = curDbSubAgents.indexOf(primaryDirector);
                primaryDirector = curDbSubAgents.get(i);
                primaryDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                primaryDirector.setTransaction("update");
                curDbSubAgents.set(i, primaryDirector);
            }
            if (standbyDirectorIsChanged && standbyDirector != null) {
                int i = curDbSubAgents.indexOf(standbyDirector);
                standbyDirector = curDbSubAgents.get(i);
                standbyDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                standbyDirector.setTransaction("update");
                curDbSubAgents.set(i, standbyDirector);
            }
//            List<SubagentId> directors = dbLayer.getDirectorSubAgentIds(agentId).stream().map(id -> SubagentId.of(id)).collect(Collectors.toList());
//            JUpdateItemOperation agent = JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(AgentPath.of(agentId), directors));
//            subAgentsToController.add(agent);
        }
        
        int index = 0;
        String directorUrl = null;
        for (DBItemInventorySubAgentInstance item : curDbSubAgents) {
            if (item.getIsDirector() == SubagentDirectorType.PRIMARY_DIRECTOR.intValue()) {
                directorUrl = item.getUri();
            }
            item.setModified(now);
            if ("save".equals(item.getTransaction())) {
                item.setOrdering(index);
                dbLayer.getSession().save(item);
            } else if ("update".equals(item.getTransaction()) || item.getOrdering() == null || item.getOrdering() != index) {
                item.setOrdering(index);
                dbLayer.getSession().update(item);
            }
            index++;
        }
        if (directorUrl != null && !dbAgent.getUri().equals(directorUrl)) {
            dbAgent.setUri(directorUrl);
            dbLayer.updateAgent(dbAgent);
        }
        
        return subAgentsToController;
    }
};