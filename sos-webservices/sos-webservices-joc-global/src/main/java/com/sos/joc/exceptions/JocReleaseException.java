package com.sos.joc.exceptions;

import com.sos.joc.model.inventory.common.ConfigurationType;

public class JocReleaseException extends JocException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-415";

    public JocReleaseException(ConfigurationType type, String path, Throwable cause) {
        super(new JocError(ERROR_CODE, String.format("[%s path=%s]%s", type, path, cause.getMessage())), cause);
    }

}
