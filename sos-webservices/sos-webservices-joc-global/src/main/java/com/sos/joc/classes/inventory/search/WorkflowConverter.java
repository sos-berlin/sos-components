package com.sos.joc.classes.inventory.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowInstruction;

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
        private List<String> titles;
        private List<String> agentIds;
        private List<String> jobClasses;
        private List<String> jobResources;
        private List<String> criticalities;
        private List<String> documentationNames;
        private List<String> scripts;
        private List<String> argNames;
        private List<String> argValues;

        private JsonObject mainInfo;
        private JsonObject scriptInfo;
        private JsonObject argInfo;

        public Jobs() {
            names = new ArrayList<String>();
            titles = new ArrayList<String>();
            agentIds = new ArrayList<String>();
            jobClasses = new ArrayList<String>();
            jobResources = new ArrayList<String>();
            criticalities = new ArrayList<String>();
            documentationNames = new ArrayList<String>();
            scripts = new ArrayList<String>();
            argNames = new ArrayList<String>();
            argValues = new ArrayList<String>();
        }

        public void process(com.sos.inventory.model.workflow.Jobs jobs) {
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
            jsonAddStringValues(builder, "titles", titles);
            jsonAddStringValues(builder, "agentIds", agentIds);
            jsonAddStringValues(builder, "jobClasses", jobClasses);
            jsonAddStringValues(builder, "jobResources", jobResources);
            jsonAddStringValues(builder, "criticalities", criticalities);
            jsonAddStringValues(builder, "documentationNames", documentationNames);
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
            jsonAddObjectValues(builder, "values", argValues);
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

        public List<String> getTitles() {
            return titles;
        }

        public List<String> getAgentIds() {
            return agentIds;
        }

        public List<String> getJobClasses() {
            return jobClasses;
        }

        public List<String> getJobResources() {
            return jobResources;
        }

        public List<String> getCriticalities() {
            return criticalities;
        }
        
        public List<String> getDocumentationNames() {
            return documentationNames;
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

        private void handleJobs(com.sos.inventory.model.workflow.Jobs jobs) {
            if (jobs == null || jobs.getAdditionalProperties() == null) {
                return;
            }

            jobs.getAdditionalProperties().forEach((jobName, job) -> {
                names.add(jobName);
                if (!SOSString.isEmpty(job.getTitle())) {
                    titles.add(job.getTitle());
                }
                if (!SOSString.isEmpty(job.getAgentName())) {
                    agentIds.add(job.getAgentName());
                }
                if (!SOSString.isEmpty(job.getJobClass())) {
                    jobClasses.add(job.getJobClass());
                }
                if (job.getJobResourceNames() != null && !job.getJobResourceNames().isEmpty()) {
                    jobResources.addAll(job.getJobResourceNames());
                }
                if (!SOSString.isEmpty(job.getDocumentationName())) {
                    documentationNames.add(job.getDocumentationName());
                }
                if (job.getCriticality() != null) {
                    if (SOSString.isEmpty(job.getCriticality().value())) {
                        criticalities.add(JobCriticality.NORMAL.value());
                    } else {
                        criticalities.add(job.getCriticality().value());
                    }
                }
                if (job.getExecutable() != null) {
                    if (ExecutableType.ScriptExecutable.equals(job.getExecutable().getTYPE())) {
                        ExecutableScript es = job.getExecutable().cast();
                        if (!SOSString.isEmpty(es.getScript())) {
                            scripts.add(es.getScript());
                        }
                    }
                }

                if (job.getDefaultArguments() != null && job.getDefaultArguments().getAdditionalProperties() != null) {
                    job.getDefaultArguments().getAdditionalProperties().forEach((name, value) -> {
                        if (!SOSString.isEmpty(name)) {
                            argNames.add(name);
                        }
                        if (value != null) {
                            argValues.add(value);
                        }
                    });
                }
            });
        }

        private void removeDuplicates() {
            names = WorkflowConverter.removeDuplicates(names);
            titles = WorkflowConverter.removeDuplicates(titles);
            agentIds = WorkflowConverter.removeDuplicates(agentIds);
            jobClasses = WorkflowConverter.removeDuplicates(jobClasses);
            jobResources = WorkflowConverter.removeDuplicates(jobResources);
            criticalities = WorkflowConverter.removeDuplicates(criticalities);
            documentationNames = WorkflowConverter.removeDuplicates(documentationNames);
            scripts = WorkflowConverter.removeDuplicates(scripts);
            argNames = WorkflowConverter.removeDuplicates(argNames);
            argValues = WorkflowConverter.removeDuplicates(argValues);
        }
    }

    public class Instructions {

        private List<String> jobNames;
        private List<String> jobLabels;
        private List<String> lockIds;
        private List<String> boardNames;
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
            boardNames = new ArrayList<String>();

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
            jsonAddStringValues(builder, "boardNames", boardNames);
            if (locks.size() > 0) {
                builder.add("locks", getJsonObject(locks));
            }
            mainInfo = builder.build();
        }

        private void jsonArgInfo() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            jsonAddStringValues(builder, "jobArgNames", jobArgNames);
            jsonAddObjectValues(builder, "jobArgvalues", jobArgValues);
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

        public List<String> getBoardNames() {
            return boardNames;
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
            List<WorkflowInstruction<NamedJob>> jobs = searcher.getJobInstructions();
            if (jobs != null) {
                for (WorkflowInstruction<NamedJob> job : jobs) {
                    if (!SOSString.isEmpty(job.getInstruction().getJobName())) {
                        jobNames.add(job.getInstruction().getJobName());
                    }
                    if (!SOSString.isEmpty(job.getInstruction().getLabel())) {
                        jobLabels.add(job.getInstruction().getLabel());
                    }
                    if (job.getInstruction().getDefaultArguments() != null && job.getInstruction().getDefaultArguments()
                            .getAdditionalProperties() != null) {
                        job.getInstruction().getDefaultArguments().getAdditionalProperties().forEach((name, value) -> {
                            if (!SOSString.isEmpty(name)) {
                                jobArgNames.add(name);
                            }
                            if (value != null) {
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
            List<WorkflowInstruction<Lock>> locks = searcher.getLockInstructions();
            if (locks != null) {
                for (WorkflowInstruction<Lock> lock : locks) {
                    if (!SOSString.isEmpty(lock.getInstruction().getLockName())) {
                        lockIds.add(lock.getInstruction().getLockName());

                        Integer currentCount = lock.getInstruction().getCount() == null ? -1 : lock.getInstruction().getCount();
                        Integer previousCount = this.locks.get(lock.getInstruction().getLockName());
                        if (previousCount == null || currentCount > previousCount) {
                            this.locks.put(lock.getInstruction().getLockName(), currentCount);
                        }
                    }
                }
            }
            List<WorkflowInstruction<PostNotice>> notices = searcher.getNoticeInstructions();
            if (notices != null) {
                for (WorkflowInstruction<PostNotice> notice : notices) {
                    if (!SOSString.isEmpty(notice.getInstruction().getBoardName())) {
                        boardNames.add(notice.getInstruction().getBoardName());
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
        if (list == null) {
            return Collections.emptyList();
        }
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

    private static void jsonAddObjectValues(JsonObjectBuilder builder, String key, List<String> list) {
        if (list.size() > 0) {
            builder.add(key, getJsonArrayFromObjects(list));
        }
    }

    private static JsonArrayBuilder getJsonArrayFromObjects(List<String> list) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        list.forEach(s -> b.add(s));
        return b;
    }

    private static JsonObjectBuilder getJsonObject(Map<String, Integer> map) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        map.forEach((k, v) -> b.add(k, v));
        return b;
    }
}
