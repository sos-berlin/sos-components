package com.sos.js7.converter.commons;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;

public class JS7ConverterResult {

    private JS7ExportObjects<Workflow> workflows = new JS7ExportObjects<>();
    private JS7ExportObjects<Calendar> calendars = new JS7ExportObjects<>();
    private JS7ExportObjects<Schedule> schedules = new JS7ExportObjects<>();
    private JS7ExportObjects<Board> boards = new JS7ExportObjects<>();

    private PostNotices postNotices = this.new PostNotices();
    private Set<String> applications = new HashSet<>();
    private List<Object> nonSupported;

    public void add(Path path, Workflow val) {
        workflows.addItem(path, val);
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

    @SuppressWarnings("rawtypes")
    public JS7ExportObject getExportObjectWorkflow(String name) {
        return workflows.getItems().stream().filter(o -> o.getOriginalPath().getPath().getFileName().toString().equals(name + ".workflow.json"))
                .findAny().orElse(null);
    }

    public JS7ExportObjects<Workflow> getWorkflows() {
        return workflows;
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

    public PostNotices getPostNotices() {
        return postNotices;
    }

    public Set<String> getApplications() {
        return applications;
    }

    public List<Object> getNonSupported() {
        return nonSupported;
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
