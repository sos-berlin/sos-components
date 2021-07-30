package com.sos.js7.order.initiator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;
 
 

//Test fails in nightly build
@Ignore
public class TestSchedules {

    @Test
    public void testIsFillListOfSchedules() throws IOException, SOSHibernateException{
        ScheduleSource orderTemplateSource = new ScheduleSourceFile("src/test/resources/schedules");
        List<Schedule> listOfSchedules = orderTemplateSource.fillListOfSchedules();
        Schedule order = listOfSchedules.get(0);
        
        assertEquals("testIsFillListOfSchedules", "testorder", order.getPath());
    }

}
