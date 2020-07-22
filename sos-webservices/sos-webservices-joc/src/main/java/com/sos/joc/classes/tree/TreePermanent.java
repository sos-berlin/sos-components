package com.sos.joc.classes.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.CalendarType;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;

public class TreePermanent {

    public static List<JobSchedulerObjectType> getAllowedTypes(TreeFilter treeBody, SOSPermissionJocCockpit sosPermission, boolean treeForJoe) {
        Set<JobSchedulerObjectType> types = new HashSet<JobSchedulerObjectType>();

        for (JobSchedulerObjectType type : treeBody.getTypes()) {
            switch (type) {
            case INVENTORY: // TODO Permission
                // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                types.add(JobSchedulerObjectType.WORKFLOW);
                types.add(JobSchedulerObjectType.WORKFLOWJOB);
                types.add(JobSchedulerObjectType.JOB);
                types.add(JobSchedulerObjectType.JOBCLASS);
                types.add(JobSchedulerObjectType.AGENTCLUSTER);
                types.add(JobSchedulerObjectType.LOCK);
                types.add(JobSchedulerObjectType.JUNCTION);
                types.add(JobSchedulerObjectType.ORDER);
                types.add(JobSchedulerObjectType.CALENDAR);
                types.add(JobSchedulerObjectType.FOLDER);
                // }
                break;
            case WORKFLOW:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getJobChain().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case WORKFLOWJOB:
            case JOB:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getJob().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case JOBCLASS:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    // if (sosPermission.getProcessClass().getView().isStatus()) {
                    types.add(type);
                    // }
                }
                break;
            case AGENTCLUSTER:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getProcessClass().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case LOCK:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getLock().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case JUNCTION:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    // if (sosPermission.getLock().getView().isStatus()) {
                    types.add(type);
                    // }
                }
                break;
            case ORDER:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getOrder().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case CALENDAR:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    // if (sosPermission.getLock().getView().isStatus()) {
                    types.add(type);
                    // }
                }
                break;
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (treeForJoe) {
                    // if (sosPermission.getJobschedulerMaster().getAdministration().getConfigurations().isView()) {
                    types.add(type);
                    // }
                } else {
                    if (sosPermission.getCalendar().getView().isStatus()) {
                        types.add(type);
                    }
                }
                break;
            case FOLDER:
                break;
            default:
                types.add(type);
                break;
            }
        }
        return new ArrayList<JobSchedulerObjectType>(types);
    }

    public static SortedSet<Tree> initFoldersByFoldersFromBody(TreeFilter treeBody, Long instanceId, String schedulerId, boolean treeForInventory)
            throws JocException {
        Set<Long> inventoryTypes = new HashSet<Long>();
        Set<Long> calendarTypes = new HashSet<Long>();
        if (treeBody.getTypes() != null && !treeBody.getTypes().isEmpty()) {
            for (JobSchedulerObjectType type : treeBody.getTypes()) {
                try {
                    inventoryTypes.add(ConfigurationType.valueOf(type.value()).value());
                } catch (Throwable e) {
                    try {
                        calendarTypes.add(CalendarType.valueOf(type.value()).value());
                    } catch (Throwable ex) {
                    }
                }
            }
        }
        if (calendarTypes.size() > 0 && inventoryTypes.size() == 0) {
            inventoryTypes.add(ConfigurationType.CALENDAR.value());
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("initFoldersByFoldersFromBody");
            Globals.beginTransaction(session);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
            SortedSet<Tree> folders = new TreeSet<Tree>(comparator);
            Set<Tree> results = null;
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                for (Folder folder : treeBody.getFolders()) {
                    String normalizedFolder = ("/" + folder.getFolder()).replaceAll("//+", "/");
                    results = dbLayer.getFoldersByFolderAndType(normalizedFolder, inventoryTypes, calendarTypes);
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
                results = dbLayer.getFoldersByFolderAndType("/", inventoryTypes, calendarTypes);
                if (results != null && !results.isEmpty()) {
                    folders.addAll(results);
                }
            }
            if (treeForInventory) {
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

    @SuppressWarnings("unchecked")
    public static <T extends Tree> T getTree(SortedSet<T> folders, SOSShiroFolderPermissions sosShiroFolderPermissions) {
        Map<Path, TreeModel> treeMap = new HashMap<Path, TreeModel>();
        Set<Folder> listOfFolders = sosShiroFolderPermissions.getListOfFolders();
        for (T folder : folders) {
            if (sosShiroFolderPermissions.isPermittedForFolder(folder.getPath(), listOfFolders)) {

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

        return (T) treeMap.get(Paths.get("/"));
    }

    private static <T extends Tree> TreeModel setFolderItemProps(T folder, TreeModel tree) {
        if (folder.getDeleted() != null && folder.getDeleted()) {
            tree.setDeleted(true);
        }
        if (folder.getLockedBy() != null && !folder.getLockedBy().isEmpty()) {
            tree.setLockedBy(folder.getLockedBy());
        }
        if (folder.getLockedSince() != null) {
            tree.setLockedSince(folder.getLockedSince());
        }
        // tree.setAgentClusters(null);
        // tree.setJobChains(null);
        // tree.setJobs(null);
        // tree.setLocks(null);
        // tree.setMonitors(null);
        // tree.setOrders(null);
        // tree.setProcessClasses(null);
        // tree.setSchedules(null);
        return tree;
    }

    // private static TreeModel setJoeFolderItemProps(JoeTree folder, TreeModel tree) {
    // if (folder.getDeleted() != null && folder.getDeleted()) {
    // tree.setDeleted(true);
    // }
    // if (folder.getLockedBy() != null && !folder.getLockedBy().isEmpty()) {
    // tree.setLockedBy(folder.getLockedBy());
    // }
    // if (folder.getLockedSince() != null) {
    // tree.setLockedSince(folder.getLockedSince());
    // }
    //// if (folder.getAgentClusters() != null && !folder.getAgentClusters().isEmpty()) {
    //// tree.setAgentClusters(folder.getAgentClusters());
    //// } else {
    //// tree.setAgentClusters(null);
    //// }
    //// if (folder.getJobChains() != null && !folder.getJobChains().isEmpty()) {
    //// tree.setJobChains(folder.getJobChains());
    //// } else {
    //// tree.setJobChains(null);
    //// }
    //// if (folder.getJobs() != null && !folder.getJobs().isEmpty()) {
    //// tree.setJobs(folder.getJobs());
    //// } else {
    //// tree.setJobs(null);
    //// }
    //// if (folder.getLocks() != null && !folder.getLocks().isEmpty()) {
    //// tree.setLocks(folder.getLocks());
    //// } else {
    //// tree.setLocks(null);
    //// }
    //// if (folder.getMonitors() != null && !folder.getMonitors().isEmpty()) {
    //// tree.setMonitors(folder.getMonitors());
    //// } else {
    //// tree.setMonitors(null);
    //// }
    //// if (folder.getOrders() != null && !folder.getOrders().isEmpty()) {
    //// tree.setOrders(folder.getOrders());
    //// } else {
    //// tree.setOrders(null);
    //// }
    //// if (folder.getProcessClasses() != null && !folder.getProcessClasses().isEmpty()) {
    //// tree.setProcessClasses(folder.getProcessClasses());
    //// } else {
    //// tree.setProcessClasses(null);
    //// }
    //// if (folder.getSchedules() != null && !folder.getSchedules().isEmpty()) {
    //// tree.setSchedules(folder.getSchedules());
    //// } else {
    //// tree.setSchedules(null);
    //// }
    //// tree.setNodeParams(null);
    // return tree;
    // }

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