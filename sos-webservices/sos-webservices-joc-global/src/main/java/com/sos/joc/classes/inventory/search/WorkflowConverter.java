package com.sos.joc.classes.inventory.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.Lock;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.inventory.common.JobCriticality;

public class WorkflowConverter {

    private final Jobs jobs;
    private final Instructions instructions;
    private WorkflowSearcher searcher;

    public WorkflowConverter() {
        jobs = new Jobs();
        instructions = new Instructions();
    }

    public void process(Workflow w) {
        searcher = new WorkflowSearcher(w);
        if (w != null) {
            jobs.process(w.getJobs());
            instructions.process(w.getInstructions());
        }
    }

    public Jobs getJobs() {
        return jobs;
    }

    public Instructions getInstructions() {
        return instructions;
    }

    public WorkflowSearcher getSearcher() {
        return searcher;
    }

    public class Jobs {

        private List<String> names;
        private List<String> titels;
        private List<String> agentIds;
        private List<String> jobClasses;
        private List<String> criticalities;
        private List<String> scripts;
        private List<String> argNames;
        private List<String> argValues;

        private JsonObject mainInfo;
        private JsonObject scriptInfo;
        private JsonObject argInfo;

        public Jobs() {
            names = new ArrayList<String>();
            titels = new ArrayList<String>();
            agentIds = new ArrayList<String>();
            jobClasses = new ArrayList<String>();
            criticalities = new ArrayList<String>();
            scripts = new ArrayList<String>();
            argNames = new ArrayList<String>();
            argValues = new ArrayList<String>();
        }

        public void process(com.sos.jobscheduler.model.workflow.Jobs jobs) {
            handleJobs(jobs);
            removeDuplicates();
            toJson();
        }

        private void toJson() {
            jsonMainInfo();
            jsonScriptInfo();
            jsonArgInfo();
        }

        private void jsonMainInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "names", names);
            jsonAddStringValues(builder, "titels", titels);
            jsonAddStringValues(builder, "agentIds", agentIds);
            jsonAddStringValues(builder, "jobClasses", jobClasses);
            jsonAddStringValues(builder, "criticalities", criticalities);
            mainInfo = builder.build();
        }

        private void jsonScriptInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "scripts", scripts);
            scriptInfo = builder.build();
        }

        private void jsonArgInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "names", argNames);
            jsonAddStringValues(builder, "values", argValues);
            argInfo = builder.build();
        }

        public JsonObject getMainInfo() {
            return mainInfo;
        }

        public JsonObject getScriptInfo() {
            return scriptInfo;
        }

        public JsonObject getArgInfo() {
            return argInfo;
        }

        public List<String> getNames() {
            return names;
        }

        public List<String> getTitels() {
            return titels;
        }

        public List<String> getAgentIds() {
            return agentIds;
        }

        public List<String> getJobClasses() {
            return jobClasses;
        }

        public List<String> getCriticalities() {
            return criticalities;
        }

        public List<String> getScripts() {
            return scripts;
        }

        public List<String> getArgNames() {
            return argNames;
        }

        public List<String> getArgValues() {
            return argValues;
        }

        private void handleJobs(com.sos.jobscheduler.model.workflow.Jobs jobs) {
            if (jobs == null || jobs.getAdditionalProperties() == null) {
                return;
            }

            jobs.getAdditionalProperties().forEach((jobName, job) -> {
                names.add(jobName);
                if (!SOSString.isEmpty(job.getTitle())) {
                    titels.add(job.getTitle());
                }
                if (!SOSString.isEmpty(job.getAgentId())) {
                    agentIds.add(job.getAgentId());
                }
                if (!SOSString.isEmpty(job.getJobClass())) {
                    jobClasses.add(job.getJobClass());
                }
                if (job.getCriticality() != null) {
                    if (SOSString.isEmpty(job.getCriticality().value())) {
                        criticalities.add(JobCriticality.NORMAL.value());
                    } else {
                        criticalities.add(job.getCriticality().value());
                    }
                }
                if (job.getExecutable() != null) {
                    if (!SOSString.isEmpty(job.getExecutable().getScript())) {
                        scripts.add(job.getExecutable().getScript());
                    }
                }

                if (job.getDefaultArguments() != null && job.getDefaultArguments().getAdditionalProperties() != null) {
                    job.getDefaultArguments().getAdditionalProperties().forEach((name, value) -> {
                        if (!SOSString.isEmpty(name)) {
                            argNames.add(name);
                        }
                        if (!SOSString.isEmpty(value)) {
                            argValues.add(value);
                        }
                    });
                }
            });
        }

        private void removeDuplicates() {
            names = WorkflowConverter.removeDuplicates(names);
            titels = WorkflowConverter.removeDuplicates(titels);
            agentIds = WorkflowConverter.removeDuplicates(agentIds);
            jobClasses = WorkflowConverter.removeDuplicates(jobClasses);
            criticalities = WorkflowConverter.removeDuplicates(criticalities);
            scripts = WorkflowConverter.removeDuplicates(scripts);
            argNames = WorkflowConverter.removeDuplicates(argNames);
            argValues = WorkflowConverter.removeDuplicates(argValues);
        }
    }

    public class Instructions {

        private List<String> jobNames;
        private List<String> jobLabels;
        private List<String> lockIds;
        private Map<String, Integer> locks;

        private List<String> jobArgNames;
        private List<String> jobArgValues;

        private JsonObject mainInfo;
        private JsonObject argInfo;

        public Instructions() {
            jobNames = new ArrayList<String>();
            jobLabels = new ArrayList<String>();
            lockIds = new ArrayList<String>();
            locks = new HashMap<String, Integer>();

            jobArgNames = new ArrayList<String>();
            jobArgValues = new ArrayList<String>();
        }

        public void process(List<Instruction> instructions) {
            handleJobInstructions(instructions);
            handleLockInstructions(instructions);
            removeDuplicates();
            toJson();
        }

        private void toJson() {
            jsonMainInfo();
            jsonArgInfo();
        }

        private void jsonMainInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "jobNames", jobNames);
            jsonAddStringValues(builder, "jobLabels", jobLabels);
            jsonAddStringValues(builder, "lockIds", lockIds);
            if (locks.size() > 0) {
                builder.add("locks", getJsonObject(locks));
            }
            mainInfo = builder.build();
        }

        private void jsonArgInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "jobArgNames", jobArgNames);
            jsonAddStringValues(builder, "jobArgvalues", jobArgValues);
            argInfo = builder.build();
        }

        public JsonObject getMainInfo() {
            return mainInfo;
        }

        public JsonObject getArgInfo() {
            return argInfo;
        }

        public List<String> getJobNames() {
            return jobNames;
        }

        public List<String> getJobLabels() {
            return jobLabels;
        }

        public List<String> getLockIds() {
            return lockIds;
        }

        public Map<String, Integer> getLocks() {
            return locks;
        }

        public List<String> getJobArgNames() {
            return jobArgNames;
        }

        public List<String> getJobArgValues() {
            return jobArgValues;
        }

        private void handleJobInstructions(List<Instruction> instructions) {
            if (instructions == null) {
                return;
            }
            List<NamedJob> jobs = searcher.getJobInstructions();
            if (jobs != null) {
                for (NamedJob job : jobs) {
                    if (!SOSString.isEmpty(job.getJobName())) {
                        jobNames.add(job.getJobName());
                    }
                    if (!SOSString.isEmpty(job.getLabel())) {
                        jobLabels.add(job.getLabel());
                    }
                    if (job.getDefaultArguments() != null && job.getDefaultArguments().getAdditionalProperties() != null) {
                        job.getDefaultArguments().getAdditionalProperties().forEach((name, value) -> {
                            if (!SOSString.isEmpty(name)) {
                                jobArgNames.add(name);
                            }
                            if (!SOSString.isEmpty(value)) {
                                jobArgValues.add(value);
                            }
                        });
                    }
                }
            }
        }

        private void handleLockInstructions(List<Instruction> instructions) {
            if (instructions == null) {
                return;
            }
            List<Lock> locks = searcher.getLockInstructions();
            if (locks != null) {
                for (Lock lock : locks) {
                    if (!SOSString.isEmpty(lock.getLockId())) {
                        lockIds.add(lock.getLockId());

                        Integer currentCount = lock.getCount() == null ? -1 : lock.getCount();
                        Integer previousCount = this.locks.get(lock.getLockId());
                        if (previousCount == null || currentCount > previousCount) {
                            this.locks.put(lock.getLockId(), currentCount);
                        }
                    }
                }
            }
        }

        private void removeDuplicates() {
            jobNames = WorkflowConverter.removeDuplicates(jobNames);
            jobLabels = WorkflowConverter.removeDuplicates(jobLabels);
            lockIds = WorkflowConverter.removeDuplicates(lockIds);

            jobArgNames = WorkflowConverter.removeDuplicates(jobArgNames);
            jobArgValues = WorkflowConverter.removeDuplicates(jobArgValues);
        }

    }

    private static <T> List<T> removeDuplicates(List<T> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    private static void jsonAddStringValues(JsonObjectBuilder builder, String key, List<String> list) {
        if (list.size() > 0) {
            builder.add(key, getJsonArray(list));
        }
    }

    private static JsonArrayBuilder getJsonArray(List<String> list) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for (String n : list) {
            b.add(n);
        }
        return b;
    }

    private static JsonObjectBuilder getJsonObject(Map<String, Integer> map) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        map.forEach((k, v) -> b.add(k, v));
        return b;
    }
}
