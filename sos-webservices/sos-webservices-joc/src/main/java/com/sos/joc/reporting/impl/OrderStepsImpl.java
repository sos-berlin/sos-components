package com.sos.joc.reporting.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.hibernate.ScrollableResults;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.reporting.CSVColumns;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.history.items.CSVItem;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.UnknownJobSchedulerControllerException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.TaskIdOfOrder;
import com.sos.joc.reporting.resource.IOrderStepsResource;
import com.sos.joc.tasks.impl.TasksResourceHistoryImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path(WebservicePaths.REPORTING)
public class OrderStepsImpl extends JOCResourceImpl implements IOrderStepsResource {
    
    public final class MyStreamingOutput implements StreamingOutput {

        private final boolean withGzipEncoding;
        private final ScrollableResults result;
        private final SOSHibernateSession session;
        private final boolean isFolderPermissionsAreChecked;
        private final Set<Folder> permittedFolders;
        private final String headline;


        public MyStreamingOutput(boolean withGzipEncoding, HistoryFilter filter, Collection<CSVColumns> osColumns,
                List<TaskIdOfOrder> historyIds, Set<Folder> permittedFolders, String action) throws JocException, IOException {
            this.withGzipEncoding = withGzipEncoding;
            this.session = Globals.createSosHibernateStatelessConnection(action);
            this.isFolderPermissionsAreChecked= filter.isFolderPermissionsAreChecked();
            this.permittedFolders = permittedFolders;
            this.headline = "";//osColumns.stream().map(CSVColumns::value).collect(Collectors.joining(";")) + "\n";
            try {
                result = null; //getResult(filter, osColumns.stream().map(CSVColumns::strValue), historyIds);
            } catch (JocException e) {
                Globals.disconnect(session);
                throw e;
            }
        }

        @Override
        public void write(OutputStream output) throws IOException {
            try {
                if (withGzipEncoding) {
                    output = new GZIPOutputStream(output);
                }
                if (result != null) {
                    Map<String, Boolean> checkedFolders = new HashMap<>();
                    output.write(headline.getBytes(StandardCharsets.UTF_8));
                    while (result.next()) {
                        CSVItem item = (CSVItem) result.get(0);
                        if (!isFolderPermissionsAreChecked && !canAdd(item.getFolder(), permittedFolders, checkedFolders)) {
                            continue;
                        }
                        output.write(item.getCsvBytes());
                    }
                }
                output.flush();
            } finally {
                try {
                    output.close();
                } catch (Exception e) {
                }
                if (result != null) {
                    result.close();
                }
                Globals.disconnect(session);
            }
        }
        
        private boolean canAdd(String folder, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
            if (folder == null || !folder.startsWith("/")) {
                return false;
            }
            Boolean result = checkedFolders.get(folder);
            if (result == null) {
                result = SOSAuthFolderPermissions.isPermittedForFolder(folder, permittedFolders);
                checkedFolders.put(folder, result);
            }
            return result;
        }
        
        private ScrollableResults getResult(HistoryFilter filter, Stream<String> columns, List<TaskIdOfOrder> historyIds) {
            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, filter);
            if (filter.hasPermission()) {
                if (filter.getTaskFromHistoryIdAndNode()) {
                    return dbLayer.getCSVJobsFromHistoryIdAndPosition(columns, historyIds.stream().filter(Objects::nonNull).filter(t -> t
                            .getHistoryId() != null).collect(Collectors.groupingBy(TaskIdOfOrder::getHistoryId, Collectors.mapping(
                                    TaskIdOfOrder::getPosition, Collectors.toSet()))));
                } else {
                    return dbLayer.getCSVJobs(columns);
                }
            }
            return null;
        }
        
    }
    
    @Override
    public JOCDefaultResponse orderSteps(String accessToken, String acceptEncoding, byte[] filterBytes) {
        return orderSteps(accessToken, acceptEncoding, filterBytes, IMPL_PATH_ORDER_STEPS);
    }

    private JOCDefaultResponse orderSteps(String accessToken, String acceptEncoding, byte[] filterBytes, String action) {

        try {
            initLogging(action, filterBytes, accessToken);
//            JsonValidator.validateFailFast(filterBytes, OrderSteps.class);
//            OrderSteps in = Globals.objectMapper.readValue(filterBytes, OrderSteps.class);
//
//            String controllerId = in.getControllerId();
//            Set<String> allowedControllers = Collections.emptySet();
//            boolean permitted = false;
//            if (controllerId == null || controllerId.isEmpty()) {
//                controllerId = "";
//                if (Proxies.getControllerDbInstances().isEmpty()) {
//                    permitted = getControllerDefaultPermissions(accessToken).getOrders().getView();
//                } else {
//                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
//                            availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
//                    permitted = !allowedControllers.isEmpty();
//                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
//                        allowedControllers = Collections.emptySet();
//                    }
//                }
//            } else {
//                allowedControllers = Collections.singleton(controllerId);
//                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
//            }
//
//            JOCDefaultResponse response = initPermissions(controllerId, permitted);
//            if (response != null) {
//                return response;
//            }
//            
//            if (Proxies.getControllerDbInstances().isEmpty()) {
//                throw new UnknownJobSchedulerControllerException(InventoryInstancesDBLayer.noRegisteredControllers());
//            }
//            
//            Collection<OrderStepsColumns> columns = in.getColumns();
//            if (in.getColumns() == null || in.getColumns().isEmpty()) {
//                columns = EnumSet.allOf(OrderStepsColumns.class);
//            }
//            
//            Set<Folder> permittedFolders = addPermittedFolder(in.getFolders());
//            HistoryFilter filter = TasksResourceHistoryImpl.getFilter(in, allowedControllers, permittedFolders);
//            filter.setLimit(in.getLimit() == null ? -1 : in.getLimit());
//            
//            boolean withGzipEncoding = acceptEncoding != null && acceptEncoding.contains("gzip");
//            StreamingOutput entityStream = new MyStreamingOutput(withGzipEncoding, filter, columns, in.getHistoryIds(), permittedFolders, action);
//            return JOCDefaultResponse.responseStatus200(entityStream, MediaType.TEXT_PLAIN, getGzipHeaders(withGzipEncoding));
//        } catch (DBMissingDataException e) {
//            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), accessToken, getJocError(), null);
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatus434JSError(e);
            throw new JocNotImplementedException("deprecated");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
//    private Map<String, Object> getGzipHeaders(boolean withGzipEncoding) {
//        Map<String, Object> headers = new HashMap<String, Object>();
//        if (withGzipEncoding) {
//            headers.put("Content-Encoding", "gzip");
//        }
//        headers.put("Transfer-Encoding", "chunked");
//        return headers;
//    }

}
