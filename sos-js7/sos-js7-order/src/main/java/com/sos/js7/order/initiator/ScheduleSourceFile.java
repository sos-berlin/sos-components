package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;

public class ScheduleSourceFile extends ScheduleSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleSourceFile.class);
    protected String templateFolder;

    public ScheduleSourceFile(String templateFolder) {
        super();
        this.templateFolder = templateFolder;
    }

    
    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        List<Schedule> listOfSchedules = new ArrayList<Schedule>();
        
         for (Path p : Files.walk(Paths.get(templateFolder)).filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet())) {
            Schedule schedule = new ObjectMapper().readValue(Files.readAllBytes(p), Schedule.class);
            if (schedule.getPlanOrderAutomatically() == null){
                schedule.setPlanOrderAutomatically(true);
            }
            if (schedule.getSubmitOrderToControllerWhenPlanned() == null){
                schedule.setSubmitOrderToControllerWhenPlanned(true);
            }
            LOGGER.trace("adding order: " + schedule.getPath() + " for workflow: " + schedule.getWorkflowPath());
            if (checkMandatory(schedule)) {
                listOfSchedules.add(schedule);
            }
        }
        return listOfSchedules;
    }


    @Override
    public String fromSource() {
        return "folder:" + templateFolder;
    }

}
