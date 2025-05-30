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
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;

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

    public void add(Path path, Workflow val) {
        workflows.addItem(path, val);
    }

    public void addPseudoWorkflow(Path path, Workflow val) {
        pseudoWorkflows.addItem(path, val);
    }

    public void add(Path path, Agent val) {
        agents.addItem(path, val);
    }

    public void addOrReplace(Path path, Workflow val) {
        workflows.addOrReplaceItem(path, val);
    }

    public void add(Path path, Calendar val) {
        calendars.addItem(path, val);
    }

    public void add(Path path, Schedule val) {
        schedules.addItem(path, val);
    }

    public void add(Path path, Board val) {
        boards.addItem(path, val);
    }

    public void add(Path path, JobResource val) {
        jobResources.addItem(path, val);
    }

    public void add(Path path, FileOrderSource val) {
        fileOrderSources.addItem(path, val);
    }

    public void add(Path path, Script val) {
        includeScripts.addItem(path, val);
    }

    public void add(Path path, Lock val) {
        locks.addItem(path, val);
    }

    public void add(Path path, JobTemplate val) {
        jobTemplates.addItem(path, val);
    }

    @SuppressWarnings("rawtypes")
    public JS7ExportObject getExportObjectWorkflowByPath(String name) {
        return workflows.getItems().stream().filter(o -> o.getOriginalPath().getPath().getFileName().toString().equals(name + ".workflow.json"))
                .findAny().orElse(null);
    }

    @SuppressWarnings("rawtypes")
    public JS7ExportObject getExportObjectWorkflowByPath(Path path) {
        if (path == null) {
            return null;
        }
        Path p = normalize(path);
        return workflows.getItems().stream().filter(o -> o.getOriginalPath().getPath().equals(p)).findAny().orElse(null);
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

    @SuppressWarnings("rawtypes")
    public JS7ExportObject getExportObjectWorkflowByJobName(String name) {
        return workflows.getItems().stream().filter(o -> o.getObject().getJobs() != null && o.getObject().getJobs().getAdditionalProperties() != null
                && o.getObject().getJobs().getAdditionalProperties().containsKey(name)).findAny().orElse(null);
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
