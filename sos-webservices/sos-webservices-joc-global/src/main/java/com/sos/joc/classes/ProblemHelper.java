package com.sos.joc.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.ControllerConflictException;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.ControllerServiceUnavailableException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class ProblemHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemHelper.class);
    private static final String UNKNOWN_KEY = "UnknownKey";

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

    public static ProblemEvent getEventOfProblem(Problem problem, String accessToken, String controller) throws JocException {
        // TODO stacktrace logging
        switch (problem.httpStatusCode()) {
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            LOGGER.error("ConflictError: " + getErrorMessage(problem));
            return new ProblemEvent(accessToken, controller, "ConflictError: " + getErrorMessage(problem));
        case 503:
            LOGGER.error("ServiceUnavailableError: " + getErrorMessage(problem));
            return new ProblemEvent(accessToken, controller, "ServiceUnavailableError: " + getErrorMessage(problem));
        default:
            // UnknownKey
            if (problem.codeOrNull() != null && UNKNOWN_KEY.equalsIgnoreCase(problem.codeOrNull().string())) {
                LOGGER.error("ObjectNotExistError: " + getErrorMessage(problem));
                return new ProblemEvent(accessToken, controller, "ObjectNotExistError: " + getErrorMessage(problem));
            }
            LOGGER.error("BadRequestError: " + getErrorMessage(problem));
            return new ProblemEvent(accessToken, controller, "BadRequestError: " + getErrorMessage(problem));
        }
    }

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

    public static void postProblemEventIfExist(Either<Problem, ?> either, String accessToken, JocError err, String controller) throws JocException {
        if (either == null || either.isLeft()) {
            if (err != null && !err.getMetaInfo().isEmpty()) {
                LOGGER.info(err.printMetaInfo());
            }
            if (either == null) {
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, "BadRequestError: Unknown problem"));
                }
            } else {
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(getEventOfProblem(either.getLeft(), accessToken, controller));
                } else {
                    getEventOfProblem(either.getLeft(), accessToken, controller);
                }
            }
        }
    }

    public static void postExceptionEventIfExist(Either<Exception, ?> either, String accessToken, JocError err, String controller)
            throws JocException {
        if (either == null || either.isLeft()) {
            if (err != null && !err.getMetaInfo().isEmpty()) {
                LOGGER.info(err.printMetaInfo());
            }
            if (either == null) {
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, "BadRequestError: Unknown problem"));
                }
            } else {
                LOGGER.error("", either.getLeft());
                if (accessToken != null && !accessToken.isEmpty()) {
                    EventBus.getInstance().post(new ProblemEvent(accessToken, controller, either.getLeft().toString()));
                }
            }
        }
    }
}
