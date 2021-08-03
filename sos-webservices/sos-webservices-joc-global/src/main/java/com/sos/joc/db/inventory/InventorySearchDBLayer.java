package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue;
import com.sos.commons.hibernate.function.json.SOSHibernateJsonValue.ReturnType;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;

public class InventorySearchDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventorySearchDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<InventorySearchItem> getInventoryConfigurations(ConfigurationType type, String search, List<String> folders)
            throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
        hql.append(",mt.name as name ");
        hql.append(",mt.title as title ");
        hql.append(",mt.valid as valid ");
        hql.append(",mt.deleted as deleted ");
        hql.append(",mt.deployed as deployed ");
        hql.append(",mt.released as released ");
        if (isReleasable) {
            hql.append(",count(irc.id) as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on mt.id=irc.cid ");
        } else {
            hql.append(",0 as countReleased ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on mt.id=dh.inventoryConfigurationId ");
        }
        hql.append("where mt.type=:type ");
        if (!SOSString.isEmpty(search)) {
            hql.append("and (mt.name like :search or mt.title like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        hql.append("group by mt.id");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (!SOSString.isEmpty(search)) {
            query.setParameter("search", '%' + search + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        return getSession().getResultList(query);
    }

    // TODO merge all functions ...
    public List<InventorySearchItem> getAdvancedInventoryConfigurations(ConfigurationType type, String search, List<String> folders,
            RequestSearchAdvancedItem advanced) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select mt.id as id ");
        hql.append(",mt.path as path ");
        hql.append(",mt.name as name ");
        hql.append(",mt.title as title ");
        hql.append(",mt.valid as valid ");
        hql.append(",mt.deleted as deleted ");
        hql.append(",mt.deployed as deployed ");
        hql.append(",mt.released as released ");
        if (isReleasable) {
            hql.append(",count(irc.id) as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on mt.id=irc.cid ");
            hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on mt.id=sw.inventoryConfigurationId and mt.released=sw.deployed ");
        } else {
            hql.append(",0 as countReleased ");
            hql.append(",count(dh.id) as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" mt ");
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on mt.id=dh.inventoryConfigurationId ");
            hql.append("left join ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" sw ");
            hql.append("on mt.id=sw.inventoryConfigurationId and mt.deployed=sw.deployed ");
        }
        hql.append("where mt.type=:type ");
        if (!SOSString.isEmpty(search)) {
            hql.append("and (mt.name like :search or mt.title like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }

        /*------------------------*/
        if (!SOSString.isEmpty(advanced.getAgentName())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.agentIds");
            hql.append("and ").append(jsonFunc).append(" like :agentName ");
        }
        if (advanced.getJobCountFrom() != null) {
            hql.append("and sw.jobsCount >= :jobCountFrom ");
        }
        if (advanced.getJobCountTo() != null) {
            hql.append("and sw.jobsCount <= :jobCountTo ");
        }
        if (!SOSString.isEmpty(advanced.getJobName())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.names");
            // hql.append("and ").append(SOSHibernateRegexp.getFunction(jsonFunc, ":jobName")).append(" ");
            hql.append("and ").append(jsonFunc).append(" like :jobName ");
        }
        boolean setJobCriticality = false;
        if (advanced.getJobCriticality() != null) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.criticalities");
            hql.append("and ").append(jsonFunc).append(" ");
            // TODO
            if (advanced.getJobCriticality().equals(JobCriticality.NORMAL)) {
                hql.append("is null ");
            } else {
                setJobCriticality = true;
                hql.append("like :jobCriticality ");
            }
        }
        if (!SOSString.isEmpty(advanced.getJobResources())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.jobs", "$.jobResources");
            hql.append("and ").append(jsonFunc).append(" like :jobResources ");
        }
        if (!SOSString.isEmpty(advanced.getBoards())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.boardNames");
            hql.append("and ").append(jsonFunc).append(" like :boards ");
        }
        if (!SOSString.isEmpty(advanced.getLock())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructions", "$.lockIds");
            hql.append("and ").append(jsonFunc).append(" like :lock ");
        }
        if (!SOSString.isEmpty(advanced.getArgumentName())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructionsArgs", "$.jobArgNames");
            hql.append("and ").append(jsonFunc).append(" like :argumentName ");
        }
        if (!SOSString.isEmpty(advanced.getArgumentValue())) {
            String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "sw.instructionsArgs", "$.jobArgvalues");
            hql.append("and ").append(jsonFunc).append(" like :argumentValue ");
        }
        hql.append("group by mt.id");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (!SOSString.isEmpty(search)) {
            query.setParameter("search", '%' + search + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        /*------------------------*/
        if (!SOSString.isEmpty(advanced.getAgentName())) {
            query.setParameter("agentName", '%' + advanced.getAgentName() + '%');
        }
        if (advanced.getJobCountFrom() != null) {
            query.setParameter("jobCountFrom", advanced.getJobCountFrom());
        }
        if (advanced.getJobCountTo() != null) {
            query.setParameter("jobCountTo", advanced.getJobCountTo());
        }
        if (!SOSString.isEmpty(advanced.getJobName())) {
            query.setParameter("jobName", '%' + advanced.getJobName() + '%');
        }
        if (setJobCriticality) {
            query.setParameter("jobCriticality", '%' + advanced.getJobCriticality().value() + '%');
        }
        if (!SOSString.isEmpty(advanced.getJobResources())) {
            query.setParameter("jobResources", '%' + advanced.getJobResources() + '%');
        }
        if (!SOSString.isEmpty(advanced.getBoards())) {
            query.setParameter("boards", '%' + advanced.getBoards() + '%');
        }
        if (!SOSString.isEmpty(advanced.getLock())) {
            query.setParameter("lock", '%' + advanced.getLock() + '%');
        }
        if (!SOSString.isEmpty(advanced.getArgumentName())) {
            query.setParameter("argumentName", '%' + advanced.getArgumentName() + '%');
        }
        if (!SOSString.isEmpty(advanced.getArgumentValue())) {
            query.setParameter("argumentValue", '%' + advanced.getArgumentValue() + '%');
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getDeployedOrReleasedConfigurations(ConfigurationType type, String search, List<String> folders,
            String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        boolean searchInFolders = searchInFolders(folders);

        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",false as deployed ");
            hql.append(",true as released ");
            hql.append(",1 as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" mt ");
        } else {
            hql.append("mt.inventoryConfigurationId as id");
            hql.append(",mt.path as path");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",mt.controllerId as controllerId ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",true as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countReleased ");
            hql.append(",1 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" mt ");
        }
        hql.append("where mt.type=:type ");
        if (!SOSString.isEmpty(search)) {
            hql.append("and (mt.name like :search or mt.title like :search) ");
        }
        if (searchInFolders) {
            hql.append("and (").append(foldersHql(folders)).append(") ");
        }
        if (!isReleasable && !SOSString.isEmpty(controllerId)) {
            hql.append("and mt.controllerId=:controllerId ");
        }

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (!SOSString.isEmpty(search)) {
            query.setParameter("search", '%' + search + '%');
        }
        if (searchInFolders) {
            foldersQueryParameters(query, folders);
        }
        if (!isReleasable && !SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        return getSession().getResultList(query);
    }

    private boolean isReleasable(ConfigurationType type) {
        switch (type) {
        case SCHEDULE:
            return true;
        default:
            return false;
        }
    }

    private boolean searchInFolders(List<String> folders) {
        return folders != null && folders.size() > 0 && !folders.contains("/");
    }

    private String foldersHql(List<String> folders) {
        List<String> f = new ArrayList<>();
        for (int i = 0; i < folders.size(); i++) {
            f.add("mt.folder like :folder" + i + " ");
        }
        return String.join(" or ", f);
    }

    private void foldersQueryParameters(Query<InventorySearchItem> query, List<String> folders) {
        for (int i = 0; i < folders.size(); i++) {
            query.setParameter("folder" + i, folders.get(i) + '%');
        }
    }
}
