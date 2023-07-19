package com.sos.jitl.jobs.orderstatustransition.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.jitl.jobs.orderstatustransition.OrderStateTransitionJobArguments;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.js7.job.OrderProcessStepLogger;

public class OrderStateTransition {

    private static final String RESUMED = "RESUMED";
    private static final String CANCELLED = "CANCELLED";
    private OrderProcessStepLogger logger;
    private OrderStateTransitionJobArguments args;

    public OrderStateTransition(OrderProcessStepLogger logger, OrderStateTransitionJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    private OrdersFilterV newOrdersFilterV() {
        OrdersFilterV ordersFilter = new OrdersFilterV();
        ordersFilter.setCompact(true);
        ordersFilter.setControllerId(args.getControllerId());
        ordersFilter.getStates().add(OrderStateText.fromValue(args.getStateTransitionSource()));
        if (args.getPersistDuration() != null && OrderStateText.fromValue(args.getStateTransitionSource()).equals(OrderStateText.FAILED)) {
            ordersFilter.setStateDateFrom(args.getPersistDuration());
        }
        return ordersFilter;
    }

    public void execute() throws Exception {
        ApiExecutor apiExecutor = new ApiExecutor(logger);
        String accessToken = null;
        List<OrderV> listOfOrders = new ArrayList<OrderV>();
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();

            OrderStateWebserviceExecuter orderStateWebserviceExecuter = new OrderStateWebserviceExecuter(logger, apiExecutor);

            if (args.getWorkflowSearchPattern() != null && args.getWorkflowSearchPattern().size() > 0) {
                for (String workflowPattern : args.getWorkflowSearchPattern()) {
                    OrdersFilterV ordersFilter = newOrdersFilterV();
                    ordersFilter.setRegex(convertGlobToRegex(workflowPattern));
                    OrdersV list = orderStateWebserviceExecuter.getOrders(ordersFilter, accessToken);
                    if (list != null) {
                        listOfOrders.addAll(list.getOrders());
                    }
                }
            }

            if (args.getOrderSearchPatterns() != null && args.getOrderSearchPatterns().size() > 0) {
                for (String orderPattern : args.getOrderSearchPatterns()) {
                    if (!orderPattern.startsWith("/")) {
                        orderPattern = "/" + orderPattern;
                    }
                    String[] p = orderPattern.split("/");
                    String orderName = p[p.length - 1];
                    orderName = "#*#*-" + orderName;
                    p[p.length - 1] = orderName;
                    orderPattern = convertStringArrayToString(p, "/");
                    OrdersFilterV ordersFilter = newOrdersFilterV();
                    ordersFilter.setRegex(convertGlobToRegex(orderPattern));
                    OrdersV list = orderStateWebserviceExecuter.getOrders(ordersFilter, accessToken);
                    if (list != null) {
                        listOfOrders.addAll(list.getOrders());
                    }
                }
            }

            OrdersFilterV ordersFilter = newOrdersFilterV();

            for (String folderName : args.getWorkflowFolders()) {
                Folder f = new Folder();
                boolean recursive = false;
                if (folderName.endsWith("/*")) {
                    folderName = folderName.substring(0, folderName.length() - 2);
                    if (folderName.isEmpty()) {
                        folderName = "/";
                    }
                    recursive = true;
                }
                f.setFolder(folderName);
                f.setRecursive(recursive);
                ordersFilter.getFolders().add(f);
            }
            OrdersV list = orderStateWebserviceExecuter.getOrders(ordersFilter, accessToken);
            if (list != null) {
                listOfOrders.addAll(list.getOrders());
            }

            Map<String, OrderV> mapOfOrders = new HashMap<String, OrderV>();

            for (OrderV order : listOfOrders) {
                mapOfOrders.put(order.getOrderId(), order);
            }

            logger.debug(mapOfOrders.size() + " orders found");
            ModifyOrders modifyOrders = new ModifyOrders();
            modifyOrders.setControllerId(args.getControllerId());

            for (OrderV order : mapOfOrders.values()) {
                logger.debug(order.getOrderId());
                modifyOrders.getOrderIds().add(order.getOrderId());
            }

            switch (args.getStateTransitionTarget()) {
            case CANCELLED:
                orderStateWebserviceExecuter.cancelOrder(modifyOrders, accessToken);
                break;
            case RESUMED:
                orderStateWebserviceExecuter.resumeOrder(modifyOrders, accessToken);
                break;
            default:
                break;
            }

        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }

    private static String convertStringArrayToString(String[] strArr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr)
            sb.append(str).append(delimiter);
        return sb.substring(0, sb.length() - 1);
    }

    private static final String convertGlobToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
            case '\\':
                if (++i >= arr.length) {
                    sb.append('\\');
                } else {
                    char next = arr[i];
                    switch (next) {
                    case ',':
                        // escape not needed
                        break;
                    case 'Q':
                    case 'E':
                        // extra escape needed
                        sb.append('\\');
                    default:
                        sb.append('\\');
                    }
                    sb.append(next);
                }
                break;
            case '*':
                if (inClass == 0)
                    sb.append(".*");
                else
                    sb.append('*');
                break;
            case '?':
                if (inClass == 0)
                    sb.append('.');
                else
                    sb.append('?');
                break;
            case '[':
                inClass++;
                firstIndexInClass = i + 1;
                sb.append('[');
                break;
            case ']':
                inClass--;
                sb.append(']');
                break;
            case '.':
            case '(':
            case ')':
            case '+':
            case '|':
            case '^':
            case '$':
            case '@':
            case '%':
                if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
                    sb.append('\\');
                sb.append(ch);
                break;
            case '!':
                if (firstIndexInClass == i)
                    sb.append('^');
                else
                    sb.append('!');
                break;
            case '{':
                inGroup++;
                sb.append('(');
                break;
            case '}':
                inGroup--;
                sb.append(')');
                break;
            case ',':
                if (inGroup > 0)
                    sb.append('|');
                else
                    sb.append(',');
                break;
            default:
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
