package com.sos.js7.converter.commons;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.model.agent.transfer.Agent;

public class JS7ConverterResult {

    private JS7ExportObjects<Workflow> workflows = new JS7ExportObjects<>();
    private JS7ExportObjects<Agent> agents = new JS7ExportObjects<>();
    private JS7ExportObjects<Calendar> calendars = new JS7ExportObjects<>();
    private JS7ExportObjects<Schedule> schedules = new JS7ExportObjects<>();
    private JS7ExportObjects<Board> boards = new JS7ExportObjects<>();
    private JS7ExportObjects<JobResource> jobResources = new JS7ExportObjects<>();
    private JS7ExportObjects<FileOrderSource> fileOrderSources = new JS7ExportObjects<>();
    private JS7ExportObjects<Script> includeScripts = new JS7ExportObjects<>();
    private JS7ExportObjects<Lock> locks = new JS7ExportObjects<>();
    private JS7ExportObjects<JobTemplate> jobTemplates = new JS7ExportObjects<>();

    private JS7ExportObjects<Workflow> pseudoWorkflows = new JS7ExportObjects<>();

    private Object converter;

    private PostNotices postNotices = this.new PostNotices();
    private Set<String> applications = new HashSet<>();
    private List<Object> nonSupported;

    public void add(Path path, Workflow val, boolean reference) {
        workflows.addItem(path, val, reference);
    }

    public void addPseudoWorkflow(Path path, Workflow val, boolean reference) {
        pseudoWorkflows.addItem(path, val, reference);
    }

    public void add(Path path, Agent val, boolean reference) {
        agents.addItem(path, val, reference);
    }

    public void addOrReplace(Path path, Workflow val, boolean reference) {
        workflows.addOrReplaceItem(path, val, reference);
    }

    public void add(Path path, Calendar val, boolean reference) {
        calendars.addItem(path, val, reference);
    }

    public void add(Path path, Schedule val, boolean reference) {
        schedules.addItem(path, val, reference);
    }

    public void add(Path path, Board val, boolean reference) {
        boards.addItem(path, val, reference);
    }

    public void add(Path path, JobResource val, boolean reference) {
        jobResources.addItem(path, val, reference);
    }

    public void add(Path path, FileOrderSource val, boolean reference) {
        fileOrderSources.addItem(path, val, reference);
    }

    public void add(Path path, Script val, boolean reference) {
        includeScripts.addItem(path, val, reference);
    }

    public void add(Path path, Lock val, boolean reference) {
        locks.addItem(path, val, reference);
    }

    public void add(Path path, JobTemplate val, boolean reference) {
        jobTemplates.addItem(path, val, reference);
    }

    public JS7ExportObject<Workflow> getExportObjectWorkflowByPath(String name) {
        return workflows.getAllItems().stream().filter(o -> o.getOriginalPath().getPath().getFileName().toString().equals(name + ".workflow.json"))
                .findAny().orElse(null);
    }

    public JS7ExportObject<Workflow> getExportObjectWorkflowByPath(Path path) {
        if (path == null) {
            return null;
        }
        Path p = normalize(path);
        return workflows.getAllItems().stream().filter(o -> o.getOriginalPath().getPath().equals(p)).findAny().orElse(null);
    }

    public JS7ExportObject<Workflow> getExportObjectWorkflowByJobName(String name) {
        return workflows.getAllItems().stream().filter(o -> o.getObject().getJobs() != null && o.getObject().getJobs()
                .getAdditionalProperties() != null && o.getObject().getJobs().getAdditionalProperties().containsKey(name)).findAny().orElse(null);
    }

    public static Path normalize(Path path) {
        if (path == null) {
            return null;
        }
        String s = path.toString().replace("\\", "/");
        if (s.startsWith("/")) {
            return Paths.get(s.substring(1));
        }
        return path;
    }

    public JS7ExportObjects<Workflow> getWorkflows() {
        return workflows;
    }

    public JS7ExportObjects<Workflow> getPseudoWorkflows() {
        return pseudoWorkflows;
    }

    public JS7ExportObjects<Agent> getAgents() {
        return agents;
    }

    public JS7ExportObjects<Calendar> getCalendars() {
        return calendars;
    }

    public JS7ExportObjects<Schedule> getSchedules() {
        return schedules;
    }

    public JS7ExportObjects<Board> getBoards() {
        return boards;
    }

    public JS7ExportObjects<JobResource> getJobResources() {
        return jobResources;
    }

    public JS7ExportObjects<FileOrderSource> getFileOrderSources() {
        return fileOrderSources;
    }

    public JS7ExportObjects<Script> getIncludeScripts() {
        return includeScripts;
    }

    public JS7ExportObjects<Lock> getLocks() {
        return locks;
    }

    public JS7ExportObjects<JobTemplate> getJobTemplates() {
        return jobTemplates;
    }

    public PostNotices getPostNotices() {
        return postNotices;
    }

    public Set<String> getApplications() {
        return applications;
    }

    public List<Object> getNonSupported() {
        return nonSupported;
    }

    public void setConverter(Object val) {
        converter = val;
    }

    public Object getConverter() {
        return converter;
    }

    public class PostNotices {

        private Set<String> success = new HashSet<>();
        private Set<String> done = new HashSet<>();
        private Set<String> failed = new HashSet<>();

        public boolean isEmpty() {
            return success.size() == 0 && done.size() == 0 && failed.size() == 0;
        }

        public Set<String> getSuccess() {
            return success;
        }

        public Set<String> getDone() {
            return done;
        }

        public Set<String> getFailed() {
            return failed;
        }

    }

}
