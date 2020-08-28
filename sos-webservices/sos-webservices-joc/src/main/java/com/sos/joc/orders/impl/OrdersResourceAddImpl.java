package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersP;
import com.sos.joc.orders.resource.IOrdersResourceP;

@Path("orders")
public class OrdersResourcePImpl extends JOCResourceImpl implements IOrdersResourceP {

    private static final String API_CALL = "./orders/p";

    @Override
    public JOCDefaultResponse postOrdersP(String xAccessToken, String accessToken, OrdersFilter ordersFilter) throws Exception {
        return postOrdersP(getAccessToken(xAccessToken, accessToken), ordersFilter);
    }

    public JOCDefaultResponse postOrdersP(String accessToken, OrdersFilter ordersFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken, ordersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            OrdersP entity = new OrdersP();
            //entity.setOrders(null);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}