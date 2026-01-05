package com.sos.joc.utilities.impl;

import java.time.ZoneId;
import java.util.Comparator;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.utilities.TimeZones;
import com.sos.joc.utilities.resource.ITimezonesResource;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.UTILITIES)
public class TimezonesImpl extends JOCResourceImpl implements ITimezonesResource {

    @Override
    public JOCDefaultResponse postTimezones() {
        try {
            initLogging(IMPL_PATH, "{}".getBytes(), CategoryType.OTHERS);
            TimeZones entity = new TimeZones();
            entity.setTimezones(ZoneId.getAvailableZoneIds().stream().sorted().toList());
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getTimezones() {
        return postTimezones();
    }
}
