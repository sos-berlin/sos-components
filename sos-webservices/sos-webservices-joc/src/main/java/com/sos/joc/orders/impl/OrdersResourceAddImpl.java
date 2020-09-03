package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.audit.AddOrderAudit;
import com.sos.joc.classes.common.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.StartOrder;
import com.sos.joc.model.order.StartOrders;
import com.sos.joc.orders.resource.IOrdersResourceAdd;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.order.JFreshOrder;
import reactor.core.publisher.Flux;

@Path("orders")
public class OrdersResourceAddImpl extends JOCResourceImpl implements IOrdersResourceAdd {

    private static final String API_CALL = "./orders/add";
    private static final DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public JOCDefaultResponse postOrdersAdd(String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, StartOrders.class);
            StartOrders startOrders = Globals.objectMapper.readValue(filterBytes, StartOrders.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, startOrders, accessToken, startOrders.getJobschedulerId(),
                    getPermissonsJocCockpit(startOrders.getJobschedulerId(), accessToken).getOrder().getExecute().isStart());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(startOrders.getAuditLog());
            if (startOrders.getOrders().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'orders'");
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Predicate<StartOrder> permissions = order -> canAdd(order.getWorkflowPath(), permittedFolders);

            // TODO Further predicate to check if workflow exists?
            
            // To check whether orderId already exists
            // final Set<OrderId> orderIds = Proxy.of(startOrders.getJobschedulerId()).currentState().orderIds();
            final String yyyymmdd = formatter.format(Instant.now());

            Function<StartOrder, Either<Err419, JFreshOrder>> mapper = order -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    CheckJavaVariableName.test("orderId", order.getOrderId());
                    AddOrderAudit orderAudit = new AddOrderAudit(order, startOrders);
                    logAuditMessage(orderAudit);
                    either = Either.right(mapToFreshOrder(order, yyyymmdd));
                    storeAuditLogEntry(orderAudit);
                } catch (Exception ex) {
                    either = Either.left(new BulkError().get(ex, getJocError(), order));
                }
                return either;
            };

            Map<Boolean, Set<Either<Err419, JFreshOrder>>> result = startOrders.getOrders().stream().filter(permissions).map(mapper).collect(
                    Collectors.groupingBy(Either::isRight, Collectors.toSet()));

            if (result.containsKey(true) && !result.get(true).isEmpty()) {
                // Proxy.start(startOrders.getJobschedulerId()).getProxyFuture().thenApplyAsync(p -> p.api().addOrders(Flux.fromStream(result.get(true)
                // .stream().map(Either::get))));
                try {
                    Either<Problem, Void> response = Proxy.of(startOrders.getJobschedulerId()).api().addOrders(Flux.fromStream(result.get(true)
                            .stream().map(Either::get))).get(Globals.httpSocketTimeout, TimeUnit.MILLISECONDS);
                    ProblemHelper.throwProblemIfExist(response);
                } catch (TimeoutException e) {
                    throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds", startOrders
                            .getJobschedulerId(), (Globals.httpSocketTimeout) / 1000));
                }
            }

            if (result.containsKey(false) && !result.get(false).isEmpty()) {
                return JOCDefaultResponse.responseStatus419(result.get(false).stream().map(Either::getLeft).collect(Collectors.toList()));
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static JFreshOrder mapToFreshOrder(StartOrder order, String yyyymmdd) {
        //TODO uniqueId comes from dailyplan, here a fake
        String uniqueId = Long.valueOf(Instant.now().toEpochMilli()).toString().substring(4);
        OrderId orderId = OrderId.of(String.format("%s#T%s-%s", yyyymmdd, uniqueId, order.getOrderId()));
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order.getTimeZone());
        Map<String, String> arguments = Collections.emptyMap();
        if (order.getArguments() != null) {
            arguments = order.getArguments().getAdditionalProperties();
        }
        return JFreshOrder.of(orderId, WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments);
    }

}