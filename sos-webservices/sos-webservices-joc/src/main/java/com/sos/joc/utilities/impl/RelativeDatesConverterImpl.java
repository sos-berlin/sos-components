package com.sos.joc.utilities.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.dailyplan.RelativeDatesConverter;
import com.sos.joc.utilities.resource.IRelativeDateConverterResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.UTILITIES)
public class RelativeDatesConverterImpl extends JOCResourceImpl implements IRelativeDateConverterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeDatesConverterImpl.class);

    @Override
    public JOCDefaultResponse postConvertRelativeDates(String accessToken, byte[] filterBytes) {
        LOGGER.debug("convert relative dates");
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, RelativeDatesConverter.class);
            RelativeDatesConverter in = Globals.objectMapper.readValue(filterBytes, RelativeDatesConverter.class);

            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }

            this.checkRequiredParameter("relativDates", in.getRelativDates());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            in.setAbsoluteDates(new ArrayList<String>());
            for (String relativeDate : in.getRelativDates()) {
                in.getAbsoluteDates().add(format.format(JobSchedulerDate.getDateFrom(relativeDate, in.getTimeZone())));
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(in));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
