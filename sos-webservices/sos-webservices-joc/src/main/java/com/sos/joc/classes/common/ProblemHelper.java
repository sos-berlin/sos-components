package com.sos.joc.classes.common;

import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConflictException;
import com.sos.joc.exceptions.JobSchedulerServiceUnavailableException;
import com.sos.joc.exceptions.JocException;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class ProblemHelper {

    public static void checkResponse(Problem problem) throws JocException {
        switch (problem.httpStatusCode()) {
        case 200:
        case 201:
            break;
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            throw new JobSchedulerConflictException(getErrorMessage(problem));
        case 503:
            throw new JobSchedulerServiceUnavailableException(getErrorMessage(problem));
        default:
            throw new JobSchedulerBadRequestException(getErrorMessage(problem));
        }
    }

    public static String getErrorMessage(Problem problem) {
        return String.format("%s%s", (problem.codeOrNull() != null) ? problem.codeOrNull() + ": " : "", problem
                .message());
    }
    
    public static void throwProblemIfExist(Either<Problem, ?> either) throws JocException {
        if (either.isLeft()) {
            checkResponse(either.getLeft());
        }
    }
}
