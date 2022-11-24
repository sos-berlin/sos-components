package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;

import com.sos.inventory.model.schedule.Schedule;

public class RunTimeHelper {

    private final Path path;
    private final Schedule schedule;

    public RunTimeHelper(Path path, Schedule schedule) {
        this.path = path;
        this.schedule = schedule;
    }

    public Path getPath() {
        return path;
    }

    public Schedule getSchedule() {
        return schedule;
    }

}
