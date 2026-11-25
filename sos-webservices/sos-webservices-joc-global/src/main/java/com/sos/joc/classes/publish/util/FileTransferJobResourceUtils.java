package com.sos.joc.classes.publish.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.util.SOSPathUtils;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.job.Environment;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;

public class FileTransferJobResourceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferJobResourceUtils.class);

    private static final String JOBRESOURCE_ARG_NAME = "settings";

    public static com.sos.sign.model.filetransfer.FileTransferJobResource toSignedFileTransferJobResource(String objectPath, String json)
            throws JsonParseException, JsonMappingException, IOException {
        com.sos.sign.model.filetransfer.FileTransferJobResource signed = new com.sos.sign.model.filetransfer.FileTransferJobResource();
        signed.setPath(SOSPathUtils.getBaseName(objectPath));

        // convert only to remove version ...
        FileTransferJobResource ft = FileTransferHelper.toFileTransferJobResource(json);
        ft.setVersion(null);

        Environment vars = new Environment();
        vars.setAdditionalProperty(JOBRESOURCE_ARG_NAME, "toFile( '" + Globals.objectMapper.writeValueAsString(ft) + "', '*.json' )");
        signed.setVariables(vars);

        Environment env = new Environment();
        env.setAdditionalProperty(JOBRESOURCE_ARG_NAME.toUpperCase(), "$" + JOBRESOURCE_ARG_NAME);
        signed.setEnv(env);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[toSignedFileTransferJobResource]%s", Globals.objectMapper.writeValueAsString(signed)));
        }

        return signed;
    }
}
