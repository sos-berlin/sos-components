package com.sos.joc.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.ControllerConflictException;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.ControllerServiceUnavailableException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class ProblemHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemHelper.class);
    private static final String UNKNOWN_KEY = "UnknownKey";

    public static String getErrorMessage(Problem problem) {
        if (problem == null) {
            return null;
        }
        return String.format("%s%s", (problem.codeOrNull() != null) ? problem.codeOrNull().string() + ": " : "", problem.message());
    }

    public static void throwProblemIfExist(Either<Problem, ?> either) throws JocException {
        if (either == null) {
            throw new JocBadRequestException("Unknown problem");
        } else if (either.isLeft()) {
            throw getExceptionOfProblem(either.getLeft());
        }
    }

    public static void postProblemEventIfExist(Either<Problem, ?> either, String accessToken, JocError err, String controller)
            throws JocException {
        postEventIfExist(either, accessToken, err, controller, false);
    }

    public static void postProblemEventAsHintIfExist(Either<Problem, ?> either, String accessToken, JocError err, String controller)
            throws JocException {
        postEventIfExist(either, accessToken, err, controller, true);
    }

    public static void postExceptionEventIfExist(Either<Exception, ?> either, String accessToken, JocError err, String controller)
            throws JocException {
        postExceptionEventIfExist(either, accessToken, err, controller, false);
    }

    public static void postExceptionEventAsHintIfExist(Either<Exception, ?> either, String accessToken, JocError err,
            String controller) throws JocException {
        postExceptionEventIfExist(either, accessToken, err, controller, true);
    }
    
    public static void postMessageAsHintIfExist(String message, String accessToken, JocError err, String controller) throws JocException {
        postMessageIfExist(message, accessToken, err, controller, true);
    }

    private static synchronized void postEventIfExist(Either<Problem, ?> either, String accessToken, JocError err, String controller,
            boolean isOnlyHint) throws JocException {
        if (either == null || either.isLeft()) {
            if (err != null && !err.getMetaInfo().isEmpty()) {
                LOGGER.info(err.printMetaInfo());
                err.clearMetaInfo();
            }
            if (either == null) {
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, "BadRequestError: Unknown problem", isOnlyHint));
                }
            } else {
                Marker marker = null;
                if (err != null && err.getApiCall() != null) {
                    marker = MarkerFactory.getMarker(err.getApiCall());
                }
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(getEventOfProblem(marker, either.getLeft(), accessToken, controller, isOnlyHint));
                } else {
                    getEventOfProblem(marker, either.getLeft(), accessToken, controller, isOnlyHint);
                }
            }
        }
    }

    private static synchronized void postExceptionEventIfExist(Either<Exception, ?> either, String accessToken, JocError err,
            String controller, boolean isOnlyHint) throws JocException {
        if (either == null || either.isLeft()) {
            if (err != null && !err.getMetaInfo().isEmpty()) {
                LOGGER.info(err.printMetaInfo());
                err.clearMetaInfo();
            }
            if (either == null) {
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, "BadRequestError: Unknown problem", isOnlyHint));
                }
            } else {
                if (err != null && err.getApiCall() != null) {
                    if (isOnlyHint) {
                        LOGGER.warn(MarkerFactory.getMarker(err.getApiCall()), "", either.getLeft());
                    } else {
                        LOGGER.error(MarkerFactory.getMarker(err.getApiCall()), "", either.getLeft());
                    }
                } else {
                    if (isOnlyHint) {
                        LOGGER.warn("", either.getLeft());
                    } else {
                        LOGGER.error("", either.getLeft());
                    }
                }
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, either.getLeft().toString(), isOnlyHint));
                }
            }
        }
    }
    
    private static synchronized void postMessageIfExist(String message, String accessToken, JocError err, String controller, boolean isOnlyHint) {
        if (message != null && !message.isEmpty()) {
            if (accessToken != null && !accessToken.isEmpty()) {
                EventBus.getInstance().post(new ProblemEvent(accessToken, controller, message, isOnlyHint));
            }
        }
    }

    public static JocException getExceptionOfProblem(Problem problem) throws JocException {
        switch (problem.httpStatusCode()) {
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            return new ControllerConflictException(getErrorMessage(problem));
        case 503:
            return new ControllerServiceUnavailableException(getErrorMessage(problem));
        default:
            // UnknownKey
            if (problem.codeOrNull() != null && UNKNOWN_KEY.equalsIgnoreCase(problem.codeOrNull().string())) {
                return new ControllerObjectNotExistException(problem.message());
            }
            return new JocBadRequestException(getErrorMessage(problem));
        }
    }

    private static ProblemEvent getEventOfProblem(Marker marker, Problem problem, String accessToken, String controller, boolean isOnlyHint)
            throws JocException {
        if (marker == null) {
            marker = MarkerFactory.getMarker("ProblemEvent");
        }
        // TODO stacktrace logging
        switch (problem.httpStatusCode()) {
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            if (isOnlyHint) {
                LOGGER.warn(marker, "ConflictWarning: " + getErrorMessage(problem));
            } else {
                LOGGER.error(marker, "ConflictError: " + getErrorMessage(problem));
            }
            return new ProblemEvent(accessToken, controller, "ConflictError: " + getErrorMessage(problem), isOnlyHint);
        case 503:
            if (isOnlyHint) {
                LOGGER.warn(marker, "ServiceUnavailableWarning: " + getErrorMessage(problem));
            } else {
                LOGGER.error(marker, "ServiceUnavailableError: " + getErrorMessage(problem));
            }
            return new ProblemEvent(accessToken, controller, "ServiceUnavailableError: " + getErrorMessage(problem), isOnlyHint);
        default:
            // UnknownKey
            if (problem.codeOrNull() != null && UNKNOWN_KEY.equalsIgnoreCase(problem.codeOrNull().string())) {
                if (isOnlyHint) {
                    LOGGER.warn(marker, "ObjectNotExistWarning: " + getErrorMessage(problem));
                } else {
                    LOGGER.error(marker, "ObjectNotExistError: " + getErrorMessage(problem));
                }
                return new ProblemEvent(accessToken, controller, "ObjectNotExistError: " + getErrorMessage(problem), isOnlyHint);
            }
            if (isOnlyHint) {
                LOGGER.warn(marker, getErrorMessage(problem));
            } else {
                LOGGER.error(marker, "BadRequestError: " + getErrorMessage(problem));
            }
            return new ProblemEvent(accessToken, controller, "BadRequestError: " + getErrorMessage(problem), isOnlyHint);
        }
    }
}
