package com.sos.joc.publish.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class UpDownloadMapper {

	public static final ObjectMapper initiateObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.disable(SerializationFeature.INDENT_OUTPUT);
		return om;
	}
}
