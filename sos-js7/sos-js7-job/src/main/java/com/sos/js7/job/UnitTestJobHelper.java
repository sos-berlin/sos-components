package com.sos.js7.job;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.js7.job.exception.JobArgumentException;

import js7.data_for_java.order.JOutcome;

public class UnitTestJobHelper<A extends JobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestJobHelper.class);

    private final Job<A> job;
    private Map<String, String> environment;

    public UnitTestJobHelper(Job<A> job) {
        this.job = job;
        setModifiableEnvironment();
    }

    public void onStart(Map<String, Object> args) throws Exception {
        job.getJobEnvironment().setDeclaredArguments(toArgs(args, null).instance);
        SOSReflection.setDeclaredFieldValue(job.getJobEnvironment(), "allArguments", args);

        job.onStart();
    }

    public void onStop() throws Exception {
        job.onStop();
    }

    public JOutcome.Completed processOrder(Map<String, Object> args) throws Exception {
        return processOrder(args, null);
    }

    public JOutcome.Completed processOrder(Map<String, Object> args, SOSTimeout timeout) throws Exception {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }

        final OrderProcessStep<A> step = newOrderProcessStep(args);
        return CompletableFuture.supplyAsync(() -> {
            try {
                ArgumentsResult r = toArgs(args, job.onCreateJobArguments(null, step));
                step.init(r.instance, r.undeclared);

                step.checkAndLogParameterization(null, null);
                this.job.processOrder(step);
                return step.processed();
            } catch (Throwable e) {
                return step.failed(e.toString(), e);
            }
        }).get(timeout.getInterval(), timeout.getTimeUnit());
    }

    public static OrderProcessStepLogger newLogger() {
        OrderProcessStepLogger l = new OrderProcessStepLogger(null);
        l.init(null);
        return l;
    }

    public void setEnvVar(String n, String v) {
        if (environment != null) {
            environment.put(n, v);
        }
    }

    public Job<A> getJob() {
        return job;
    }

    private OrderProcessStep<A> newOrderProcessStep(Map<String, Object> args) throws Exception {
        // OrderProcessStep<A> step = new OrderProcessStep<A>(job.getJobEnvironment(), null);
        OrderProcessStep<A> step = new OrderProcessStep<A>(job.getJobEnvironment(), null) {

            @Override
            protected <AJ extends JobArguments> AJ onExecuteJobCreateArguments(Job<AJ> job, OrderProcessStep<AJ> step,
                    List<JobArgumentException> exceptions) throws Exception {

                AJ aj = job.onCreateJobArguments(exceptions, step);
                aj = job.getJobArgumensClass().getDeclaredConstructor().newInstance();
                aj = job.setDeclaredJobArguments(exceptions, null, args, null, null, aj);
                return aj;
            }
        };
        return step;
    }

    private ArgumentsResult toArgs(Map<String, Object> args, A instance) throws Exception {
        instance = instance == null ? job.getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        Set<String> declared = getDeclaredJobArgumentNames(instance);

        List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();
        instance = job.setDeclaredJobArguments(exceptions, null, args, null, null, instance);

        ArgumentsResult r = new ArgumentsResult();
        r.instance = instance;
        r.undeclared = args.entrySet().stream().filter(e -> !declared.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
        return r;
    }

    private Set<String> getDeclaredJobArgumentNames(JobArguments instance) throws Exception {
        Set<String> declared = new HashSet<>();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument<?>>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument<?> arg : e.getValue()) {
                    if (arg.getName() == null) {
                        continue;
                    }
                    declared.add(arg.getName());
                }
            }
        }
        if (instance.hasDynamicArgumentFields()) {
            for (JobArgument<?> arg : instance.getDynamicArgumentFields()) {
                if (arg.getName() == null) {
                    continue;
                }
                declared.add(arg.getName());
            }
        }
        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    if (arg.getName() == null) {
                        continue;
                    }
                    declared.add(arg.getName());
                }
            } catch (Throwable e) {
            }
        }
        return declared;
    }

    @SuppressWarnings("unchecked")
    private void setModifiableEnvironment() {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field props = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            props.setAccessible(true);
            environment = (Map<String, String>) props.get(null);
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_HOME, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_WORK_DIR, "");
        } catch (Throwable e) {
            LOGGER.info(String.format(
                    "[can't set env][use 'Run Configurations -> Environment' instead or allow access with '--add-opens java.base/java.util=ALL-UNNAMED']%s",
                    e.toString()));
        }
    }

    private void setDefaultEnvVar(String n, String v) {
        if (environment != null && environment.get(n) == null) {
            environment.put(n, v);
        }
    }

    private class ArgumentsResult {

        private A instance;
        private Map<String, Object> undeclared;
    }

}
