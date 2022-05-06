package com.sos.jitl.jobs.checkhistory.classes;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.JobLogger;

public class ParameterResolver {

    private JobLogger logger;
    private static final int EQ = 1;
    private static final int GE = 2;
    private static final int GT = 3;
    private static final int LE = 4;
    private static final int LT = 5;

    private boolean paramStartedFrom = false;
    private boolean paramStartedTo = false;
    private boolean paramCompletedFrom = false;
    private boolean paramCompletedTo = false;
    private boolean paramCount = false;
    private String startedFrom = "0d";
    private String startedTo = "0d";
    private String completedFrom = "0d";
    private String completedTo = "0d";
    protected int countCommand = 0;
    protected int count = 0;

    public ParameterResolver(JobLogger logger) {
        super();
        this.logger = logger;
    }

    public void resolveParameter(String parameter) throws SOSException {
        String[] parameters = parameter.split(",");
        String startedFrom = "0d";
        String startedTo = "0d";
        String completedFrom = "0d";
        String completedTo = "0d";

        if (parameters.length > 0) {
            for (String parameterAssignment : parameters) {
                String[] p = parameterAssignment.split("=");
                String pName = p[0].trim();
                if (pName.startsWith("count")) {
                    pName = "count";
                }
                String pValue = "";
                if (p.length > 1) {
                    pValue = p[1].trim();
                }
                switch (pName.toLowerCase()) {
                case "startedfrom":
                    paramStartedFrom = true;
                    startedFrom = pValue;
                    Globals.debug(logger, "startedFrom=" + startedFrom);
                    break;
                case "startedto":
                    paramStartedTo = true;
                    startedTo = pValue;
                    Globals.debug(logger, "startedTo=" + startedTo);
                    break;
                case "completedfrom":
                    paramCompletedFrom = true;
                    completedFrom = pValue;
                    Globals.debug(logger, "completedFrom=" + completedFrom);
                    break;
                case "completedto":
                    paramCompletedTo = true;
                    completedTo = pValue;
                    Globals.debug(logger, "completedto=" + completedTo);
                    break;
                case "count":
                    try {
                        String[] pEq = parameterAssignment.split("=");
                        String[] pLe = parameterAssignment.split("<=");
                        String[] pLt = parameterAssignment.split("<");
                        String[] pGe = parameterAssignment.split(">=");
                        String[] pGt = parameterAssignment.split(">");
                        if (pEq.length > 1) {
                            count = Integer.valueOf(pEq[1]);
                            countCommand = EQ;
                        }
                        if (pLe.length > 1) {
                            count = Integer.valueOf(pLe[1]);
                            countCommand = LE;
                        }
                        if (pLt.length > 1) {
                            count = Integer.valueOf(pLt[1]);
                            countCommand = LT;
                        }
                        if (pGe.length > 1) {
                            count = Integer.valueOf(pGe[1]);
                            countCommand = GE;
                        }
                        if (pGt.length > 1) {
                            count = Integer.valueOf(pGt[1]);
                            countCommand = GT;
                        }
                        paramCount = true;

                    } catch (NumberFormatException e) {
                        Globals.log(logger, "Not a valid number:" + pValue);
                        count = 0;
                    }
                    Globals.debug(logger, "completedto=" + completedTo);
                    break;
                default:
                    if (!pName.isEmpty()) {
                        throw new SOSException("unknown parameter name: " + pName);
                    }
                }
            }
        }

    }

    public boolean isParamStartedFrom() {
        return paramStartedFrom;
    }

    public boolean isParamStartedTo() {
        return paramStartedTo;
    }

    public boolean isParamCompletedFrom() {
        return paramCompletedFrom;
    }

    public boolean isParamCompletedTo() {
        return paramCompletedTo;
    }

    public boolean isParamCount() {
        return paramCount;
    }

    public int getCountCommand() {
        return countCommand;
    }

    public int getCount() {
        return count;
    }

    public String getStartedFrom() {
        return startedFrom;
    }

    public String getStartedTo() {
        return startedTo;
    }

    public String getCompletedFrom() {
        return completedFrom;
    }

    public String getCompletedTo() {
        return completedTo;
    }

    public boolean getCountResult(int countHistory, boolean historyResult) throws SOSException {

        boolean result = historyResult;
        if (isParamCount() && historyResult) {
            switch (getCountCommand()) {
            case EQ:
                result = countHistory == count;
                break;
            case GE:
                result = countHistory >= count;
                break;
            case GT:
                result = countHistory > count;
                break;
            case LE:
                result = countHistory <= count;
                break;
            case LT:
                result = countHistory < count;
                break;
            default:
                throw new SOSException("unknown operator in count parameter");
            }
        }
        return result;
    }
}
