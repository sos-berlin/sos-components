package com.sos.joc.order.impl;

import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogOrderContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderRunningLogFilter;
import com.sos.joc.order.resource.IOrderLogResource;
import com.sos.schema.JsonValidator;

@Path("order")
public class OrderLogResourceImpl extends JOCResourceImpl implements IOrderLogResource {

    private static final String API_CALL = "./order/log";
    private static final String API_CALL_DOWNLOAD = "./order/log/download";
    private static final String API_CALL_RUNNING = "./order/log/running";

    @Override
    public JOCDefaultResponse postOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
            OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getPermissonsJocCockpit(orderHistoryFilter
                    .getControllerId(), accessToken).getOrder().getView().isOrderLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter.getHistoryId());
            return JOCDefaultResponse.responseStatus200(logOrderContent.getOrderLog());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse downloadOrderLog(String accessToken, String queryAccessToken, String jobschedulerId, Long historyId) {
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (jobschedulerId != null) {
            builder.add("jobschedulerId", jobschedulerId);
        }
        if (historyId != null) {
            builder.add("historyId", historyId);
        }
        return downloadOrderLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JOCDefaultResponse downloadOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_DOWNLOAD, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
            OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getPermissonsJocCockpit(orderHistoryFilter
                    .getControllerId(), accessToken).getOrder().getView().isOrderLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter.getHistoryId());
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(logOrderContent.getStreamOutput(), logOrderContent.getDownloadFilename(),
                    logOrderContent.getUnCompressedLength());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postRollingOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RUNNING, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderRunningLogFilter.class);
            OrderRunningLogFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderRunningLogFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getPermissonsJocCockpit(orderHistoryFilter
                    .getControllerId(), accessToken).getOrder().getView().isOrderLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter);
            return JOCDefaultResponse.responseStatus200(logOrderContent.getOrderLog());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
