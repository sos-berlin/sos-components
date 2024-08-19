package com.sos.joc.classes.dependencies;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DependencyResolver {


    /*
     * This class contains utility methods to determine, read and write related configuration objects
     * 
     * */
    
    private static Logger LOGGER = LoggerFactory.getLogger(DependencyResolver.class);
    
    public static ReferencedDbItem resolve(SOSHibernateSession session, ConfigurationObject inventoryObject)
            throws SOSHibernateException {
        return resolve(session, inventoryObject.getName(), inventoryObject.getObjectType());
    }

    public static ReferencedDbItem resolve(SOSHibernateSession session, String name, ConfigurationType type)
            throws SOSHibernateException {
        DBItemInventoryConfiguration dbItem = null;
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        if(!ConfigurationType.FOLDER.equals(type)) {
            List<DBItemInventoryConfiguration> results = dbLayer.getConfigurationByName(name, type.intValue());
            if(!results.isEmpty()) {
                dbItem = results.get(0); 
            }
        }
        return resolve(session, dbItem);
    }

    public static ReferencedDbItem resolve(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem) throws SOSHibernateException {
        ReferencedDbItem cfg = new ReferencedDbItem(inventoryDbItem);
        cfg.setReferencedItem(inventoryDbItem);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        switch(inventoryDbItem.getTypeAsEnum()) {
        case LOCK:
            List<DBItemInventoryConfiguration> workflowsWithLocks =  dbLayer.getUsedWorkflowsByLockId(inventoryDbItem.getName());
            if(workflowsWithLocks != null) {
                cfg.getReferencedBy().addAll(workflowsWithLocks);
            }
            break;
        case JOBRESOURCE:
            List<DBItemInventoryConfiguration> workflowsWithJobResource =  dbLayer.getUsedWorkflowsByJobResource(inventoryDbItem.getName());
            if(workflowsWithJobResource != null) {
                cfg.getReferencedBy().addAll(workflowsWithJobResource);
            }
            List<DBItemInventoryConfiguration> jobTemplatesByJobResource = dbLayer.getUsedJobTemplatesByJobResource(inventoryDbItem.getName());
            if(jobTemplatesByJobResource != null) {
                cfg.getReferencedBy().addAll(jobTemplatesByJobResource);
            }
            break;
        case NOTICEBOARD:
            List<DBItemInventoryConfiguration> workflowsWithNoticeBoards =  dbLayer.getUsedWorkflowsByBoardName(inventoryDbItem.getName());
            if(workflowsWithNoticeBoards != null) {
                cfg.getReferencedBy().addAll(workflowsWithNoticeBoards);
            }
            break;
        case WORKFLOW:
            List<DBItemInventoryConfiguration> fileOrderSources = dbLayer.getUsedFileOrderSourcesByWorkflowName(inventoryDbItem.getName());
            if (fileOrderSources != null) {
                cfg.getReferencedBy().addAll(fileOrderSources);
            }
            List<DBItemInventoryConfiguration> workflowSchedules = dbLayer.getUsedSchedulesByWorkflowName(inventoryDbItem.getName());
            if(workflowSchedules != null) {
                cfg.getReferencedBy().addAll(workflowSchedules);
            }
            List<DBItemInventoryConfiguration> workflowsWithInstruction = dbLayer.getUsedWorkflowsByAddOrdersWorkflowName(
                    inventoryDbItem.getName());
            if(workflowsWithInstruction != null) {
                cfg.getReferencedBy().addAll(workflowsWithInstruction);
            }
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            List<DBItemInventoryConfiguration> schedulesBasedOnCalendar = dbLayer.getUsedSchedulesByCalendarName(inventoryDbItem.getName());
            if(schedulesBasedOnCalendar != null) {
                cfg.getReferencedBy().addAll(schedulesBasedOnCalendar);
            }
            break;
        case JOBTEMPLATE:
            List<DBItemInventoryConfiguration> workflowsByJobTemplate = dbLayer.getUsedWorkflowsByJobTemplateName(inventoryDbItem.getName());
            if(workflowsByJobTemplate != null) {
                cfg.getReferencedBy().addAll(workflowsByJobTemplate);
            }
            break;
        default:
            break;
        }
        return cfg;
    }
    
    public static void updateDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException {
        boolean ownTransaction = false;
        if (!session.isAutoCommit() && !session.isTransactionOpened()) {
            session.beginTransaction();
            ownTransaction = true;
        }
        insertOrRenewDependencies(session, inventoryDbItem);
        if(ownTransaction) {
            session.commit();
        }
    }
    
//    public static void updateRelatedConfigurations(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
//            throws SOSHibernateException {
//        ReferencedDbItem references = resolve(session, inventoryDbItem);
//        InventoryDBLayer layer = new InventoryDBLayer(session);
//        // update inventory items
//        references.getReferencedBy().stream().peek(item -> {
//            item.setDeployed(false);
//            item.setReleased(false);
//            item.setModified(Date.from(Instant.now()));
//        }).forEach(item -> {
//            try {
//                JocInventory.insertOrUpdateConfiguration(layer, item);
//            } catch (SOSHibernateException|IOException e) {
//                LOGGER.error(String.format("Could not update item: %1$s - %2$s", item.getName(), item.getTypeAsEnum().name()), e);
//            }
//        });
//    }

    public static void insertOrRenewDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException {
        ReferencedDbItem references = resolve(session, inventoryDbItem);
        InventoryDBLayer layer = new InventoryDBLayer(session);
        // store new dependencies
        layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references));
    }

    public static ReferencedDbItem getReferencedDbItems(SOSHibernateSession session, DBItemInventoryConfiguration item,
            List<DBItemInventoryDependency> dependencies) throws SOSHibernateException {
        ReferencedDbItem referencedItem = new ReferencedDbItem(item);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        Map<Long, List<Long>> groupedDependencies = dependencies.stream()
                .filter(dependency -> dependency.getInvId().equals(item.getId()))
                .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
                        Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
        referencedItem.getReferencedBy().addAll(dbLayer.getConfigurations(groupedDependencies.get(item.getId())));
        return referencedItem;
    }

    public static List<DBItemInventoryDependency> getStoredDependencies(SOSHibernateSession session, ReferencedDbItem references)
            throws SOSHibernateException {
        List<DBItemInventoryDependency> dependencies = new ArrayList<DBItemInventoryDependency>();
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        dependencies = dbLayer.getDependencies(references.getReferencedItem());
        return dependencies;
    }

    public static List<DBItemInventoryDependency> convert(ReferencedDbItem reference) {
        return reference.getReferencedBy().stream().map(item -> {
            DBItemInventoryDependency dependency = new DBItemInventoryDependency();
            dependency.setInvId(reference.getReferencedItem().getId());
            dependency.setDependencyType(reference.getReferencedItem().getTypeAsEnum());
            dependency.setInvDependencyId(item.getId());
            dependency.setPublished(false);
            return dependency;
        }).collect(Collectors.toList());
    }
    
    public static ReferencedDbItem convert(SOSHibernateSession session, List<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ReferencedDbItem newDbItem = new ReferencedDbItem(dbLayer.getConfiguration(dependencies.get(0).getInvId()));
        for(DBItemInventoryDependency dependency : dependencies) {
            // TODO: conversion
            DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(dependency.getInvDependencyId());
            newDbItem.getReferencedBy().add(newItem);
        }
        return newDbItem;
    }
}
