package com.sos.joc.classes.calendar;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.model.event.CalendarEvent;
import com.sos.jobscheduler.model.event.CalendarObjectType;
import com.sos.jobscheduler.model.event.CalendarVariables;
import com.sos.joc.exceptions.JocException;

public abstract class SendCalendarEventsUtil {

    private static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    
    public static void sendEvent(Collection<String> xmlCommands, List<DBItemInventoryInstance> dbItemInventoryInstances, String accessToken) throws JocException {
        if (dbItemInventoryInstances != null && xmlCommands != null) {
            for (DBItemInventoryInstance dbItemInventoryInstance : dbItemInventoryInstances) {
                sendEvent(xmlCommands, dbItemInventoryInstance, accessToken);
            }
        }
    }

    public static void sendEvent(Collection<String> xmlCommands, DBItemInventoryInstance dbItemInventoryInstance, String accessToken)
            throws JocException {
    	//TODO
//        JOCXmlCommand jocXmlCommand = new JOCXmlCommand(dbItemInventoryInstance);
//        if (xmlCommands != null) {
//            for (String command : xmlCommands) {
//                try {
//                    jocXmlCommand.executePostWithRetry(command, accessToken);
//                } catch (JobSchedulerConnectionRefusedException | JobSchedulerConnectionResetException e) {
//                }
//            }
//        }
    }

    public static String addCalUsageEvent(String path, String objectType, String key) throws JsonProcessingException, JocException {
        CalendarEvent calEvt = new CalendarEvent();
        calEvt.setKey(key);
        CalendarVariables calEvtVars = new CalendarVariables();
        calEvtVars.setPath(path);
        calEvtVars.setObjectType(CalendarObjectType.fromValue(objectType));
        calEvt.setVariables(calEvtVars);
        return addEvent(calEvt);
    }

    public static String addEvent(CalendarEvent calEvt) throws JocException, JsonProcessingException {
        String xmlCommand = objectMapper.writeValueAsString(calEvt);
        return "<publish_event>" + xmlCommand + "</publish_event>";
    }
    
    public static void sendEvent(CalendarEvent calEvt, List<DBItemInventoryInstance> dbItemInventoryInstances, String accessToken)
            throws JocException, JsonProcessingException {
        if (dbItemInventoryInstances != null) {
            for (DBItemInventoryInstance dbItemInventoryInstance : dbItemInventoryInstances) {
                sendEvent(calEvt, dbItemInventoryInstance, accessToken);
            }
        }
    }

    public static void sendEvent(CalendarEvent calEvt, DBItemInventoryInstance dbItemInventoryInstance, String accessToken) throws JocException,
            JsonProcessingException {
        //TODO
//    	try {
//            JOCXmlCommand jocXmlCommand = new JOCXmlCommand(dbItemInventoryInstance);
//            jocXmlCommand.executePostWithRetry(addEvent(calEvt), accessToken);
//        } catch (JobSchedulerConnectionRefusedException e) {
//        } catch (JobSchedulerConnectionResetException e) {
//        }
    }
}
