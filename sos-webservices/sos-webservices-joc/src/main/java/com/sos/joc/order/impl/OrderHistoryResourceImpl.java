package com.sos.joc.order.impl;

import java.util.ArrayList;
import java.util.Collections;
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
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderHistoryItemChildItem;
import com.sos.joc.model.order.OrderHistoryItemChildren;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.order.resource.IOrderHistoryResource;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.ORDER)
public class OrderHistoryResourceImpl extends JOCResourceImpl implements IOrderHistoryResource {

    @Override
    public JOCDefaultResponse postOrderHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, OrderHistoryFilter.class);
            OrderHistoryFilter in = Globals.objectMapper.readValue(inBytes, OrderHistoryFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getControllerPermissions(in.getControllerId(), accessToken)
                    .getOrders().getView());
            if (response != null) {
                return response;
            }
            
            HistoryFilter dbFilter = null;
            if (in.getHistoryId() == null) {
                dbFilter = new HistoryFilter();
                dbFilter.setControllerIds(Collections.singleton(in.getControllerId()));
                dbFilter.setOrderId(in.getOrderId());
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);
            
            if (in.getHistoryId() == null) {
                Long historyId = dbLayer.getHistoryId();
                if (historyId == null) {
                    throw new DBMissingDataException(String.format("Couldn't find order log for '%s'", in.getOrderId()));
                } else {
                    in.setHistoryId(historyId);
                }
            }

            OrderHistoryItemChildren answer = new OrderHistoryItemChildren();
            answer.setHistoryId(in.getHistoryId());
            mapStates(answer, dbLayer.getOrderStates(in.getHistoryId()));
            mapChildren(answer, dbLayer.getOrderSteps(in.getHistoryId()), dbLayer.getOrderForkChilds(in.getHistoryId()));
            answer.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private void mapChildren(OrderHistoryItemChildren answer, List<DBItemHistoryOrderStep> steps, List<DBItemHistoryOrder> forks) {
        List<OrderHistoryItemChildItem> list = new ArrayList<OrderHistoryItemChildItem>();
        if (steps != null && steps.size() > 0) {
            list.addAll(steps.stream().map(step -> {
                OrderHistoryItemChildItem history = new OrderHistoryItemChildItem();
                history.setTask(HistoryMapper.map2TaskHistoryItem(step));
                return history;
            }).collect(Collectors.toList()));
            answer.setHasTasks(true);
        }
        if (forks != null && forks.size() > 0) {
            list.addAll(forks.stream().map(fork -> {
                OrderHistoryItemChildItem history = new OrderHistoryItemChildItem();
                history.setOrder(HistoryMapper.map2OrderHistoryItem(fork));
                return history;
            }).collect(Collectors.toList()));
            answer.setHasOrders(true);
        }
        if (list.size() > 0) {
            answer.setChildren(list.stream().sorted((item1, item2) -> {
                // TODO additional sort by task retryCounter ???
                int position1 = (item1.getTask() == null ? item1.getOrder().getSequence() : item1.getTask().getSequence());
                int position2 = (item2.getTask() == null ? item2.getOrder().getSequence() : item2.getTask().getSequence());
                return Integer.compare(position1, position2);
            }).collect(Collectors.toList()));
        }
    }

    private void mapStates(OrderHistoryItemChildren answer, List<DBItemHistoryOrderState> states) {
        if (states != null) {
            switch (states.size()) {
            case 0:
                break;
            case 1:
                if (OrderStateText.FAILED.intValue() == states.get(0).getState()) {
                    break;
                }
            default:
                answer.setStates(states.stream().map(state -> HistoryMapper.map2OrderHistoryStateItem(state)).collect(Collectors.toList()));
                break;
            }
        }
    }

}
