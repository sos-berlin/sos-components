package com.sos.jitl.jobs.orderstatustransition.classes;

import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class OrderStateWebserviceExecuter {

    private ApiExecutor apiExecutor;
    private OrderProcessStepLogger logger;

    public OrderStateWebserviceExecuter(OrderProcessStepLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public OrdersV getOrders(OrdersFilterV ordersFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(ordersFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/orders", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }

        }
        logger.debug(body);
        logger.debug("answer=" + answer);

        OrdersV orderHistory = new OrdersV();
        orderHistory = Globals.objectMapper.readValue(answer, OrdersV.class);
        if (orderHistory.getOrders().size() == 0) {
            return null;
        }

        return orderHistory;

    }

    public void cancelOrder(ModifyOrders modifyOrders, String accessToken) throws Exception {
        if (modifyOrders.getOrderIds().size() > 0) {
            String body = Globals.objectMapper.writeValueAsString(modifyOrders);
            ApiResponse apiResponse = apiExecutor.post(accessToken, "/orders/cancel", body);
            String answer = null;
            if (apiResponse.getStatusCode() == 200) {
                answer = apiResponse.getResponseBody();
            } else {
                if (apiResponse.getException() != null) {
                    throw apiResponse.getException();
                } else {
                    throw new Exception(apiResponse.getResponseBody());
                }

            }
            logger.debug(body);
            logger.debug("answer=" + answer);
        } else {
            logger.info("Nothing to do. No orders found");
        }
    }

    public void resumeOrder(ModifyOrders modifyOrders, String accessToken) throws Exception {
        if (modifyOrders.getOrderIds().size() > 0) {
            modifyOrders.setPosition(0);
            String body = Globals.objectMapper.writeValueAsString(modifyOrders);
            ApiResponse apiResponse = apiExecutor.post(accessToken, "/orders/resume", body);
            String answer = null;
            if (apiResponse.getStatusCode() == 200) {
                answer = apiResponse.getResponseBody();
            } else {
                if (apiResponse.getException() != null) {
                    throw apiResponse.getException();
                } else {
                    throw new Exception(apiResponse.getResponseBody());
                }

            }
            logger.debug(body);
            logger.debug("answer=" + answer);
        } else {
            logger.info("Nothing to do. No orders found");
        }

    }
}
