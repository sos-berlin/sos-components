package com.sos.js7.converter.js1.output.js7.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.js1.common.commands.AddOrder;
import com.sos.js7.converter.js1.common.commands.Commands;
import com.sos.js7.converter.js1.common.commands.Order;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeOnReturnCode;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class ReturnCodeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodeHelper.class);

    private static final String JS7_VAR_RETURN_CODE = "$returnCode";

    public static List<Instruction> JS17OnReturnCodes2JS7(String js7WorkflowName, JobHelper h) {
        if (h.getJS1Job().getCommands() == null || h.getJS1Job().getCommands().size() == 0) {
            return null;
        }

        List<Instruction> in = new ArrayList<>();
        for (Commands c : h.getJS1Job().getCommands()) {
            String rc = "unknown";
            try {
                rc = c.getOnExitCode();
                if (SOSString.isEmpty(rc)) {
                    rc = "0";
                }
                if (c.getOrders() != null && c.getOrders().size() > 0) {
                    for (Order co : c.getOrders()) {
                        IfElse ie = new IfElse();
                        ie.setPredicate(getIfElsePredicate(rc));

                        com.sos.inventory.model.instruction.AddOrder ao = new com.sos.inventory.model.instruction.AddOrder();
                        ao.setWorkflowName(getWorkflowName(js7WorkflowName, co));
                        ao.setArguments(getArguments(h, co));

                        List<Instruction> lt = new ArrayList<>();
                        lt.add(ao);

                        ie.setThen(new Instructions(lt));
                        in.add(ie);
                    }
                }
                if (c.getAddOrders() != null && c.getAddOrders().size() > 0) {
                    for (AddOrder cao : c.getAddOrders()) {
                        IfElse ie = new IfElse();
                        ie.setPredicate(getIfElsePredicate(rc));

                        com.sos.inventory.model.instruction.AddOrder ao = new com.sos.inventory.model.instruction.AddOrder();
                        ao.setWorkflowName(getWorkflowName(js7WorkflowName, cao));
                        ao.setArguments(getArguments(h, cao));

                        List<Instruction> lt = new ArrayList<>();
                        lt.add(ao);

                        ie.setThen(new Instructions(lt));
                        in.add(ie);
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[convert return codes][js1 job=%s, on_exit_code=%s]%s", h.getJS1Job().getPath(), rc, e.toString()), e);
                ConverterReport.INSTANCE.addErrorRecord(h.getJS1Job().getPath(), "[convert on_exit_code]on_exit_code=" + rc, e);
            }
        }

        return in;
    }

    public static List<Instruction> JS17OnReturnCodes2JS7(JobChainStateHelper h, JobHelper jh) {
        List<Instruction> l = JS17OnReturnCodes2JS7(h.getJS7WorkflowName(), jh);
        if (jh.getCopyParams() != null) {
            for (Variables v : jh.getCopyParams()) {
                h.addCopyParams(v);
            }
        }
        return l;
    }

    public static List<Instruction> JS17OnReturnCodes2JS7(JobChainStateHelper h) {
        if (!h.hasOnReturnCodes()) {
            return null;
        }
        List<JobChainNodeOnReturnCode> js1 = h.getJS1Node().getOnReturnCodes();
        List<Instruction> in = new ArrayList<>();
        for (JobChainNodeOnReturnCode nrc : js1) {
            String rc = "unknown";
            try {
                rc = nrc.getReturnCode();
                if (SOSString.isEmpty(rc)) {
                    rc = "0";
                }
                if (nrc.getAddOrders() == null || nrc.getAddOrders().size() == 0) {
                    LOGGER.warn(String.format("[convert return codes][js1 JobChain=%s, state=%s, return_code=%s][skip]add_order not found", h
                            .getJS1Node().getJobChainPath(), h.getJS1State(), rc));
                    ConverterReport.INSTANCE.addWarningRecord(h.getJS1Node().getJobChainPath(), "[convert return codes]state=" + h.getJS1State()
                            + ", return_code=" + rc, "[skip]add_order not found");

                    continue;
                }

                for (AddOrder cao : nrc.getAddOrders()) {
                    IfElse ie = new IfElse();
                    ie.setPredicate(getIfElsePredicate(rc));

                    com.sos.inventory.model.instruction.AddOrder ao = new com.sos.inventory.model.instruction.AddOrder();
                    ao.setWorkflowName(getWorkflowName(h, cao));
                    ao.setArguments(getArguments(h, cao));

                    List<Instruction> lt = new ArrayList<>();
                    lt.add(ao);

                    ie.setThen(new Instructions(lt));
                    in.add(ie);
                }

            } catch (Throwable e) {
                LOGGER.error(String.format("[convert return codes][js1 JobChain=%s, state=%s, return_code=%s]%s", h.getJS1Node().getJobChainPath(), h
                        .getJS1State(), rc, e.toString()), e);
                ConverterReport.INSTANCE.addErrorRecord(h.getJS1Node().getJobChainPath(), "[convert return codes]state=" + h.getJS1State()
                        + ", return_code=" + rc, e);
            }
        }
        return in;
    }

    private static String getWorkflowName(String js7WorkflowName, AddOrder addOrder) {
        if (SOSString.isEmpty(addOrder.getJobChain())) {
            return js7WorkflowName;
        }
        return JS7ConverterHelper.getJS7ObjectName(JS7ConverterHelper.getFileName(addOrder.getJobChain()));
    }

    private static String getWorkflowName(String js7WorkflowName, Order order) {
        if (SOSString.isEmpty(order.getJobChain())) {
            return js7WorkflowName;
        }
        return JS7ConverterHelper.getJS7ObjectName(JS7ConverterHelper.getFileName(order.getJobChain()));
    }

    private static String getWorkflowName(JobChainStateHelper h, AddOrder addOrder) {
        if (SOSString.isEmpty(addOrder.getJobChain())) {
            return h.getJS7WorkflowName();
        }
        return JS7ConverterHelper.getJS7ObjectName(JS7ConverterHelper.getFileName(addOrder.getJobChain()));
    }

    private static Variables getArguments(JobHelper h, AddOrder addOrder) {
        Variables v = null;
        if (addOrder.hasParams()) {
            v = JS12JS7Converter.JS12JS7(addOrder.getParams());
        }
        if (addOrder.hasCopyParams()) {
            // task/order ignored - always orderPreparation params
            if (v == null) {
                v = new Variables();
            }
            h.addCopyParams(v);
        }
        return v;
    }

    private static Variables getArguments(JobHelper h, Order order) {
        Variables v = null;
        if (order.hasParams()) {
            v = JS12JS7Converter.JS12JS7(order.getParams());
        }
        if (order.hasCopyParams()) {
            // task/order ignored - always orderPreparation params
            if (v == null) {
                v = new Variables();
            }
            h.addCopyParams(v);
        }
        return v;
    }

    private static Variables getArguments(JobChainStateHelper h, AddOrder addOrder) {
        Variables v = null;
        if (addOrder.hasParams()) {
            v = JS12JS7Converter.JS12JS7(addOrder.getParams());
        }
        if (addOrder.hasCopyParams()) {
            // task/order ignored - always orderPreparation params
            if (v == null) {
                v = new Variables();
            }
            h.addCopyParams(v);
        }
        return v;
    }

    private static String getIfElsePredicate(String js1ReturnCode) {
        StringBuilder sb = new StringBuilder();
        String[] arr = js1ReturnCode.split(" ");
        switch (arr.length) {
        case 1:
            if (js1ReturnCode.indexOf("..") == -1) {
                sb.append(JS7_VAR_RETURN_CODE).append(getSingleRCOpAndReturnCode(js1ReturnCode));
            } else {
                sb.append(JS1ReturnCodeRange2JS7(js1ReturnCode));
            }
            break;
        default:
            List<String> l1 = new ArrayList<>();
            List<String> l2 = new ArrayList<>();
            for (String s : arr) {
                if (s.indexOf("..") == -1) {
                    l1.add(s);
                } else {
                    l2.add("(" + JS1ReturnCodeRange2JS7(s).toString() + ")");
                }
            }

            int l1s = l1.size();
            if (l1s > 0) {
                sb.append(JS7_VAR_RETURN_CODE);
                if (l1s == 1) {
                    sb.append(getSingleRCOpAndReturnCode(l1.get(0)));
                } else {
                    sb.append(" in [").append(String.join(",", l1)).append("]");
                }
            }
            if (l2.size() > 0) {
                if (l1s > 0) {
                    sb.append(" || ");
                }
                sb.append(String.join(" || ", l2));
            }
            break;
        }

        return sb.toString();
    }

    private static String getSingleRCOpAndReturnCode(String js1ReturnCode) {
        String op = " == ";
        String rc = js1ReturnCode;
        switch (rc.toLowerCase()) {
        case "success":
            rc = "0";
            break;
        case "error":
            op = " != ";
            rc = "0";
            break;
        }
        return op + rc;
    }

    private static StringBuilder JS1ReturnCodeRange2JS7(String js1RcRange) {
        String[] arr = js1RcRange.split("\\.\\.");

        StringBuilder sb = new StringBuilder();
        sb.append(JS7_VAR_RETURN_CODE).append(" >= ").append(arr[0]);
        if (arr.length > 1) {
            sb.append(" && ");
            sb.append(JS7_VAR_RETURN_CODE).append(" <= ").append(arr[1]);
        }
        return sb;
    }
}
