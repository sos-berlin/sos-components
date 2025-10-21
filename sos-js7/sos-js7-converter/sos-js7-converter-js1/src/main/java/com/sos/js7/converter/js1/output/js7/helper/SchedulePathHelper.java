package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;

import com.sos.inventory.model.schedule.Schedule;

public class SchedulePathHelper {

    private final Path path;
    private final Schedule schedule;

    public SchedulePathHelper(Path path, Schedule schedule) {
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
