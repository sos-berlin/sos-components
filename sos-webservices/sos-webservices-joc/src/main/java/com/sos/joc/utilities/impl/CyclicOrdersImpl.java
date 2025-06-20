package com.sos.joc.utilities.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.order.OrderIds;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.utilities.resource.ICyclicOrdersResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.UTILITIES)
public class CyclicOrdersImpl extends JOCOrderResourceImpl implements ICyclicOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicOrdersImpl.class);

    @Override
    public JOCDefaultResponse postCyclicOrders(String accessToken, byte[] filterBytes) {
        LOGGER.debug("reading list of cyclic orders");
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV in = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);
            String controllerId = in.getControllerId();
            
            JOCDefaultResponse response = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (response != null) {
                return response;
            }

            setSettings(IMPL_PATH);
            OrderIds answer = new OrderIds();
            Set<String> mainParts = new HashSet<>();
            for (String orderId : in.getOrderIds()) {
                String mainPart = OrdersHelper.getCyclicOrderIdMainPart(orderId);
                if (!mainParts.contains(mainPart)) {
                    mainParts.add(mainPart);
                    
                    answer.getOrderIds().add(orderId);
                    addCyclicOrderIds(answer.getOrderIds(), orderId, controllerId);

                    // TODO add when not found or single order?
                    // DBItemDailyPlanOrder item = addCyclicOrderIds(answer.getOrderIds(), orderId, in.getControllerId());
                    // if (item != null && item.getStartMode().equals(new Integer(1))) {
                    // answer.getOrderIds().add(orderId);
                    // }
                }
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
