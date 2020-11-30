package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventCallable2;
import com.sos.joc.event.resource.IEventResource2;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.event.JobSchedulerEvent;
import com.sos.joc.model.event.JobSchedulerEvents;
import com.sos.joc.model.event.JobSchedulerObjects;
import com.sos.joc.model.event.RegisterEvent;
import com.sos.schema.JsonValidator;

@Path("events2")
public class EventResourceImpl2 extends JOCResourceImpl implements IEventResource2 {

    private static final String API_CALL = "./events";

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        JobSchedulerEvents entity = new JobSchedulerEvents();
        Map<String, JobSchedulerEvent> eventList = new HashMap<String, JobSchedulerEvent>();
        Session session = null;
        
        
        try {
            initLogging(API_CALL, inBytes, accessToken);
            // TODO register RegisterEvent at validator?
            JsonValidator.validateFailFast(inBytes, RegisterEvent.class);
            RegisterEvent in = Globals.objectMapper.readValue(inBytes, RegisterEvent.class);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getJS7Controller().getView().isStatus());

            if (response != null) {
                return response;
            }
            
            try {
                session = getJobschedulerUser().getSosShiroCurrentUser().getCurrentSubject().getSession(false);
            } catch (InvalidSessionException e1) {
                throw new SessionNotExistException(e1);
            }
            
//            if (in.getClose() != null && in.getClose()) {
//                entity.setEvents(null);
//                entity.setDeliveryDate(Date.from(Instant.now()));
//                return JOCDefaultResponse.responseStatus200(entity);
//            }
            
            if (in.getControllers() == null && in.getControllers().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'jobscheduler'");
            }
            
            Long defaultEventId = Instant.now().toEpochMilli() * 1000;
            List<EventCallable2> tasks = new ArrayList<EventCallable2>();
            
            for (JobSchedulerObjects jsObject : in.getControllers()) {
                if (jsObject.getEventId() == null) {
                    jsObject.setEventId(defaultEventId);
                }
                tasks.add(new EventCallable2(session, jsObject.getEventId(), jsObject.getControllerId()));
            }
            
            if (!tasks.isEmpty()) {
                ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                try {
                    JobSchedulerEvent evt = executorService.invokeAny(tasks);
                    eventList.put(evt.getControllerId(), evt);
                } catch (ExecutionException e) {
                    throw (Exception) e.getCause();
                } finally {
                    executorService.shutdown();
                }
            }

            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
        } catch (JobSchedulerConnectionRefusedException e) {
            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (InvalidSessionException e) {
            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
        return JOCDefaultResponse.responseStatus200(entity);
    }

}
