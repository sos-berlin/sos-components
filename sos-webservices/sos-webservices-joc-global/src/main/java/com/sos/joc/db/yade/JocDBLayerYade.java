package com.sos.joc.db.yade;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.yade.FileTransferStateText;
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
    public Integer getSuccessfulTransferredFilesCount(Date from, Date to) throws DBInvalidDataException, DBConnectionRefusedException {
        return getTransferredFilesCount(from, to, TransferEntryState.TRANSFERRED);
    }

    // TODO: at the moment only state = 7 (TRANSFER_HAS_ERRORS) is checked
    public Integer getFailedTransferredFilesCount(Date from, Date to) throws DBInvalidDataException, DBConnectionRefusedException {
        return getTransferredFilesCount(from, to, TransferEntryState.FAILED);
    }

    private Integer getTransferredFilesCount(Date from, Date to, TransferEntryState state) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("select count(*) from ");
            hql.append(DBLayer.DBITEM_YADE_FILES).append(" yf, ");
            hql.append(DBLayer.DBITEM_YADE_TRANSFERS).append(" yt ");
            hql.append("where yf.transferId=yt.id ");
            if (from != null) {
                hql.append("and yt.end >= :from ");
            }
            if (to != null) {
                hql.append("and yt.end < :to ");
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    // TODO: at the moment only state = 7 (TRANSFER_HAS_ERRORS) is checked
    public List<DBItemYadeFile> getFailedTransferredFiles(Long transferId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES).append(" ");
            hql.append("where state = :state");
            hql.append("and transferId=:transferId ");

            Query<DBItemYadeFile> query = session.createQuery(hql.toString());
            query.setParameter("state", TransferEntryState.FAILED.intValue());
            query.setParameter("transferId", transferId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemYadeFile> getFilesById(List<Long> ids) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getSourceFilesByIdsAndTransferId(Long transferId, List<Long> ids) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemYadeTransfer> getAllTransfers() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_TRANSFERS);

            Query<DBItemYadeTransfer> query = session.createQuery(hql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemYadeFile getTransferFile(Long id) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES).append(" ");
            hql.append("where id=:id");

            Query<DBItemYadeFile> query = session.createQuery(hql.toString());
            query.setParameter("id", id);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemYadeTransfer> getFilteredTransfers(JocYadeFilter filter) throws DBInvalidDataException, DBConnectionRefusedException {
        return getTransfers(filter, false);
    }

    public List<Long> getFilteredTransferIds(JocYadeFilter filter) throws DBInvalidDataException, DBConnectionRefusedException {
        return getTransfers(filter, true);
    }

    private <T> List<T> getTransfers(JocYadeFilter filter, boolean onlyTransferIds) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            boolean withControllerId = filter.getControllerId() != null && !filter.getControllerId().isEmpty();
            boolean withTransferIds = filter.getTransferIds() != null && !filter.getTransferIds().isEmpty();
            boolean withOperations = filter.getOperations() != null && !filter.getOperations().isEmpty();
            boolean withStates = filter.getStates() != null && !filter.getStates().isEmpty();
            boolean withSourceHosts = filter.getSourceHosts() != null && !filter.getSourceHosts().isEmpty();
            boolean withSourceProtocols = filter.getSourceProtocols() != null && !filter.getSourceProtocols().isEmpty();
            boolean withTargetHosts = filter.getTargetHosts() != null && !filter.getTargetHosts().isEmpty();
            boolean withTargetProtocols = filter.getTargetProtocols() != null && !filter.getTargetProtocols().isEmpty();
            boolean withProfiles = filter.getProfiles() != null && !filter.getProfiles().isEmpty();
            String and = " where";

            StringBuilder hql = new StringBuilder();
            hql.append("select yt");
            if (onlyTransferIds) {
                hql.append(".id");
            }
            hql.append(" from ").append(DBLayer.DBITEM_YADE_TRANSFERS).append(" yt");
            if (withSourceHosts || withSourceProtocols) {
                hql.append(", ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" yps");
            }
            if (withTargetHosts || withTargetProtocols) {
                hql.append(", ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" ypt");
            }
            if (withSourceHosts || withSourceProtocols) {
                hql.append(and).append(" yt.sourceProtocolId = yps.id");
                and = " and";
            }
            if (withTargetHosts || withTargetProtocols) {
                hql.append(and).append(" yt.targetProtocolId is not null and yt.targetProtocolId = ypt.id");
                and = " and";
            }
            if (withControllerId) {
                hql.append(and).append(" yt.controllerId = :controllerId");
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
            if (withSourceHosts) {
                hql.append(and).append(SearchStringHelper.getStringSetSql(filter.getSourceHosts(), "yps.hostname"));
                and = " and";
            }

            if (withTargetHosts) {
                hql.append(and).append(SearchStringHelper.getStringSetSql(filter.getTargetHosts(), "ypt.hostname"));
                and = " and";
            }

            if (withSourceProtocols) {
                hql.append(and).append(" yps.protocol in (:sourceProtocols)");
                and = " and";
            }
            if (withTargetProtocols) {
                hql.append(and).append(" ypt.protocol in (:targetProtocols)");
                and = " and";
            }
            if (withProfiles) {
                hql.append(and).append(SearchStringHelper.getStringListPathSql(filter.getProfiles(), "yt.profileName"));
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
            if (withControllerId) {
                query.setParameter("controllerId", filter.getControllerId());
            }
            if (withTransferIds) {
                query.setParameter("transferIds", filter.getTransferIds());
            }
            if (withOperations) {
                query.setParameterList("operations", filter.getOperations());
            }
            if (withSourceProtocols) {
                query.setParameterList("sourceProtocols", filter.getSourceProtocols());
            }
            if (withTargetProtocols) {
                query.setParameterList("targetProtocols", filter.getTargetProtocols());
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemYadeProtocol getProtocolById(Long id) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_PROTOCOLS).append(" ");
            hql.append("where id=:id");

            Query<DBItemYadeProtocol> query = session.createQuery(hql.toString());
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemYadeFile> getFilteredTransferFiles(List<Long> transferIds, List<FileTransferStateText> states, List<String> sources,
            List<String> targets, List<Long> interventionTransferIds, Integer limit) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            boolean anotherValueAlreadySet = false;
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_FILES);
            if ((transferIds != null && !transferIds.isEmpty()) || (states != null && !states.isEmpty()) || (sources != null && !sources.isEmpty())
                    || (targets != null && !targets.isEmpty()) || (interventionTransferIds != null && !interventionTransferIds.isEmpty())) {
                hql.append(" where ");
                if (transferIds != null && !transferIds.isEmpty()) {
                    boolean first = true;
                    hql.append("transferId in (");
                    for (Long transferId : transferIds) {
                        if (first) {
                            first = false;
                            hql.append(transferId.toString());
                        } else {
                            hql.append(", ").append(transferId.toString());
                        }
                    }
                    hql.append(")");
                    anotherValueAlreadySet = true;
                }
                if (states != null && !states.isEmpty()) {
                    if (anotherValueAlreadySet) {
                        hql.append(" and");
                    }
                    boolean first = true;
                    hql.append("state in (");
                    for (FileTransferStateText state : states) {
                        if (first) {
                            first = false;
                            hql.append(state.name());
                        } else {
                            hql.append(", ").append(state.name());
                        }
                    }
                    hql.append(")");
                    anotherValueAlreadySet = true;
                }
                if (sources != null && !sources.isEmpty()) {
                    if (anotherValueAlreadySet) {
                        hql.append(" and");
                    }
                    boolean first = true;
                    hql.append("sourcePath in (");
                    for (String source : sources) {
                        if (first) {
                            first = false;
                            hql.append(source);
                        } else {
                            hql.append(", ").append(source);
                        }
                    }
                    hql.append(")");
                    anotherValueAlreadySet = true;
                }
                if (targets != null && !targets.isEmpty()) {
                    if (anotherValueAlreadySet) {
                        hql.append(" and");
                    }
                    boolean first = true;
                    hql.append("targetPath in (");
                    for (String target : targets) {
                        if (first) {
                            first = false;
                            hql.append(target);
                        } else {
                            hql.append(", ").append(target);
                        }
                    }
                    hql.append(")");
                    anotherValueAlreadySet = true;
                }
                if (interventionTransferIds != null && !interventionTransferIds.isEmpty()) {
                    if (anotherValueAlreadySet) {
                        hql.append(" and");
                    }
                    boolean first = true;
                    hql.append("interventionTransferId in (");
                    for (Long interventionTransferId : interventionTransferIds) {
                        if (first) {
                            first = false;
                            hql.append(interventionTransferId);
                        } else {
                            hql.append(", ").append(interventionTransferId);
                        }
                    }
                    hql.append(")");
                    anotherValueAlreadySet = true;
                }
            }
            Query<DBItemYadeFile> query = session.createQuery(hql.toString());
            if (limit != null && limit > 0) {
                query.setMaxResults(limit);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemYadeTransfer getTransfer(Long id) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_YADE_TRANSFERS).append(" ");
            hql.append("where id=:id");

            Query<DBItemYadeTransfer> query = session.createQuery(hql.toString());
            query.setParameter("id", id);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public boolean transferHasFiles(Long transferId, List<String> sourceFiles, List<String> targetFiles) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<Long> transferIdsFilteredBySourceTargetPath(List<Long> transferIds, List<String> sourceFiles, List<String> targetFiles)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            boolean withTransferIds = (transferIds != null && !transferIds.isEmpty());
            boolean withSourceFiles = (sourceFiles != null && !sourceFiles.isEmpty());
            boolean withTargetFiles = (targetFiles != null && !targetFiles.isEmpty());
            String and = " where";
            StringBuilder hql = new StringBuilder();
            hql.append("select transferId from ");
            hql.append(DBLayer.DBITEM_YADE_FILES);
            if (withTransferIds) {
                hql.append(and).append(" transferId in (:transferIds)");
                and = " and";
            }
            if (withSourceFiles) {
                hql.append(and).append(SearchStringHelper.getStringListPathSql(sourceFiles, "sourcePath"));
                and = " and";
            }
            if (withTargetFiles) {
                hql.append(and).append(SearchStringHelper.getStringListPathSql(targetFiles, "targetPath"));
            }
            hql.append(" group by transferId");
            Query<Long> query = session.createQuery(hql.toString());
            if (withTransferIds) {
                query.setParameterList("transferIds", transferIds);
            }

            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<YadeSourceTargetFiles> SourceTargetFilePaths(List<Long> transferIds) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
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
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private Integer getTransfersCount(String controllerId, boolean successFull, Date from, Date to) throws SOSHibernateException,
            DBInvalidDataException, DBConnectionRefusedException {
        StringBuilder hql = new StringBuilder();
        hql.append("select count(*) from ");
        hql.append(DBLayer.DBITEM_YADE_TRANSFERS).append(" transfer");
        if (successFull) {
            hql.append(" where state = 1");
        } else {
            hql.append(" where state = 3");
        }
        if (controllerId != null) {
            hql.append(" and controllerId = :controllerId");
        }
        if (from != null) {
            hql.append(" and transfer.end >= :from");
        }
        if (to != null) {
            hql.append(" and transfer.end < :to");
        }
        Query<Long> query = session.createQuery(hql.toString());
        if (from != null) {
            query.setParameter("from", from, TemporalType.TIMESTAMP);
        }
        if (to != null) {
            query.setParameter("to", to, TemporalType.TIMESTAMP);
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }

        try {
            return session.getSingleResult(query).intValue();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private Integer getTransfersCount(String controllerId, boolean successFull, Date from, Date to, Collection<Folder> permittedFolders)
            throws SOSHibernateException, DBInvalidDataException, DBConnectionRefusedException {
        if (permittedFolders == null || permittedFolders.isEmpty()) {
            return getTransfersCount(controllerId, successFull, from, to);
        }
        StringBuilder hql = new StringBuilder();
        hql.append("select new ").append(YADE_GROUPED_SUMMARY).append("(count(*), workflowPath) from ");
        hql.append(DBLayer.DBITEM_YADE_TRANSFERS).append(" transfer");
        TransferState state = TransferState.UNKNOWN;
        if (successFull) {
            hql.append(" where state = :state");
            state = TransferState.SUCCESSFUL;
        } else {
            hql.append(" where state = :state");
            state = TransferState.FAILED;
        }
        if (controllerId != null) {
            hql.append(" and controllerId = :controllerId");
        }
        if (from != null) {
            hql.append(" and transfer.end >= :from");
        }
        if (to != null) {
            hql.append(" and transfer.end < :to");
        }
        hql.append(" group by jobChain, job");
        Query<YadeGroupedSummary> query = session.createQuery(hql.toString());
        query.setParameter("state", state.intValue());
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        if (controllerId != null) {
            query.setParameter("controllerId", controllerId);
        }

        try {
            List<YadeGroupedSummary> result = session.getResultList(query);
            if (result != null) {
                return result.stream().filter(s -> isPermittedForFolder(s.getFolder(), permittedFolders)).mapToInt(s -> s.getCount()).sum();
            }
            return 0;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
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

    public Integer getSuccessFulTransfersCount(String controllerId, Date from, Date to, Collection<Folder> folders) throws SOSHibernateException,
            DBInvalidDataException, DBConnectionRefusedException {
        return getTransfersCount(controllerId, true, from, to, folders);
    }

    public Integer getFailedTransfersCount(String controllerId, Date from, Date to, Collection<Folder> folders) throws SOSHibernateException,
            DBInvalidDataException, DBConnectionRefusedException {
        return getTransfersCount(controllerId, false, from, to, folders);
    }

}