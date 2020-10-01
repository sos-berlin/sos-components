package com.sos.joc.classes;

import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConflictException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JobSchedulerServiceUnavailableException;
import com.sos.joc.exceptions.JocException;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class ProblemHelper {

    private static final String UNKNOWN_ORDER = "UnknownOrder";

    public static JocException getExceptionOfProblem(Problem problem) throws JocException {
        switch (problem.httpStatusCode()) {
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            return new JobSchedulerConflictException(getErrorMessage(problem));
        case 503:
            return new JobSchedulerServiceUnavailableException(getErrorMessage(problem));
        default:
            //UnknownKey
            if (problem.codeOrNull() != null && UNKNOWN_ORDER.equalsIgnoreCase(problem.codeOrNull().string())) {
                return new JobSchedulerObjectNotExistException(problem.message());
            }
            return new JobSchedulerBadRequestException(getErrorMessage(problem));
        }
    }

    public static String getErrorMessage(Problem problem) {
        return String.format("%s%s", (problem.codeOrNull() != null) ? problem.codeOrNull().string() + ": " : "", problem.message());
    }

    public static void throwProblemIfExist(Either<Problem, ?> either) throws JocException {
        if (either.isLeft()) {
            throw getExceptionOfProblem(either.getLeft());
        }
    }
}
