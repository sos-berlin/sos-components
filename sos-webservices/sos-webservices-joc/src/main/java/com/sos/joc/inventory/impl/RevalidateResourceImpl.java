package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.RevalidateCallable;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IRevalidateResource;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.validate.Report;
import com.sos.joc.model.inventory.validate.ReportItem;
import com.sos.joc.model.inventory.validate.RequestFolder;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class RevalidateResourceImpl extends JOCResourceImpl implements IRevalidateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevalidateResourceImpl.class);
    private static final List<ConfigurationType> types = Arrays.asList(ConfigurationType.LOCK, ConfigurationType.INCLUDESCRIPT,
            ConfigurationType.NOTICEBOARD, ConfigurationType.JOBRESOURCE, ConfigurationType.NONWORKINGDAYSCALENDAR,
            ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.JOBTEMPLATE, ConfigurationType.WORKFLOW, ConfigurationType.FILEORDERSOURCE,
            ConfigurationType.SCHEDULE);
    // exclusive WORKFLOW, gets an extra handling
    private static final List<ConfigurationType> refObjectTypes = Arrays.asList(ConfigurationType.LOCK, ConfigurationType.INCLUDESCRIPT,
            ConfigurationType.NOTICEBOARD, ConfigurationType.JOBRESOURCE, ConfigurationType.NONWORKINGDAYSCALENDAR,
            ConfigurationType.WORKINGDAYSCALENDAR);

    @Override
    public JOCDefaultResponse revalidate(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + in.getPath());
            }

            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(revalidate(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Report revalidate(RequestFolder in) throws Exception {
        DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

        Predicate<DBItemInventoryConfiguration> folderFilter = dbItem -> true;
        if (in.getRecursive()) {
            if (!"/".equals(in.getPath())) {
                folderFilter = dbItem -> dbItem.getFolder().startsWith(in.getPath());
            }
        } else {
            folderFilter = dbItem -> dbItem.getFolder().equals(in.getPath());
        }

        Report report = revalidate(folderFilter, getJocError(), dbAuditLog);
        Stream.concat(report.getInvalidObjs().stream(), report.getValidObjs().stream()).map(ReportItem::getPath).map(JOCResourceImpl::getParent)
                .distinct().forEach(JocInventory::postEvent);

        report.setDeliveryDate(Date.from(Instant.now()));

        return report;
    }
    
    public static Report revalidate(Collection<DBItemInventoryConfiguration> dbItems, JocError jocError) throws SOSHibernateException,
            InterruptedException {
        SOSHibernateSession session = null;
        ExecutorService executorService = null;

        try {
            Report report = new Report();
            Comparator<ReportItem> comp = Comparator.comparing(ReportItem::getPath).thenComparing(ReportItem::getObjectType);
            SortedSet<ReportItem> valids = new TreeSet<>(comp);
            SortedSet<ReportItem> invalids = new TreeSet<>(comp);
            SortedSet<ReportItem> errornous = new TreeSet<>(comp);
            List<AuditLogDetail> auditDetails = new ArrayList<>();

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(session);
            Set<String> agentNames = agentDbLayer.getVisibleAgentNames();

            Map<ConfigurationType, List<DBItemInventoryConfiguration>> inventoryObjectsByType = dbLayer.getConfigurationsByType(null).stream()
                    .collect(Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum));

            Map<ConfigurationType, Set<String>> inventoryObjectNamesByType = new HashMap<>();
            refObjectTypes.forEach(type -> inventoryObjectNamesByType.put(type, inventoryObjectsByType.getOrDefault(type, Collections.emptyList())
                    .stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet())));

            Map<String, String> workflowJsonsByName = inventoryObjectsByType.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyList())
                    .stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getName, DBItemInventoryConfiguration::getContent));

            List<RevalidateCallable> tasks = new ArrayList<>();
            dbItems.stream().forEach(dbItem -> {
                try {
                    IConfigurationObject conf = JocInventory.content2IJSObject(dbItem.getContent(), dbItem.getTypeAsEnum());
                    tasks.add(new RevalidateCallable(conf, dbItem, inventoryObjectNamesByType, workflowJsonsByName, agentNames));
                } catch (Exception e) {
                    ReportItem reportItem = createReportItem(dbItem);
                    reportItem.setError(new BulkError(LOGGER).get(e, jocError, dbItem.getPath()));
                    errornous.add(reportItem);
                }
            });

            int maxTasks = tasks.size();

            if (!tasks.isEmpty()) {
                if (tasks.size() == 1) {
                    setReportItem(tasks.get(0).call(), dbLayer, null, valids, invalids, errornous, auditDetails, jocError);
                } else {
                    executorService = Executors.newFixedThreadPool(Math.min(maxTasks, 32));
                    for (Future<RevalidateCallable> result : executorService.invokeAll(tasks)) {
                        try {
                            setReportItem(result.get(), dbLayer, null, valids, invalids, errornous, auditDetails, jocError);
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }

            if (!errornous.isEmpty()) {
                report.setErroneousObjs(errornous);
            }
            if (!valids.isEmpty()) {
                report.setValidObjs(valids);
            }
            if (!invalids.isEmpty()) {
                report.setInvalidObjs(invalids);
            }

            return report;
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
            Globals.disconnect(session);
        }
    }

    private static Report revalidate(Predicate<DBItemInventoryConfiguration> folderFilter, JocError jocError,
            DBItemJocAuditLog dbAuditLog) throws SOSHibernateException, InterruptedException {

        SOSHibernateSession session = null;
        ExecutorService executorService = null;

        try {
            Report report = new Report();
            Comparator<ReportItem> comp = Comparator.comparing(ReportItem::getPath).thenComparing(ReportItem::getObjectType);
            SortedSet<ReportItem> valids = new TreeSet<>(comp);
            SortedSet<ReportItem> invalids = new TreeSet<>(comp);
            SortedSet<ReportItem> errornous = new TreeSet<>(comp);
            List<AuditLogDetail> auditDetails = new ArrayList<>();

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(session);
            Set<String> agentNames = agentDbLayer.getVisibleAgentNames();

            Map<ConfigurationType, List<DBItemInventoryConfiguration>> inventoryObjectsByType = dbLayer.getConfigurationsByType(null).stream()
                    .collect(Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum));

            Map<ConfigurationType, Set<String>> inventoryObjectNamesByType = new HashMap<>();
            refObjectTypes.forEach(type -> inventoryObjectNamesByType.put(type, inventoryObjectsByType.getOrDefault(type, Collections.emptyList())
                    .stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet())));

            Map<String, String> workflowJsonsByName = inventoryObjectsByType.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyList())
                    .stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getName, DBItemInventoryConfiguration::getContent));

            List<RevalidateCallable> tasks = new ArrayList<>();
            types.forEach(type -> {
                inventoryObjectsByType.getOrDefault(type, Collections.emptyList()).stream().filter(folderFilter).forEach(dbItem -> {
                    try {
                        IConfigurationObject conf = JocInventory.content2IJSObject(dbItem.getContent(), dbItem.getTypeAsEnum());
                        tasks.add(new RevalidateCallable(conf, dbItem, inventoryObjectNamesByType, workflowJsonsByName, agentNames));
                    } catch (Exception e) {
                        ReportItem reportItem = createReportItem(dbItem);
                        reportItem.setError(new BulkError(LOGGER).get(e, jocError, dbItem.getPath()));
                        errornous.add(reportItem);
                    }
                });
                inventoryObjectsByType.remove(type);
            });

            int maxTasks = tasks.size();

            if (!tasks.isEmpty()) {
                if (tasks.size() == 1) {
                    setReportItem(tasks.get(0).call(), dbLayer, dbAuditLog, valids, invalids, errornous, auditDetails, jocError);
                } else {
                    executorService = Executors.newFixedThreadPool(Math.min(maxTasks, 32));
                    for (Future<RevalidateCallable> result : executorService.invokeAll(tasks)) {
                        try {
                            setReportItem(result.get(), dbLayer, dbAuditLog, valids, invalids, errornous, auditDetails, jocError);
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }

            if (dbAuditLog != null && !auditDetails.isEmpty()) {
                JocAuditLog.storeAuditLogDetails(auditDetails, dbLayer.getSession(), dbAuditLog);
            }

            if (!errornous.isEmpty()) {
                report.setErroneousObjs(errornous);
            }
            if (!valids.isEmpty()) {
                report.setValidObjs(valids);
            }
            if (!invalids.isEmpty()) {
                report.setInvalidObjs(invalids);
            }

            return report;
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
            Globals.disconnect(session);
        }
    }
    
    
    private static void setReportItem(RevalidateCallable callable, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog,
            Set<ReportItem> valids, Set<ReportItem> invalids, Set<ReportItem> errornous, List<AuditLogDetail> auditDetails, JocError jocError) {
        DBItemInventoryConfiguration dbItem = callable.getDbItem();
        ReportItem reportItem = createReportItem(dbItem);
        if (!callable.hasThrowable()) {
            reportItem.setValid(true);
            if (!dbItem.getValid()) {
                dbItem.setValid(true);
                if (dbAuditLog != null) {
                    dbItem.setAuditLogId(dbAuditLog.getId());
                }
                try {
                    JocInventory.updateConfiguration(dbLayer, dbItem, callable.getConf());
                    valids.add(reportItem);
                    if (dbAuditLog != null) {
                        auditDetails.add(new AuditLogDetail(dbItem.getPath(), dbItem.getType()));
                    }
                } catch (Exception e1) {
                    reportItem.setError(new BulkError(LOGGER).get(e1, jocError, dbItem.getPath()));
                    errornous.add(reportItem);
                }
            } else {
                // only change objects in then response valids.add(reportItem);
            }
        } else {
            reportItem.setValid(false);
            reportItem.setInvalidMsg(callable.getThrowable().getMessage());
            if (dbItem.getValid()) {
                dbItem.setValid(false);
                dbItem.setDeployed(false);
                dbItem.setReleased(false);
                if (dbAuditLog != null) {
                    dbItem.setAuditLogId(dbAuditLog.getId());
                }
                try {
                    JocInventory.updateConfiguration(dbLayer, dbItem, callable.getConf());
                    invalids.add(reportItem);
                    if (dbAuditLog != null) {
                        auditDetails.add(new AuditLogDetail(dbItem.getPath(), dbItem.getType()));
                    }
                } catch (Exception e1) {
                    reportItem.setError(new BulkError(LOGGER).get(e1, jocError, dbItem.getPath()));
                    errornous.add(reportItem);
                }
            } else {
                // only change objects in then response invalids.add(reportItem);
            }
        }
    }

    private static ReportItem createReportItem(DBItemInventoryConfiguration dbItem) {
        ReportItem reportItem = new ReportItem();
        reportItem.setValid(dbItem.getValid());
        reportItem.setPath(dbItem.getPath());
        reportItem.setName(dbItem.getName());
        reportItem.setObjectType(dbItem.getTypeAsEnum());
        reportItem.setTitle(dbItem.getTitle());
        return reportItem;
    }
}
