package com.sos.joc.classes.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTrashDBLayer;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;
import com.sos.joc.model.tree.TreeType;

public class TreePermanent {

    public static List<TreeType> getAllowedTypes(List<TreeType> bodyTypes, JocPermissions jocPermissions, ControllerPermissions controllerPermission,
            boolean treeForInventory, boolean treeForInventoryTrash, boolean treeForDescriptors, boolean treeForDescriptorsTrash) {

        if (bodyTypes == null || bodyTypes.isEmpty()) {
            if (treeForInventory || treeForInventoryTrash) {
                bodyTypes = Arrays.asList(TreeType.INVENTORY, TreeType.DOCUMENTATION);
            } else if (treeForDescriptors || treeForDescriptorsTrash) {
                bodyTypes = Arrays.asList(TreeType.DEPLOYMENTDESCRIPTOR, TreeType.DESCRIPTORFOLDER);
            } else {
                bodyTypes = Arrays.asList(TreeType.values());
            }
        }
        Set<TreeType> types = new HashSet<TreeType>();
        boolean inventoryPermission = jocPermissions.getInventory().getView();

        for (TreeType type : bodyTypes) {
            switch (type) {
            case INVENTORY:
                if ((treeForInventory || treeForInventoryTrash) && inventoryPermission) {
                    types.add(TreeType.FOLDER);
                    types.add(TreeType.WORKFLOW);
                    types.add(TreeType.JOBTEMPLATE);
                    types.add(TreeType.JOBCLASS);
                    types.add(TreeType.LOCK);
                    types.add(TreeType.FILEORDERSOURCE);
                    types.add(TreeType.NOTICEBOARD);
                    types.add(TreeType.SCHEDULE);
                    types.add(TreeType.INCLUDESCRIPT);
                    types.add(TreeType.WORKINGDAYSCALENDAR);
                    types.add(TreeType.NONWORKINGDAYSCALENDAR);
                }
                break;
            case WORKFLOW:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } else {
                    if (controllerPermission.getWorkflows().getView()) {
                        types.add(type);
                    }
                }
                break;
            case JOBTEMPLATE:
                if (inventoryPermission) {
                    types.add(type);
                }
                break;
            case JOBCLASS:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                }
                break;
            case JOBRESOURCE:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                }
                break;
            case LOCK:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } else {
                    if (controllerPermission.getLocks().getView()) {
                        types.add(type);
                    }
                }
                break;
            case FILEORDERSOURCE:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                }
                break;
            case NOTICEBOARD:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } else {
                    if (controllerPermission.getNoticeBoards().getView()) {
                        types.add(type);
                    }
                }
                break;
            case SCHEDULE:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } else {
                    if (jocPermissions.getDailyPlan().getView()) {
                        types.add(type);
                    }
                }
                break;
            case INCLUDESCRIPT:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                }
                break;
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } else {
                    if (jocPermissions.getCalendars().getView()) {
                        types.add(type);
                    }
                }
                break;
            case FOLDER:
                if (treeForInventory || treeForInventoryTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                } 
                break;
            case DOCUMENTATION:
                if (jocPermissions.getDocumentations().getView()) {
                    types.add(type);
                }
                break;
            case DEPLOYMENTDESCRIPTOR:
            case DESCRIPTORFOLDER:
                if (treeForDescriptors || treeForDescriptorsTrash) {
                    if (inventoryPermission) {
                        types.add(type);
                    }
                }
                break;
            }
        }
        return new ArrayList<TreeType>(types);
    }
    
//    public static Tree initFoldersByTagsForInventory(TagTreeFilter treeBody, SOSAuthFolderPermissions sosAuthFolderPermissions) throws JocException {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("initTreeForInventory");
//            Globals.beginTransaction(session);
//            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
//
//            List<InventoryTagItem> tagItems = dbLayer.getFoldersByTagAndTypeForInventory(getInventoryTypes(treeBody.getTypes()), treeBody
//                    .getOnlyValidObjects());
//            // TODO consider treeBody.getTags()
//            return dbLayer.getAllTagsTree(getNotPermittedTagIds(tagItems, sosAuthFolderPermissions));
//        } catch (JocException e) {
//            throw e;
//        } finally {
//            Globals.rollback(session);
//            Globals.disconnect(session);
//        }
//    }
    
//    public static Tree initFoldersByTagsForDescriptors(TagTreeFilter treeBody, SOSAuthFolderPermissions sosAuthFolderPermissions)
//            throws JocException {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("initTreeForDescriptors");
//            Globals.beginTransaction(session);
//            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
//
//            List<InventoryTagItem> tagItems = dbLayer.getFoldersByTagAndTypeForInventory(Collections.singleton(TreeType.DEPLOYMENTDESCRIPTOR
//                    .intValue()), treeBody.getOnlyValidObjects());
//            // TODO consider treeBody.getTags()
//            return dbLayer.getAllTagsTree(getNotPermittedTagIds(tagItems, sosAuthFolderPermissions));
//        } catch (JocException e) {
//            throw e;
//        } finally {
//            Globals.rollback(session);
//            Globals.disconnect(session);
//        }
//    }
    
//    public static Tree initFoldersByTagsForInventoryTrash(TagTreeFilter treeBody, SOSAuthFolderPermissions sosAuthFolderPermissions) throws JocException {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("initTreeForInventoryTrash");
//            Globals.beginTransaction(session);
//            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
//
//            List<InventoryTagItem> tagItems = dbLayer.getFoldersByTagAndTypeForInventoryTrash(getInventoryTypes(treeBody.getTypes()), treeBody
//                    .getOnlyValidObjects());
//            // TODO consider treeBody.getTags()
//            return dbLayer.getAllTagsTree(getNotPermittedTagIds(tagItems, sosAuthFolderPermissions));
//        } catch (JocException e) {
//            throw e;
//        } finally {
//            Globals.rollback(session);
//            Globals.disconnect(session);
//        }
//    }
    
//    public static Tree initFoldersByTagsForDescriptorTrash(TagTreeFilter treeBody, SOSAuthFolderPermissions sosAuthFolderPermissions)
//            throws JocException {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("initTreeForDescriptorTrash");
//            Globals.beginTransaction(session);
//            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
//
//            List<InventoryTagItem> tagItems = dbLayer.getFoldersByTagAndTypeForInventoryTrash(Collections.singleton(TreeType.DEPLOYMENTDESCRIPTOR
//                    .intValue()), treeBody.getOnlyValidObjects());
//            // TODO consider treeBody.getTags()
//            return dbLayer.getAllTagsTree(getNotPermittedTagIds(tagItems, sosAuthFolderPermissions));
//        } catch (JocException e) {
//            throw e;
//        } finally {
//            Globals.rollback(session);
//            Globals.disconnect(session);
//        }
//    }
    
//    public static Tree initFoldersByTagsForViews(TagTreeFilter treeBody, SOSAuthFolderPermissions sosAuthFolderPermissions) throws JocException {
//        
//        List<TreeType> possibleInventoryTypes = Arrays.asList(TreeType.JOBTEMPLATE, TreeType.SCHEDULE, TreeType.INCLUDESCRIPT,
//                TreeType.WORKINGDAYSCALENDAR, TreeType.NONWORKINGDAYSCALENDAR, TreeType.DEPLOYMENTDESCRIPTOR);
//        Set<Integer> possibleDeployIntTypes = Arrays.asList(DeployType.values()).stream().map(DeployType::intValue).collect(Collectors.toSet());
//        Set<Integer> deployTypes = new HashSet<>();
//        Set<Integer> inventoryTypes = new HashSet<>();
//
//        for (TreeType type : treeBody.getTypes()) {
//            if (possibleDeployIntTypes.contains(type.intValue())) {
//                deployTypes.add(type.intValue());
//            } else if (TreeType.DOCUMENTATION.equals(type)) {
//                if (treeBody.getTypes().size() == 1) { //if docu is the only choosen type
//                    throw new JocNotImplementedException("Documentations are not tagged");
//                }
//            } else if (possibleInventoryTypes.contains(type)) {
//                inventoryTypes.add(type.intValue());
//            }
//        }
//        if (!deployTypes.isEmpty() && (treeBody.getControllerId() == null || treeBody.getControllerId().isEmpty())) {
//            throw new JocMissingRequiredParameterException("undefined 'controllerId'");
//        }
//        
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("initTreeForViews");
//            Globals.beginTransaction(session);
//            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
//            List<InventoryTagItem> tagItems = new ArrayList<>();
//            
//            if (!deployTypes.isEmpty()) {
//                List<InventoryTagItem> tagItems1 = dbLayer.getFoldersByTagAndTypeForDeployedObjects(deployTypes, treeBody.getControllerId());
//                if (tagItems1 != null && !tagItems1.isEmpty()) {
//                    tagItems.addAll(tagItems1);
//                }
//            }
//            if (!inventoryTypes.isEmpty()) {
//                List<InventoryTagItem> tagItems2 = dbLayer.getFoldersByTagAndTypeForReleasedObjects(inventoryTypes);
//                if (tagItems2 != null && !tagItems2.isEmpty()) {
//                    tagItems.addAll(tagItems2);
//                }
//            }
//            
//            // TODO consider treeBody.getTags()
//            return dbLayer.getAllTagsTree(getNotPermittedTagIds(tagItems, sosAuthFolderPermissions));
//        } catch (JocException e) {
//            throw e;
//        } finally {
//            Globals.rollback(session);
//            Globals.disconnect(session);
//        }
//    }
    
//    private static Set<Long> getNotPermittedTagIds(Collection<InventoryTagItem> tagItems, SOSAuthFolderPermissions sosAuthFolderPermissions) {
//        final Set<Folder> listOfFolders = sosAuthFolderPermissions.getListOfFolders();
//        Predicate<InventoryTagItem> isPermittedForFolder = tagItem -> SOSAuthFolderPermissions.isPermittedForFolder(tagItem.getFolder(), listOfFolders);
//        
//        Set<Long> permittedInvTagIds = tagItems.stream().filter(isPermittedForFolder).map(InventoryTagItem::getTagId).collect(Collectors.toSet());
//        Set<Long> notPermittedInvTagIds = tagItems.stream().map(InventoryTagItem::getTagId).collect(Collectors.toSet());
//        notPermittedInvTagIds.removeAll(permittedInvTagIds);
//        return notPermittedInvTagIds;
//    }

    public static SortedSet<Tree> initFoldersByFoldersForInventory(TreeFilter treeBody) throws JocException {
        Set<Integer> inventoryTypes = getInventoryTypes(treeBody.getTypes());
        
        List<ConfigurationType> folderTypes = Collections.singletonList(
                ConfigurationType.FOLDER);
        if (treeBody.getTypes().contains(TreeType.DESCRIPTORFOLDER)) {
            folderTypes = Arrays.asList(ConfigurationType.FOLDER, ConfigurationType.DESCRIPTORFOLDER);
        }
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initTreeForInventory");
            Globals.beginTransaction(session);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            //DocumentationDBLayer dbDocLayer = new DocumentationDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            //Set<Tree> docResults = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndTypeForInventory(normalizedFolder, inventoryTypes, treeBody.getOnlyValidObjects(),
                            folderTypes);
                    // if (withDocus) {
                    // docResults = dbDocLayer.getFoldersByFolder(normalizedFolder);
                    // if (docResults != null && !docResults.isEmpty()) {
                    // results.addAll(docResults);
                    // }
                    // }
                    if (results != null && !results.isEmpty()) {
                        if (folder.getRecursive() == null || folder.getRecursive()) {
                            folders.addAll(results);
                        } else {
                            final int parentDepth = Paths.get(normalizedFolder).getNameCount();
                            folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() == parentDepth + 1).collect(
                                    Collectors.toSet()));
                        }
                    }
                }
            } else {
                results = dbLayer.getFoldersByFolderAndTypeForInventory("/", inventoryTypes, treeBody.getOnlyValidObjects(), folderTypes);
                // if (withDocus) {
                // docResults = dbDocLayer.getFoldersByFolder("/");
                // if (docResults != null && !docResults.isEmpty()) {
                // results.addAll(docResults);
                // }
                // }
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            List<DBItemJocLock> jocLocks = dbLayer.getJocLocks();
            if (jocLocks != null && jocLocks.size() > 0) {
                Supplier<TreeSet<Tree>> supplier = () -> new TreeSet<Tree>(comparator);
                folders = folders.stream().map(folder -> {
                    Optional<DBItemJocLock> jocLock = jocLocks.stream().filter(l -> l.getFolder().equals(folder.getPath())).findFirst();
                    if (jocLock.isPresent()) {
                        folder.setLockedBy(jocLock.get().getAccount());
                        folder.setLockedSince(jocLock.get().getCreated());
                    }
                    return folder;
                }).collect(Collectors.toCollection(supplier));
            }
            Globals.commit(session);
            return folders;
        } catch (JocException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static SortedSet<Tree> initFoldersByFoldersForInventoryTrash(TreeFilter treeBody) throws JocException {
        Set<Integer> inventoryTypes = getInventoryTypes(treeBody.getTypes());

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initTreeForInventoryTrash");
            Globals.beginTransaction(session);
            InventoryTrashDBLayer dbLayer = new InventoryTrashDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndType(normalizedFolder, inventoryTypes, treeBody.getOnlyValidObjects(), false);
                    if (results != null && !results.isEmpty()) {
                        if (folder.getRecursive() == null || folder.getRecursive()) {
                            folders.addAll(results);
                        } else {
                            final int parentDepth = Paths.get(normalizedFolder).getNameCount();
                            folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() == parentDepth + 1).collect(
                                    Collectors.toSet()));
                        }
                    }
                }
            } else {
                results = dbLayer.getFoldersByFolderAndType("/", inventoryTypes, treeBody.getOnlyValidObjects(), false);
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            Globals.commit(session);
            return folders;
        } catch (JocException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static Set<Integer> getInventoryTypes(List<TreeType> types) {
        Set<Integer> inventoryTypes = types.stream().map(TreeType::intValue).collect(Collectors.toSet());
        // DOCUMENTATION is not part of INV_CONFIGURATION_TRASH
        inventoryTypes.removeIf(i -> i == TreeType.DOCUMENTATION.intValue());
        return inventoryTypes;
    }

    public static SortedSet<Tree> initFoldersByFoldersForDescriptors(TreeFilter treeBody) {
        Set<Integer> descriptorTypes = treeBody.getTypes().stream().map(TreeType::intValue).collect(Collectors.toSet());

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initTreeForDescriptors");
            Globals.beginTransaction(session);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndTypeForInventory(normalizedFolder, descriptorTypes, treeBody.getOnlyValidObjects(),
                            Collections.singleton(ConfigurationType.DESCRIPTORFOLDER));
                    if (results != null && !results.isEmpty()) {
                        if (folder.getRecursive() == null || folder.getRecursive()) {
                            folders.addAll(results);
                        } else {
                            final int parentDepth = Paths.get(normalizedFolder).getNameCount();
                            folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() == parentDepth + 1).collect(
                                    Collectors.toSet()));
                        }
                    }
                }
            } else {
                results = dbLayer.getFoldersByFolderAndTypeForInventory("/", descriptorTypes, treeBody.getOnlyValidObjects(), Collections.singleton(
                        ConfigurationType.DESCRIPTORFOLDER));
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            List<DBItemJocLock> jocLocks = dbLayer.getJocLocks();
            if (jocLocks != null && jocLocks.size() > 0) {
                Supplier<TreeSet<Tree>> supplier = () -> new TreeSet<Tree>(comparator);
                folders = folders.stream().map(folder -> {
                    Optional<DBItemJocLock> jocLock = jocLocks.stream().filter(l -> l.getFolder().equals(folder.getPath())).findFirst();
                    if (jocLock.isPresent()) {
                        folder.setLockedBy(jocLock.get().getAccount());
                        folder.setLockedSince(jocLock.get().getCreated());
                    }
                    return folder;
                }).collect(Collectors.toCollection(supplier));
            }
            Globals.commit(session);
            return folders;
        } catch (JocException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static SortedSet<Tree> initFoldersByFoldersForDescriptorsTrash(TreeFilter treeBody) throws JocException {
        Set<Integer> inventoryTypes = treeBody.getTypes().stream().map(TreeType::intValue).collect(Collectors.toSet());

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initTreeForDescriptorsTrash");
            Globals.beginTransaction(session);
            InventoryTrashDBLayer dbLayer = new InventoryTrashDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndType(normalizedFolder, inventoryTypes, treeBody.getOnlyValidObjects(), true);
                    if (results != null && !results.isEmpty()) {
                        if (folder.getRecursive() == null || folder.getRecursive()) {
                            folders.addAll(results);
                        } else {
                            final int parentDepth = Paths.get(normalizedFolder).getNameCount();
                            folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() == parentDepth + 1).collect(
                                    Collectors.toSet()));
                        }
                    }
                }
            } else {
                results = dbLayer.getFoldersByFolderAndType("/", inventoryTypes, treeBody.getOnlyValidObjects(), true);
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            Globals.commit(session);
            return folders;
        } catch (JocException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static SortedSet<Tree> initFoldersByFoldersForViews(TreeFilter treeBody) throws JocException {

        boolean withDocus = false;
        boolean onlyWithAssignReference = treeBody.getOnlyWithAssignReference() == Boolean.TRUE;
        List<TreeType> possibleInventoryTypes = Arrays.asList(TreeType.JOBTEMPLATE, TreeType.SCHEDULE, TreeType.INCLUDESCRIPT,
                TreeType.WORKINGDAYSCALENDAR, TreeType.NONWORKINGDAYSCALENDAR, TreeType.DEPLOYMENTDESCRIPTOR);
        Set<Integer> possibleDeployIntTypes = Arrays.asList(DeployType.values()).stream().map(DeployType::intValue).collect(Collectors.toSet());
        Set<Integer> deployTypes = new HashSet<>();
        Set<Integer> inventoryTypes = new HashSet<>();

        for (TreeType type : treeBody.getTypes()) {
            if (possibleDeployIntTypes.contains(type.intValue())) {
                deployTypes.add(type.intValue());
            } else if (TreeType.DOCUMENTATION.equals(type)) {
                withDocus = true;
            } else if (possibleInventoryTypes.contains(type)) {
                inventoryTypes.add(type.intValue());
            }
        }
        if (!deployTypes.isEmpty() && (treeBody.getControllerId() == null || treeBody.getControllerId().isEmpty())) {
            throw new JocMissingRequiredParameterException("undefined 'controllerId'");
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initTreeForViews");
            Globals.beginTransaction(session);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DocumentationDBLayer dbDocLayer = new DocumentationDBLayer(session);
            InventoryDBLayer dbInvLayer = new InventoryDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = new HashSet<>();
            Set<Tree> deployedResults = new HashSet<>();
            Set<Tree> docResults = new HashSet<>();
            Set<Tree> inventoryResults = new HashSet<>();
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    if (!deployTypes.isEmpty()) {
                        deployedResults = dbLayer.getFoldersByFolderAndType(treeBody.getControllerId(), normalizedFolder, deployTypes);
                        if (deployedResults != null && !deployedResults.isEmpty()) {
                            results.addAll(deployedResults);
                        }
                    }
                    if (withDocus) {
                        docResults = dbDocLayer.getFoldersByFolder(normalizedFolder, onlyWithAssignReference);
                        if (docResults != null && !docResults.isEmpty()) {
                            results.addAll(docResults);
                        }
                    }
                    if (!inventoryTypes.isEmpty()) {
                        inventoryResults = dbInvLayer.getFoldersByFolderAndTypeForViews(normalizedFolder, inventoryTypes);
                        if (inventoryResults != null && !inventoryResults.isEmpty()) {
                            results.addAll(inventoryResults);
                        }
                    }
                    
                    if (results != null && !results.isEmpty()) {
                        if (folder.getRecursive() == null || folder.getRecursive()) {
                            folders.addAll(results);
                        } else {
                            final int parentDepth = Paths.get(normalizedFolder).getNameCount();
                            folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() == parentDepth + 1).collect(
                                    Collectors.toSet()));
                        }
                    }
                }
            } else {
                if (!deployTypes.isEmpty()) {
                    deployedResults = dbLayer.getFoldersByFolderAndType(treeBody.getControllerId(), "/", deployTypes);
                    if (deployedResults != null && !deployedResults.isEmpty()) {
                        results.addAll(deployedResults);
                    }
                }
                if (withDocus) {
                    docResults = dbDocLayer.getFoldersByFolder("/", onlyWithAssignReference);
                    if (docResults != null && !docResults.isEmpty()) {
                        results.addAll(docResults);
                    }
                }
                if (!inventoryTypes.isEmpty()) {
                    inventoryResults = dbInvLayer.getFoldersByFolderAndTypeForViews("/", inventoryTypes);
                    if (inventoryResults != null && !inventoryResults.isEmpty()) {
                        results.addAll(inventoryResults);
                    }
                }
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            Globals.commit(session);
            return folders;
        } catch (JocException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public static Tree getTree(SortedSet<Tree> folders, String controllerId, SOSAuthFolderPermissions sosAuthFolderPermissions) {
        Map<Path, TreeModel> treeMap = new HashMap<Path, TreeModel>();
        Set<Folder> listOfFolders = sosAuthFolderPermissions.getListOfFolders();
        Set<String> notPermittedParentFolders = sosAuthFolderPermissions.getNotPermittedParentFolders().getOrDefault(controllerId, Collections
                .emptySet());
        for (Tree folder : folders) {
            boolean isPermittedForFolder = SOSAuthFolderPermissions.isPermittedForFolder(folder.getPath(), listOfFolders);
            boolean isNotPermittedParentFolder = notPermittedParentFolders.contains(folder.getPath());
            
            if (isPermittedForFolder || isNotPermittedParentFolder) {

                Path pFolder = Paths.get(folder.getPath());
                TreeModel tree = new TreeModel();
                if (treeMap.containsKey(pFolder)) {
                    tree = treeMap.get(pFolder);
                    tree = setFolderItemProps(folder, isPermittedForFolder, tree);
                } else {
                    tree.setPath(folder.getPath());
                    Path fileName = pFolder.getFileName();
                    tree.setName(fileName == null ? "" : fileName.toString());
                    tree.setFolders(null);
                    tree = setFolderItemProps(folder, isPermittedForFolder, tree);
                    treeMap.put(pFolder, tree);
                }
                fillTreeMap(treeMap, pFolder, tree);
            }
        }
        if (treeMap.isEmpty()) {
            return null;
        }

        return treeMap.get(Paths.get("/"));
    }

    private static TreeModel setFolderItemProps(Tree folder, boolean isPermitted, TreeModel tree) {
        if (folder.getDeleted() != null && folder.getDeleted()) {
            tree.setDeleted(true);
        }
        if (folder.getLockedBy() != null && !folder.getLockedBy().isEmpty()) {
            tree.setLockedBy(folder.getLockedBy());
        }
        if (folder.getLockedSince() != null) {
            tree.setLockedSince(folder.getLockedSince());
        }
        tree.setRepoControlled(folder.getRepoControlled());
        tree.setPermitted(isPermitted);
        return tree;
    }

    private static void fillTreeMap(Map<Path, TreeModel> treeMap, Path folder, TreeModel tree) {
        Path parent = folder.getParent();
        if (parent != null) {
            TreeModel parentTree = new TreeModel();
            if (treeMap.containsKey(parent)) {
                parentTree = treeMap.get(parent);
                List<Tree> treeList = parentTree.getFolders();
                if (treeList == null) {
                    treeList = new ArrayList<Tree>();
                    treeList.add(tree);
                    parentTree.setFolders(treeList);
                } else {
                    if (treeList.contains(tree)) {
                        treeList.remove(tree);
                    }
                    treeList.add(0, tree);
                }
            } else {
                parentTree.setPath(parent.toString().replace('\\', '/'));
                Path fileName = parent.getFileName();
                parentTree.setName(fileName == null ? "" : fileName.toString());
                List<Tree> treeList = new ArrayList<Tree>();
                treeList.add(tree);
                parentTree.setFolders(treeList);
                treeMap.put(parent, parentTree);
            }
            fillTreeMap(treeMap, parent, parentTree);
        }
    }
}