package com.sos.joc.orders.impl;

import java.time.Instant;
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
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConflictException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JobSchedulerServiceUnavailableException;
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

    private static final String API_CALL_START = "./orders/start";

    @Override
    public JOCDefaultResponse postOrdersAdd(String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, StartOrders.class);
            StartOrders startOrders = Globals.objectMapper.readValue(filterBytes, StartOrders.class);
            
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_START, startOrders, accessToken, startOrders.getJobschedulerId(),
                    getPermissonsJocCockpit(startOrders.getJobschedulerId(), accessToken).getOrder().getExecute().isStart());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkRequiredComment(startOrders.getAuditLog());
            if (startOrders.getOrders().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'orders'");
            }
            
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            // TODO is 5 seconds a good value?
            final long timeout = 5L;
            
            Predicate<StartOrder> permissions = o -> canAdd(o.getWorkflowPath(), permittedFolders);
            
            Function<StartOrder, Either<Err419, JFreshOrder>> mapper = o -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(o.getScheduledFor(), o.getTimeZone());
                    either = Either.right(JFreshOrder.of(OrderId.of(o.getOrderId()), WorkflowPath.of(o.getWorkflowPath()), scheduledFor, o
                            .getArguments().getAdditionalProperties()));
                } catch (Exception ex) {
                    either = Either.left(new BulkError().get(ex, getJocError(), o));
                }
                return either;
            };
            
            Map<Boolean, Set<Either<Err419, JFreshOrder>>> result = startOrders.getOrders().stream().filter(permissions).map(mapper).collect(
                    Collectors.groupingBy(Either::isRight, Collectors.toSet()));

            if (result.containsKey(true) && !result.get(true).isEmpty()) {
                // Proxy.start(startOrders.getJobschedulerId()).getProxyFuture().thenApply(p -> p.api().addOrders(Flux.fromStream(result.get(true)
                // .stream().map(Either::get)))); //TODO consider response
                try {
                    Either<Problem, Void> response = Proxy.of(startOrders.getJobschedulerId()).api().addOrders(Flux.fromStream(result.get(true)
                            .stream().map(Either::get))).get(timeout, TimeUnit.SECONDS);
                    if (response.isLeft()) {
                        checkResponse(response.getLeft());
                    }
                } catch (TimeoutException e) {
                    throw new JobSchedulerNoResponseException(String.format("no response from controller '%s' since %ds", startOrders
                            .getJobschedulerId(), timeout));
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
    
    private static void checkResponse(Problem problem) throws JocException {
        switch (problem.httpStatusCode()) {
        case 200:
        case 201:
            break;
        case 409:
            throw new JobSchedulerConflictException(getErrorMessage(problem));
        case 503:
            throw new JobSchedulerServiceUnavailableException(getErrorMessage(problem));
        default:
            throw new JobSchedulerBadRequestException(getErrorMessage(problem));
        }
    }
    
    private static String getErrorMessage(Problem problem) {
        return String.format("http %d: %s%s", problem.httpStatusCode(), (problem.codeOrNull() != null) ? problem.codeOrNull() + ": " : "", problem
                .message());
    }
}