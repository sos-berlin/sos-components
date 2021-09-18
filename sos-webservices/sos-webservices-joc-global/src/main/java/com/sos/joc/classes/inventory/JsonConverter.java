package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.ListParameterType;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.sign.model.workflow.ListParameters;
import com.sos.sign.model.workflow.OrderPreparation;
import com.sos.sign.model.workflow.Parameter;
import com.sos.sign.model.workflow.ParameterListType;
import com.sos.sign.model.workflow.Parameters;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

public class JsonConverter {
    
    private final static String instructionsToConvert = String.join("|", InstructionType.FORKLIST.value(), InstructionType.ADD_ORDER.value());
    private final static Predicate<String> hasInstructionToConvert = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + instructionsToConvert + ")\"").asPredicate();
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonConverter.class);

    @SuppressWarnings("unchecked")
    public static <T> T readAsConvertedDeployObject(String json, Class<T> clazz, String commitId) throws JsonParseException, JsonMappingException,
            IOException {
        if (commitId != null && !commitId.isEmpty()) {
            json = json.replaceAll("(\"versionId\"\\s*:\\s*\")[^\"]*\"", "$1" + commitId + "\"");
        }
        if (clazz.getName().equals("com.sos.sign.model.workflow.Workflow")) {
            return (T) readAsConvertedWorkflow(json);
        } else {
            return Globals.objectMapper.readValue(json, clazz);
        }
    }
    
    public static com.sos.sign.model.workflow.Workflow readAsConvertedWorkflow(String json) throws JsonParseException, JsonMappingException, IOException {
        
        com.sos.sign.model.workflow.Workflow signWorkflow = Globals.objectMapper.readValue(json, com.sos.sign.model.workflow.Workflow.class);
        Workflow invWorkflow = Globals.objectMapper.readValue(json, Workflow.class);

        signWorkflow.setOrderPreparation(invOrderPreparationToSignOrderPreparation(invWorkflow.getOrderPreparation()));
        
        if (signWorkflow.getInstructions() != null) {
            // at the moment the converter is only necessary for ForkList, AddOrder instructions
            if (hasInstructionToConvert.test(json)) {
                convertInstructions(invWorkflow, invWorkflow.getInstructions(), signWorkflow.getInstructions());
            }
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Globals.objectMapper.writeValueAsString(signWorkflow));
        }
        
        return signWorkflow;
    }
    
    private static void convertInstructions(Workflow w, List<Instruction> invInstructions, List<com.sos.sign.model.instruction.Instruction> signInstructions) {
        if (invInstructions != null) {
            for (int i = 0; i < invInstructions.size(); i++) {
                Instruction invInstruction = invInstructions.get(i);
                com.sos.sign.model.instruction.Instruction signInstruction = signInstructions.get(i);
                switch (invInstruction.getTYPE()) {
                case FORKLIST:
                    ForkList fl = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkList sfl = signInstruction.cast();
                    convertForkList(fl, sfl);
                    if (fl.getWorkflow() != null) {
                        convertInstructions(w, fl.getWorkflow().getInstructions(), sfl.getWorkflow().getInstructions());
                    }
                    break;
                case FORK:
                    ForkJoin fj = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkJoin sfj = signInstruction.cast();
                    for (int j = 0; j < fj.getBranches().size(); j++) {
                        Branch invBranch = fj.getBranches().get(j);
                        if (invBranch.getWorkflow() != null) {
                            convertInstructions(w, invBranch.getWorkflow().getInstructions(), sfj.getBranches().get(j).getWorkflow().getInstructions());
                        }
                    }
                    break;
                case IF:
                    IfElse ifElse = invInstruction.cast();
                    com.sos.sign.model.instruction.IfElse sIfElse = signInstruction.cast();
                    if (ifElse.getThen() != null) {
                        convertInstructions(w, ifElse.getThen().getInstructions(), sIfElse.getThen().getInstructions());
                    }
                    if (ifElse.getElse() != null) {
                        convertInstructions(w, ifElse.getElse().getInstructions(), sIfElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = invInstruction.cast();
                    com.sos.sign.model.instruction.TryCatch sTryCatch = signInstruction.cast();
                    if (tryCatch.getTry() != null) {
                        convertInstructions(w, tryCatch.getTry().getInstructions(), sTryCatch.getTry().getInstructions());
                    }
                    if (tryCatch.getCatch() != null) {
                        convertInstructions(w, tryCatch.getCatch().getInstructions(), sTryCatch.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock lock = invInstruction.cast();
                    if (lock.getLockedWorkflow() != null) {
                        com.sos.sign.model.instruction.Lock sLock = signInstruction.cast();
                        convertInstructions(w, lock.getLockedWorkflow().getInstructions(), sLock.getLockedWorkflow().getInstructions());
                    }
                    break;
                case ADD_ORDER:
                    convertAddOrder(w, invInstruction.cast(), signInstruction.cast());
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void convertForkList(ForkList fl, com.sos.sign.model.instruction.ForkList sfl) {
        sfl.setChildren("$" + fl.getChildren());
        sfl.setChildToArguments("(x) => $x");
        sfl.setChildToId("(x, i) => ($i + 1) ++ \".\" ++ $x." + fl.getChildToId());
        //sfl.setChildToId("(x) => $x." + fl.getChildToId());
    }
    
    private static void convertAddOrder(Workflow w, AddOrder ao, com.sos.sign.model.instruction.AddOrder sao) {
        sao.setDeleteWhenTerminated(ao.getRemainWhenTerminated() != Boolean.TRUE);
        String timeZone = w.getTimeZone();
        if (timeZone == null || timeZone.isEmpty()) {
            timeZone = "Etc/UTC";
        }
//        String orderName = ao.getOrderName();
//        if (orderName != null && !orderName.isEmpty()) {
//            orderName = "-" + orderName;
//            String idPattern =
//                    "'#' ++ now(format='yyyy-MM-dd', timezone='%s') ++ \"#D$epochSecond-\" ++ replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2}[^-]+).*$', '$1') ++ \"%s\"";
//            sao.setOrderId(String.format(idPattern, timeZone, orderName));
//        } else {
            String idPattern =
                    "'#' ++ replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2}).*$', '$1') ++ '#D0' ++ replaceAll($js7OrderId, '^#[0-9]{4}-[0-9]{2}-[0-9]{2}#.[0-9]([0-9]{9}).*$', '$1') ++ '-' ++ replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2}[^-]+).*$', '$1')";
            sao.setOrderId(idPattern);
            // now, $epochSecond not yet supported
//            String idPattern =
//                    "'#' ++ now(format='yyyy-MM-dd', timezone='%s') ++ \"#D$epochSecond-\" ++ replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2}[^-]+).*$', '$1')";
//            sao.setOrderId(String.format(idPattern, timeZone));
//        }
        
        if (sao.getArguments() != null && sao.getArguments().getAdditionalProperties() != null) {
            sao.getArguments().getAdditionalProperties().replaceAll((k, v) -> quoteVariable(v));
        }
    }
    
    private static String quoteVariable(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> listVal = (List<Map<String, Object>>) val;
            return listVal.stream().map(mp -> {
                mp.replaceAll((k, v) -> quoteString(v.toString()).replace('=', '\0'));
                return mp;
            }).collect(Collectors.toList()).toString().replace('=', ':').replace('\0', '=');
        }
        return quoteString(val.toString());
    }
    
    private static String quoteString(String str) {
        if (str == null) {
            return null;
        }
        Either<Problem, JExpression> e = JExpression.parse(str);
        if (e.isLeft()) {
            str = JExpression.quoteString(str);
        }
        return str;
    }
    
    public static OrderPreparation invOrderPreparationToSignOrderPreparation(Requirements orderPreparation) {
        if (orderPreparation == null) {
            return null;
        }
        Parameters params = new Parameters();
        if (orderPreparation.getParameters() != null && orderPreparation.getParameters().getAdditionalProperties() != null) {
            orderPreparation.getParameters().getAdditionalProperties().forEach((k, v) -> {
                Parameter p = new Parameter();
                p.setDefault(v.getDefault());
                p.setFinal(v.getFinal());
                if (ParameterType.List.equals(v.getType())) {
                    ListParameters lps = new ListParameters();
                    if (v.getListParameters() != null && v.getListParameters().getAdditionalProperties() != null) {
                        v.getListParameters().getAdditionalProperties().forEach((k1, v1) -> {
                            lps.setAdditionalProperty(k1, v1.getType());
                        });
                    }
                    p.setType(new ParameterListType("List", lps));
                } else {
                    p.setType(v.getType()); // wrong type enum
                }
                params.setAdditionalProperty(k, p);
            });
        }
        return new OrderPreparation(params, orderPreparation.getAllowUndeclared());
    }
    
    @SuppressWarnings("unchecked")
    public static Requirements signOrderPreparationToInvOrderPreparation(OrderPreparation orderPreparation) {
        if (orderPreparation == null) {
            return null;
        }
        com.sos.inventory.model.workflow.Parameters params = new com.sos.inventory.model.workflow.Parameters();
        if (orderPreparation.getParameters() != null && orderPreparation.getParameters().getAdditionalProperties() != null) {
            orderPreparation.getParameters().getAdditionalProperties().forEach((k, v) -> {
                com.sos.inventory.model.workflow.Parameter p = new com.sos.inventory.model.workflow.Parameter();
                p.setDefault(v.getDefault());
                p.setFinal(v.getFinal());
                if (v.getType() != null) {
                    System.out.println(v.getType().getClass().getName());
                    if (v.getType() instanceof String) {
                        try {
                            p.setType(ParameterType.fromValue((String) v.getType()));
                        } catch (Exception e) {
                        }
                    } else if (v.getType() instanceof ParameterListType) {
                        p.setType(ParameterType.List);
                        ParameterListType plt = (ParameterListType) v.getType();
                        if (plt.getElementType() != null && plt.getElementType().getAdditionalProperties() != null) {
                            com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                            plt.getElementType().getAdditionalProperties().forEach((k1, v1) -> {
                                lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(v1));
                                p.setListParameters(lp);
                            });
                        }
                    } else if (v.getType() instanceof Map) {
                        p.setType(ParameterType.List);
                        Map<String, String> slp = (Map<String, String>) ((Map<String, Object>) v.getType()).get("elementType");
                        if (slp != null) {
                            com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                            slp.forEach((k1, v1) -> {
                                if (!"TYPE".equals(k1)) {
                                    try {
                                        lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(ListParameterType.fromValue(v1)));
                                        p.setListParameters(lp);
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        }
                    }
                }
                params.setAdditionalProperty(k, p);
            });
        }
        return new Requirements(params, orderPreparation.getAllowUndeclared()); 
    }

}
