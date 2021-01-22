package com.sos.webservices.order.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.audit.DailyPlanAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.ControllerId;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.joc.model.dailyplan.RelativeDatesConverter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.ScheduleSource;
import com.sos.js7.order.initiator.ScheduleSourceDB;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;
import com.sos.webservices.order.resource.IRelativeDateConverterResource;


@Path("daily_plan")
public class RelativeDatesConverterImpl extends JOCResourceImpl implements IRelativeDateConverterResource {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeDatesConverterImpl.class);
    private static final String API_CALL = "./daily_plan/relativedates";

    @Override
    public JOCDefaultResponse postConvertRelativeDates(String accessToken, byte[] filterBytes) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RelativeDatesConverter.class);
            RelativeDatesConverter relativeDatesConverter = Globals.objectMapper.readValue(filterBytes, RelativeDatesConverter.class);
        
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, relativeDatesConverter, accessToken, null,true);
            
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("relativDates", relativeDatesConverter.getRelativDates());
            List<String> absoluteDates = new ArrayList<String>();
            relativeDatesConverter.setAbsoluteDates(absoluteDates );
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (String relativeDate:relativeDatesConverter.getRelativDates()) {
                Date d = JobSchedulerDate.getDateFrom(relativeDate, relativeDatesConverter.getTimeZone());
                String absolutDate = dateFormat.format(d);
                absoluteDates.add(absolutDate); 
            }
 
            return JOCDefaultResponse.responseStatus200(relativeDatesConverter);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
