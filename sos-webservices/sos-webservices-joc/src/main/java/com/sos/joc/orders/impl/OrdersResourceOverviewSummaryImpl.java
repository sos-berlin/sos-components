package com.sos.joc.orders.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersHistoricSummary;
import com.sos.joc.model.order.OrdersOverView;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSummary;

@Path("orders")
public class OrdersResourceOverviewSummaryImpl extends JOCResourceImpl implements IOrdersResourceOverviewSummary {

    private static final String API_CALL = "./orders/overview/summary";

    @Override
    public JOCDefaultResponse postOrdersOverviewSummary(String xAccessToken, String accessToken, OrdersFilter ordersFilter) throws Exception {
        return postOrdersOverviewSummary(getAccessToken(xAccessToken, accessToken), ordersFilter);
    }

    public JOCDefaultResponse postOrdersOverviewSummary(String accessToken, OrdersFilter ordersFilter) throws Exception {

		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken,
					ordersFilter.getJobschedulerId(),
					getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getOrder().getView()
							.isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrdersOverView entity = new OrdersOverView();
            entity.setDeliveryDate(new Date());
            entity.setSurveyDate(new Date());
            OrdersHistoricSummary ordersHistoricSummary = new OrdersHistoricSummary();
            ordersHistoricSummary.setFailed(0);
            ordersHistoricSummary.setSuccessful(0);
            entity.setOrders(ordersHistoricSummary);
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
