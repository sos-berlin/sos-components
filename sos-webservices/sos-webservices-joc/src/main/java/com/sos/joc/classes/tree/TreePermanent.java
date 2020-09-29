package com.sos.joc.classes.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;
import com.sos.joc.model.tree.TreeType;

public class TreePermanent {

    public static List<TreeType> getAllowedTypes(TreeFilter treeBody, SOSPermissionJocCockpit sosPermission, boolean treeForInventory) {
        Set<TreeType> types = new HashSet<TreeType>();

        for (TreeType type : treeBody.getTypes()) {
            switch (type) {
            case INVENTORY:
                if (sosPermission.getInventory().getConfigurations().isView()) {
                    types.add(TreeType.FOLDER);
                    types.add(TreeType.WORKFLOW);
                    types.add(TreeType.JOB);
                    types.add(TreeType.JOBCLASS);
                    types.add(TreeType.AGENTCLUSTER);
                    types.add(TreeType.LOCK);
                    types.add(TreeType.JUNCTION);
                    types.add(TreeType.ORDER);
                    types.add(TreeType.WORKINGDAYSCALENDAR);
                    types.add(TreeType.NONWORKINGDAYSCALENDAR);
                }
                break;
            case WORKFLOW:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getWorkflow().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case JOB:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getJob().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case JOBCLASS:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    // if (sosPermission.getProcessClass().getView().isStatus()) {
                        types.add(type);
                    // }
                }
                break;
            case AGENTCLUSTER:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getJS7UniversalAgent().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case LOCK:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getLock().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case JUNCTION:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    // TODO if (sosPermission.getJunction().getView().isStatus()) {
                    types.add(type);
                    // }
                }
                break;
            case ORDER:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getOrder().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                } else {
                    if (sosPermission.getCalendar().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case FOLDER:
                if (treeForInventory) {
                    if (sosPermission.getInventory().getConfigurations().isView()) {
                        types.add(type);
                    }
                }
                break;
            case DOCUMENTATION:
                break;
            }
        }
        return new ArrayList<TreeType>(types);
    }

    public static SortedSet<Tree> initFoldersByFoldersForInventory(TreeFilter treeBody)
            throws JocException {
        Set<Integer> inventoryTypes = new HashSet<Integer>();
        if (treeBody.getTypes() != null && !treeBody.getTypes().isEmpty()) {
            inventoryTypes = treeBody.getTypes().stream().map(TreeType::intValue).collect(Collectors.toSet());
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initFoldersByFoldersForInventory");
            Globals.beginTransaction(session);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndType(normalizedFolder, inventoryTypes);
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
                results = dbLayer.getFoldersByFolderAndType("/", inventoryTypes);
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
    
    public static SortedSet<Tree> initFoldersByFoldersForViews(TreeFilter treeBody, String controllerId)
            throws JocException {
        Set<Integer> bodyTypes = new HashSet<Integer>();
        Set<Integer> deployIntTypes = Arrays.asList(DeployType.values()).stream().map(DeployType::intValue).collect(Collectors.toSet());

        if (treeBody.getTypes() != null && !treeBody.getTypes().isEmpty()) {
            for (TreeType type : treeBody.getTypes()) {
                if (deployIntTypes.contains(type.intValue())) {
                    bodyTypes.add(type.intValue());
                }
            }
        } else {
            bodyTypes = deployIntTypes;
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initFoldersByFoldersForViews");
            Globals.beginTransaction(session);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndType(controllerId, normalizedFolder, bodyTypes);
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
                results = dbLayer.getFoldersByFolderAndType(controllerId, "/", bodyTypes);
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

    public static Tree getTree(SortedSet<Tree> folders, SOSShiroFolderPermissions sosShiroFolderPermissions) {
        Map<Path, TreeModel> treeMap = new HashMap<Path, TreeModel>();
        Set<Folder> listOfFolders = sosShiroFolderPermissions.getListOfFolders();
        for (Tree folder : folders) {
            if (SOSShiroFolderPermissions.isPermittedForFolder(folder.getPath(), listOfFolders)) {

                Path pFolder = Paths.get(folder.getPath());
                TreeModel tree = new TreeModel();
                if (treeMap.containsKey(pFolder)) {
                    tree = treeMap.get(pFolder);
                    tree = setFolderItemProps(folder, tree);
                } else {
                    tree.setPath(folder.getPath());
                    Path fileName = pFolder.getFileName();
                    tree.setName(fileName == null ? "" : fileName.toString());
                    tree.setFolders(null);
                    tree = setFolderItemProps(folder, tree);
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

    private static TreeModel setFolderItemProps(Tree folder, TreeModel tree) {
        if (folder.getDeleted() != null && folder.getDeleted()) {
            tree.setDeleted(true);
        }
        if (folder.getLockedBy() != null && !folder.getLockedBy().isEmpty()) {
            tree.setLockedBy(folder.getLockedBy());
        }
        if (folder.getLockedSince() != null) {
            tree.setLockedSince(folder.getLockedSince());
        }
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