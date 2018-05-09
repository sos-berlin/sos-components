package com.sos.joc.db.calendars;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.db.joc.DBItemCalendar;
import com.sos.commons.db.joc.DBItemInventoryCalendarUsage;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.event.CalendarEvent;
import com.sos.jobscheduler.model.event.CalendarObjectType;
import com.sos.jobscheduler.model.event.CalendarVariables;
import com.sos.joc.model.calendar.Calendar;

public class CalendarUsedByWriter {

    private Long instanceId;
    private String path;
    private CalendarObjectType objectType;
    private String command;
    private SOSHibernateSession sosHibernateSession;
    private Map<String, Calendar> calendars = new HashMap<String, Calendar>();

    public CalendarUsedByWriter(SOSHibernateSession sosHibernateSession, Long instanceId, CalendarObjectType objectType, String path,
            String command) {
        this.sosHibernateSession = sosHibernateSession;
        this.instanceId = instanceId;
        this.objectType = objectType;
        this.path = path;
        this.command = command;
    }

    public CalendarUsedByWriter(SOSHibernateSession sosHibernateSession, Long instanceId, CalendarObjectType objectType, String path, 
            String command, List<Calendar> calendars) {
        this.sosHibernateSession = sosHibernateSession;
        this.instanceId = instanceId;
        this.objectType = objectType;
        this.path = path;
        this.command = command;
        if (calendars != null) {
            for (Calendar calendar : calendars) {
                if (calendar.getBasedOn() != null) {
                    this.calendars.put(calendar.getBasedOn(), calendar);
                }
            }
        }
    }

    public void updateUsedBy() throws Exception {
    	// TODO: new JS 2 implementation needed
//        SOSXMLXPath sosxml = new SOSXMLXPath(new StringBuffer(command));
        Set<String> calendarPaths = new HashSet<String>();
//        NodeList calendarNodes = sosxml.selectNodeList("//date/@calendar|//holiday/@calendar");
        NodeList calendarNodes = null;
        try {
            sosHibernateSession.beginTransaction();
            CalendarUsageDBLayer calendarUsageDBLayer = new CalendarUsageDBLayer(this.sosHibernateSession);
            CalendarsDBLayer calendarsDBLayer = new CalendarsDBLayer(this.sosHibernateSession);
            List<DBItemInventoryCalendarUsage> dbCalendarUsage = calendarUsageDBLayer.getCalendarUsagesOfAnObject(instanceId,
            		objectType.name(), path);
            DBItemInventoryCalendarUsage calendarUsageDbItem = new DBItemInventoryCalendarUsage();
            calendarUsageDbItem.setInstanceId(instanceId);
            calendarUsageDbItem.setObjectType(objectType.name());
            calendarUsageDbItem.setEdited(false);
            calendarUsageDbItem.setPath(path);

            if (calendarNodes != null) {
				for (int i = 0; i < calendarNodes.getLength(); i++) {
					String calendarPath = calendarNodes.item(i).getNodeValue();
					if (calendarPath != null && !calendarPaths.contains(calendarPath)) {
						calendarPaths.add(calendarPath);
						DBItemCalendar calendarDbItem = calendarsDBLayer.getCalendar(instanceId, calendarPath);
						if (calendarDbItem != null) {
							calendarUsageDbItem.setCalendarId(calendarDbItem.getId());
							Calendar calendar = calendars.get(calendarPath);
							if (calendar != null) {
								calendarUsageDbItem.setConfiguration(new ObjectMapper().writeValueAsString(calendar));
							}
							int index = dbCalendarUsage.indexOf(calendarUsageDbItem);
							if (index == -1) {
								calendarUsageDBLayer.saveCalendarUsage(calendarUsageDbItem);
							} else {
								DBItemInventoryCalendarUsage dbItem = dbCalendarUsage.remove(index);
								dbItem.setEdited(false);
								dbItem.setConfiguration(calendarUsageDbItem.getConfiguration());
								calendarUsageDBLayer.updateCalendarUsage(dbItem);
							}
						}
					}
				} 
			}
			for (DBItemInventoryCalendarUsage dbItem : dbCalendarUsage) {
                calendarUsageDBLayer.deleteCalendarUsage(dbItem);
            }
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
            throw e;
        }
    }

    public String getEvent() throws JsonProcessingException {
        CalendarEvent calEvt = new CalendarEvent();
        calEvt.setKey("CalendarUsageUpdated");
        CalendarVariables calEvtVars = new CalendarVariables();
        calEvtVars.setPath(path);
        calEvtVars.setObjectType(objectType);
        calEvt.setVariables(calEvtVars);
        String xmlCommand = new ObjectMapper().writeValueAsString(calEvt);
        xmlCommand = "<publish_event>" + xmlCommand + "</publish_event>";
        return xmlCommand;
    }

}
