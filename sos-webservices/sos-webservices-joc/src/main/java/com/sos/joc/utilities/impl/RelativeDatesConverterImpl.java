package com.sos.joc.utilities.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.RelativeDatesConverter;
import com.sos.joc.utilities.resource.IRelativeDateConverterResource;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.UTILITIES)
public class RelativeDatesConverterImpl extends JOCResourceImpl implements IRelativeDateConverterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeDatesConverterImpl.class);

    @Override
    public JOCDefaultResponse postConvertRelativeDates(String accessToken, byte[] filterBytes) {
        LOGGER.debug("convert relative dates");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
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

            return JOCDefaultResponse.responseStatus200(in);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
