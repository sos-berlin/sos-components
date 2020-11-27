package com.sos.joc.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistoryChildItem;
import com.sos.joc.model.order.OrderHistoryChilds;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.order.resource.IOrderHistoryResource;
import com.sos.schema.JsonValidator;

/** currently not active */
@Path(WebservicePaths.ORDER)
public class OrderHistoryResourceImpl extends JOCResourceImpl implements IOrderHistoryResource {

    @Override
    public JOCDefaultResponse postOrderHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, OrderHistoryFilter.class);
            OrderHistoryFilter in = Globals.objectMapper.readValue(inBytes, OrderHistoryFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermissonsJocCockpit(in.getControllerId(), accessToken).getOrder()
                    .getView().isStatus());
            if (response != null) {
                return response;
            }
            checkRequiredParameter("historyId", in.getHistoryId());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session);

            OrderHistoryChilds answer = new OrderHistoryChilds();
            mapStates(answer, dbLayer.getOrderStates(in.getHistoryId()));
            mapChildren(answer, dbLayer.getOrderSteps(in.getHistoryId()), dbLayer.getOrderForkChilds(in.getHistoryId()));
            answer.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(answer);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private void mapChildren(OrderHistoryChilds answer, List<DBItemHistoryOrderStep> steps, List<DBItemHistoryOrder> forks) {
        List<OrderHistoryChildItem> list = new ArrayList<OrderHistoryChildItem>();
        if (steps != null && steps.size() > 0) {
            list.addAll(steps.stream().map(step -> {
                OrderHistoryChildItem history = new OrderHistoryChildItem();
                history.setTask(HistoryMapper.map2TaskHistoryItem(step));
                return history;
            }).collect(Collectors.toList()));
            answer.setHasTasks(true);
        }
        if (forks != null && forks.size() > 0) {
            list.addAll(forks.stream().map(fork -> {
                OrderHistoryChildItem history = new OrderHistoryChildItem();
                history.setOrder(HistoryMapper.map2OrderHistoryItem(fork));
                return history;
            }).collect(Collectors.toList()));
            answer.setHasOrders(true);
        }
        if (list.size() > 0) {
            answer.setChildren(list.stream().sorted((item1, item2) -> {
                int position1 = geLastPosition(item1.getTask() == null ? item1.getOrder().getPosition() : item1.getTask().getPosition(), 0);
                int position2 = geLastPosition(item2.getTask() == null ? item2.getOrder().getPosition() : item2.getTask().getPosition(), 1);
                return Integer.compare(position1, position2);
            }).collect(Collectors.toList()));
        }
    }

    // TODO use history methods
    private int geLastPosition(String position, int defaultPosition) {
        try {
            return Integer.parseInt(position);
        } catch (Throwable e) {
            try {
                String[] arr = position.split("/");
                return Integer.parseInt(arr[arr.length - 1]);
            } catch (Throwable t) {
            }
        }
        return defaultPosition;
    }

    private void mapStates(OrderHistoryChilds answer, List<DBItemHistoryOrderState> states) {
        if (states != null) {
            answer.setStates(states.stream().map(state -> {
                return HistoryMapper.map2OrderHistoryStateItem(state);
            }).collect(Collectors.toList()));
        }
    }

}
