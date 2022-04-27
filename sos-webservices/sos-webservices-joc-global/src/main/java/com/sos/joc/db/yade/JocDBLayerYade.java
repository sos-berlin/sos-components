package com.sos.joc.db.yade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.yade.FilesFilter;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferState;

public class JocDBLayerYade {

    private static final String YADE_SOURCE_TARGET_FILES = YadeSourceTargetFiles.class.getName();
    private static final String YADE_GROUPED_SUMMARY = YadeGroupedSummary.class.getName();

    private SOSHibernateSession session;

    public JocDBLayerYade(SOSHibernateSession session) {
        this.session = session;
    }

    // TODO: at the moment only state = 5 (TRANSFERRED) is checked
    public Integer getSuccessfulTransferredFilesCount(Date from, Date to) throws SOSHibernateException {
        return getTransferredFilesCount(from, to, TransferEntryState.TRANSFERRED);
    }

    // TODO: at the moment only state = 7 (TRANSFER_HAS_ERRORS) is checked
    public Integer getFailedTransferredFilesCount(Date from, Date to) throws SOSHibernateException {
        return getTransferredFilesCount(from, to, TransferEntryState.FAILED);
    }

    private Integer getTransferredFilesCount(Date from, Date to, TransferEntryState state) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(*) from ");
        hql.append(DBLayer.DBITEM_YADE_FILES).append(" yf, ");
        hql.append(DBLayer.DBITEM_YADE_TRANSFERS).append(" yt ");
        hql.append("where yf.transferId=yt.id ");
        if (from != null) {
            hql.append("and yt.start >= :from ");  // or end?
        }
        if (to != null) {
            hql.append("and yt.start < :to ");  // or end?
        }
        hql.append("and yf.state=:state").append(state.intValue());

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("state", state.intValue());
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        return session.getSingleResult(query).intValue();
    }

    // TODO: at the moment only state = 7 (TRANSFER_HAS_ERRORS) is checked
    public List<DBItemYadeFile> getFailedTransferredFiles(Long transferId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES).append(" ");
        hql.append("where state = :state");
        hql.append("and transferId=:transferId ");

        Query<DBItemYadeFile> query = session.createQuery(hql.toString());
        query.setParameter("state", TransferEntryState.FAILED.intValue());
        query.setParameter("transferId", transferId);
        return session.getResultList(query);
    }

    public List<DBItemYadeFile> getFilesById(List<Long> ids) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES).append(" ");
        if (ids != null && !ids.isEmpty()) {
            if (ids.size() == 1) {
                hql.append("where id=:id ");
            } else {
                hql.append("where id in (:ids) ");
            }
        }

        Query<DBItemYadeFile> query = session.createQuery(hql.toString());
        if (ids != null && !ids.isEmpty()) {
            if (ids.size() == 1) {
                query.setParameter("id", ids.get(0));
            } else {
                query.setParameterList("ids", ids);
            }
        }
        return session.getResultList(query);
    }

    public List<String> getSourceFilesByIdsAndTransferId(Long transferId, List<Long> ids) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select sourcePath from ").append(DBLayer.DBITEM_YADE_FILES).append(" ");
        hql.append("where transferId=:transferId ");
        if (ids != null && !ids.isEmpty()) {
            if (ids.size() == 1) {
                hql.append("and id=:id ");
            } else {
                hql.append("and id in (:ids) ");
            }
        }

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("transferId", transferId);
        if (ids != null && !ids.isEmpty()) {
            if (ids.size() == 1) {
                query.setParameter("id", ids.get(0));
            } else {
                query.setParameterList("ids", ids);
            }
        }
        return session.getResultList(query);
    }

    public List<DBItemYadeTransfer> getAllTransfers() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_TRANSFERS);

        Query<DBItemYadeTransfer> query = session.createQuery(hql.toString());
        return session.getResultList(query);
    }

    public DBItemYadeFile getTransferFile(Long id) throws SOSHibernateException {
        return session.get(DBItemYadeFile.class, id);
    }

    public List<DBItemYadeTransfer> getFilteredTransfers(JocYadeFilter filter) throws SOSHibernateException {
        return getTransfers(filter, false);
    }

    public List<Long> getFilteredTransferIds(JocYadeFilter filter) throws SOSHibernateException {
        return getTransfers(filter, true);
    }

    private <T> List<T> getTransfers(JocYadeFilter filter, boolean onlyTransferIds) throws SOSHibernateException {
        boolean withControllerIds = filter.getControllerIds() != null && !filter.getControllerIds().isEmpty();
        boolean withTransferIds = filter.getTransferIds() != null && !filter.getTransferIds().isEmpty();
        boolean withOperations = filter.getOperations() != null && !filter.getOperations().isEmpty();
        boolean withStates = filter.getStates() != null && !filter.getStates().isEmpty();
        boolean withSourceHosts = filter.getSourceHosts() != null && !filter.getSourceHosts().isEmpty();
        boolean withSourceProtocols = filter.getSourceProtocols() != null && !filter.getSourceProtocols().isEmpty();
        boolean withSourceProtocolHosts = filter.getSourceProtocolHosts() != null && !filter.getSourceProtocolHosts().isEmpty();
        boolean withTargetHosts = filter.getTargetHosts() != null && !filter.getTargetHosts().isEmpty();
        boolean withTargetProtocols = filter.getTargetProtocols() != null && !filter.getTargetProtocols().isEmpty();
        boolean withTargetProtocolHosts = filter.getTargetProtocolHosts() != null && !filter.getTargetProtocolHosts().isEmpty();
        boolean withProfiles = filter.getProfiles() != null && !filter.getProfiles().isEmpty();
        String and = " where";

        StringBuilder hql = new StringBuilder();
        hql.append("select yt");
        if (onlyTransferIds) {
            hql.append(".id");
        }
        hql.append(" from ").append(DBLayer.DBITEM_YADE_TRANSFERS).append(" yt");
        if (withSourceHosts || withSourceProtocols || withSourceProtocolHosts) {
            hql.append(", ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" yps");
        }
        if (withTargetHosts || withTargetProtocols || withTargetProtocolHosts) {
            hql.append(", ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" ypt");
        }
        if (withSourceHosts || withSourceProtocols || withSourceProtocolHosts) {
            hql.append(and).append(" yt.sourceProtocolId = yps.id");
            and = " and";
        }
        if (withTargetHosts || withTargetProtocols || withTargetProtocolHosts) {
            hql.append(and).append(" yt.targetProtocolId is not null and yt.targetProtocolId = ypt.id");
            and = " and";
        }
        if (withControllerIds) {
            hql.append(and).append(" yt.controllerId in (:controllerIds)");
            and = " and";
        }
        if (withTransferIds) {
            hql.append(and).append(" yt.id in (:transferIds)");
            and = " and";
        }
        if (withOperations) {
            hql.append(and).append(" yt.operation in (:operations)");
            and = " and";
        }
        if (withStates) {
            hql.append(and).append(" yt.state in (:states)");
            and = " and";
        }

        List<String> source = new ArrayList<String>();
        if (withSourceHosts) {
            source.add("yps.hostname in (:sourceHostnames)");
        }
        if (withSourceProtocols) {
            source.add("yps.protocol in (:sourceProtocols)");
        }
        if (withSourceProtocolHosts) {
            source.add("concat(yps.protocol,yps.hostname) in (:sourceProtocolHostnames)");
        }
        if (!source.isEmpty()) {
            hql.append(and).append(source.stream().collect(Collectors.joining(" or ", " (", ") ")));
            and = " and";
        }

        List<String> target = new ArrayList<String>();
        if (withTargetHosts) {
            target.add("ypt.hostname in (:targetHostnames)");
        }
        if (withTargetProtocols) {
            target.add("ypt.protocol in (:targetProtocols)");
        }
        if (withTargetProtocolHosts) {
            target.add("concat(ypt.protocol,ypt.hostname) in (:targetProtocolHostnames)");
        }
        if (!target.isEmpty()) {
            hql.append(and).append(target.stream().collect(Collectors.joining(" or ", " (", ") ")));
            and = " and";
        }

        if (withProfiles) {
            hql.append(and).append(filter.getProfiles().stream().map(p -> {
                if (SearchStringHelper.isGlobPattern(p)) {
                   return "yt.profileName like '" + SearchStringHelper.globToSqlPattern(p) + "'"; 
                } else {
                   return "yt.profileName = '" + p + "'"; 
                }
            }).collect(Collectors.joining(" or ", " (", ")")));
            and = " and";
        }
        if (filter.getDateFrom() != null) {
            hql.append(and).append(" yt.start >= :dateFrom");
            and = " and";
        }
        if (filter.getDateTo() != null) {
            hql.append(and).append(" yt.start < :dateTo");
        }
        if (onlyTransferIds) {
            hql.append(" group by yt.id");
        }
        Query<T> query = session.createQuery(hql.toString());
        if (withControllerIds) {
            query.setParameterList("controllerIds", filter.getControllerIds());
        }
        if (withTransferIds) {
            query.setParameterList("transferIds", filter.getTransferIds());
        }
        if (withOperations) {
            query.setParameterList("operations", filter.getOperations());
        }
        if (withSourceHosts) {
            query.setParameterList("sourceHostnames", filter.getSourceHosts());
        }
        if (withTargetHosts) {
            query.setParameterList("targetHostnames", filter.getTargetHosts());
        }
        if (withSourceProtocols) {
            query.setParameterList("sourceProtocols", filter.getSourceProtocols());
        }
        if (withTargetProtocols) {
            query.setParameterList("targetProtocols", filter.getTargetProtocols());
        }
        if (withSourceProtocolHosts) {
            query.setParameterList("sourceProtocolHostnames", filter.getSourceProtocolHosts());
        }
        if (withTargetProtocolHosts) {
            query.setParameterList("targetProtocolHostnames", filter.getTargetProtocolHosts());
        }
        if (withStates) {
            query.setParameterList("states", filter.getStates());
        }
        if (filter.getDateFrom() != null) {
            query.setParameter("dateFrom", filter.getDateFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getDateTo() != null) {
            query.setParameter("dateTo", filter.getDateTo(), TemporalType.TIMESTAMP);
        }
        if (!onlyTransferIds && filter.getLimit() != null && filter.getLimit() > 0) {
            query.setMaxResults(filter.getLimit());
        }
        return session.getResultList(query);
    }

    public DBItemYadeProtocol getProtocolById(Long id) throws SOSHibernateException {
        return session.get(DBItemYadeProtocol.class, id);
    }

    public List<DBItemYadeFile> getFilteredTransferFiles(List<Long> transferIds, FilesFilter filter, Integer limit) throws SOSHibernateException {
        boolean withTransferIds = transferIds != null && !transferIds.isEmpty();
        
        if (withTransferIds && transferIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemYadeFile> r = new ArrayList<>();
            for (int i = 0; i < transferIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getFilteredTransferFiles(SOSHibernate.getInClausePartition(i, transferIds), filter, limit));
                int resultSize = r.size();
                if (resultSize > limit) {
                    //reduce r to limit items
                    r.subList(limit, resultSize).clear();
                    break;
                }
            }
            return r;
        } else {
            boolean withSourceFiles = filter.getSourceFiles() != null && !filter.getSourceFiles().isEmpty();
            boolean withTargetFiles = filter.getTargetFiles() != null && !filter.getTargetFiles().isEmpty();
            boolean withStates = filter.getStates() != null && !filter.getStates().isEmpty();
            boolean withIntegrityHash = !SOSString.isEmpty(filter.getIntegrityHash());
            
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES);
            List<String> clauses = new ArrayList<>(5);
            if (withTransferIds) {
                clauses.add("transferId in (:transferIds)");
            }
            if (withStates) {
                clauses.add("state in (:states)");
            }
            if (withSourceFiles) {
                clauses.add(filter.getSourceFiles().stream().map(f -> {
                    if (SearchStringHelper.isGlobPattern(f)) {
                       return "sourcePath like '" + SearchStringHelper.globToSqlPattern(f) + "'"; 
                    } else {
                       return "sourcePath = '" + f + "'"; 
                    }
                }).collect(Collectors.joining(" or ", "(", ")")));
            }
            if (withTargetFiles) {
                clauses.add(filter.getTargetFiles().stream().map(f -> {
                    if (SearchStringHelper.isGlobPattern(f)) {
                       return "sourcePath like '" + SearchStringHelper.globToSqlPattern(f) + "'"; 
                    } else {
                       return "sourcePath = '" + f + "'"; 
                    }
                }).collect(Collectors.joining(" or ", "(", ")")));
            }
            if (withIntegrityHash) {
                clauses.add("integrityHash = :integrityHash");
            }
            if (!clauses.isEmpty()) {
                hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<DBItemYadeFile> query = session.createQuery(hql.toString());
            if (limit != null && limit > 0) {
                query.setMaxResults(limit);
            }
            if (withTransferIds) {
                query.setParameterList("transferIds", transferIds);
            }
            if (withStates) {
                query.setParameterList("states", filter.getStates().stream().map(s -> Yade.TransferEntryState.fromValue(s.value()).intValue()).collect(
                        Collectors.toSet()));
            }
            if (withIntegrityHash) {
                query.setParameter("integrityHash", filter.getIntegrityHash());
            }
            List<DBItemYadeFile> result = session.getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        }

    }

    public DBItemYadeTransfer getTransfer(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_TRANSFERS).append(" ");
        hql.append("where id=:id");

        Query<DBItemYadeTransfer> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public boolean transferHasFiles(Long transferId, List<String> sourceFiles, List<String> targetFiles) throws SOSHibernateException {
        boolean withSourceFiles = (sourceFiles != null && !sourceFiles.isEmpty());
        boolean withTargetFiles = (targetFiles != null && !targetFiles.isEmpty());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES);
        hql.append(" where transferId = :transferId");
        if (withSourceFiles && !withTargetFiles) {
            hql.append(" and");
            hql.append(" sourcePath in (:sourceFiles)");
        } else if (withTargetFiles && !withSourceFiles) {
            hql.append(" and");
            hql.append(" targetPath in (:targetFiles)");
        } else if (withSourceFiles && withTargetFiles) {
            hql.append(" and");
            hql.append(" (sourcePath in (:sourceFiles)");
            hql.append(" or");
            hql.append(" targetPath in (:targetFiles))");
        }
        Query<DBItemYadeFile> query = session.createQuery(hql.toString());
        query.setParameter("transferId", transferId);
        if (withSourceFiles) {
            query.setParameterList("sourceFiles", sourceFiles);
        }
        if (withTargetFiles) {
            query.setParameter("targetFiles", targetFiles);
        }
        List<DBItemYadeFile> foundFiles = session.getResultList(query);
        return (foundFiles != null && !foundFiles.isEmpty());
    }

    public List<Long> transferIdsFilteredBySourceTargetPath(Collection<String> sourceFiles, Collection<String> targetFiles) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("select transferId from ").append(DBLayer.DBITEM_YADE_FILES);
        List<String> clauses = new ArrayList<>(2);
        
        if (sourceFiles != null && !sourceFiles.isEmpty()) {
            clauses.add(sourceFiles.stream().map(f -> {
                if (SearchStringHelper.isGlobPattern(f)) {
                   return "sourcePath like '" + SearchStringHelper.globToSqlPattern(f) + "'"; 
                } else {
                   return "sourcePath = '" + f + "'"; 
                }
            }).collect(Collectors.joining(" or ", "(", ")")));
        }
        if (targetFiles != null && !targetFiles.isEmpty()) {
            clauses.add(targetFiles.stream().map(f -> {
                if (SearchStringHelper.isGlobPattern(f)) {
                   return "sourcePath like '" + SearchStringHelper.globToSqlPattern(f) + "'"; 
                } else {
                   return "sourcePath = '" + f + "'"; 
                }
            }).collect(Collectors.joining(" or ", "(", ")")));
        }
        

        if (!clauses.isEmpty()) {
            hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
        }
        hql.append(" group by transferId");
        Query<Long> query = session.createQuery(hql.toString());
        
        return session.getResultList(query);
    }

    public List<YadeSourceTargetFiles> SourceTargetFilePaths(List<Long> transferIds) throws SOSHibernateException {
        boolean withTransferIds = (transferIds != null && !transferIds.isEmpty());
        StringBuilder hql = new StringBuilder();
        hql.append("select new ").append(YADE_SOURCE_TARGET_FILES).append("(transferId, sourcePath, targetPath) from ");
        hql.append(DBLayer.DBITEM_YADE_FILES).append(" where");
        if (withTransferIds) {
            hql.append(" transferId in (:transferIds)");
        }
        Query<YadeSourceTargetFiles> query = session.createQuery(hql.toString());
        if (withTransferIds) {
            query.setParameterList("transferIds", transferIds);
        }
        return session.getResultList(query);
    }

    private Long getTransfersCount(Collection<String> controllerIds, boolean successful, Date from, Date to) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("select count(*) from ").append(DBLayer.DBITEM_YADE_TRANSFERS);
        hql.append(" where state = :state");
        if (controllerIds != null && !controllerIds.isEmpty()) {
            hql.append(" and controllerId in (:controllerIds)");
        }
        if (from != null) {
            hql.append(" and start >= :from");
        }
        if (to != null) {
            hql.append(" and start < :to");
        }
        Query<Long> query = session.createQuery(hql.toString());
        if (successful) {
            query.setParameter("state", TransferState.SUCCESSFUL.intValue());
        } else {
            query.setParameter("state", TransferState.FAILED.intValue());
        }
        if (from != null) {
            query.setParameter("from", from, TemporalType.TIMESTAMP);
        }
        if (to != null) {
            query.setParameter("to", to, TemporalType.TIMESTAMP);
        }
        if (controllerIds != null && !controllerIds.isEmpty()) {
            query.setParameterList("controllerIds", controllerIds);
        }
        return session.getSingleResult(query);
    }

    private Long getTransfersCount(Collection<String> controllerIds, boolean successful, Date from, Date to,
            Map<String, Set<Folder>> permittedFoldersMap) throws SOSHibernateException {
        if (permittedFoldersMap == null || permittedFoldersMap.isEmpty()) {
            return getTransfersCount(controllerIds, successful, from, to);
        }
        StringBuilder hql = new StringBuilder();
        hql.append("select new ").append(YADE_GROUPED_SUMMARY).append("(count(id), controllerId, workflowPath) from ");
        hql.append(DBLayer.DBITEM_YADE_TRANSFERS);
        hql.append(" where state = :state");

        if (controllerIds != null && !controllerIds.isEmpty()) {
            hql.append(" and controllerId in (:controllerIds)");
        }
        if (from != null) {
            hql.append(" and start >= :from");
        }
        if (to != null) {
            hql.append(" and start < :to");
        }
        hql.append(" group by controllerId, workflowPath");
        Query<YadeGroupedSummary> query = session.createQuery(hql.toString());
        if (successful) {
            query.setParameter("state", TransferState.SUCCESSFUL.intValue());
        } else {
            query.setParameter("state", TransferState.FAILED.intValue());
        }
        if (from != null) {
            query.setParameter("from", from, TemporalType.TIMESTAMP);
        }
        if (to != null) {
            query.setParameter("to", to, TemporalType.TIMESTAMP);
        }
        if (controllerIds != null && !controllerIds.isEmpty()) {
            query.setParameterList("controllerIds", controllerIds);
        }

        List<YadeGroupedSummary> result = session.getResultList(query);
        if (result != null) {
            return result.stream().filter(s -> isPermittedForFolder(s.getFolder(), permittedFoldersMap.get(s.getControllerId()))).mapToLong(s -> s
                    .getCount()).sum();
        }
        return 0L;
    }

    private static boolean isPermittedForFolder(String folder, Collection<Folder> permittedFolders) {
        if (folder == null || folder.isEmpty()) {
            return true;
        }
        if (permittedFolders == null || permittedFolders.isEmpty()) {
            return true;
        }
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return permittedFolders.stream().parallel().anyMatch(filter);
    }

    public Long getSuccessFulTransfersCount(Collection<String> controllerIds, Date from, Date to, Map<String, Set<Folder>> permittedFoldersMap)
            throws SOSHibernateException {
        return getTransfersCount(controllerIds, true, from, to, permittedFoldersMap);
    }

    public Long getFailedTransfersCount(Collection<String> controllerIds, Date from, Date to, Map<String, Set<Folder>> permittedFoldersMap)
            throws SOSHibernateException {
        return getTransfersCount(controllerIds, false, from, to, permittedFoldersMap);
    }

}