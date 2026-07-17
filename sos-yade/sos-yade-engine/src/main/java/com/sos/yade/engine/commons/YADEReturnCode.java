package com.sos.yade.engine.commons;

public enum YADEReturnCode {

    SUCCESS(0),

    // Connection/Authentication: 20-29
    // 20-21 - source
    // 22-23 - target
    // 24-25 - jump host
    // 26 - internal return code returned by the jump client to switch the host client to an alternative profile, if enabled.
    SOURCE_CONNECTION_ERROR(20), SOURCE_AUTHENTICATION_ERROR(21), TARGET_CONNECTION_ERROR(22), TARGET_AUTHENTICATION_ERROR(23), JUMP_CONNECTION_ERROR(
            24), JUMP_AUTHENTICATION_ERROR(25), JUMP_INITIAL_SOURCE_TARGET_CONNECTION_ERROR(26),

    // Source files related: 30-39
    // 30+ - specialized source file errors
    // 39 - generic source files error
    // -- read/write/delete/access denied etc. and fallback for all other unqualified source file related errors
    SOURCE_NO_FILES_FOUND_ERROR(30), SOURCE_FILES_EXPECTED_RESULTSET_ERROR(31), SOURCE_FILES_ZERO_BYTES_ERROR(32), SOURCE_FILES_ERROR(39),

    // Target related : 40-49
    // 40+ - specialized target file errors
    // 49 - generic target files error
    // -- read/write/delete/access denied etc. during transfer and fallback for all other unqualified target file related errors
    TARGET_FILESIZE_MISMATCH_ERROR(40), TARGET_FILE_CHECKSUM_ERROR(41), TARGET_FILE_MAX_UPLOAD_FILESIZE_ERROR(42), TARGET_FILES_ERROR(49),

    // Configuration related: 80-89
    // 80 - settings file not found
    // 81 - settings file parse error (invalid XML or invalid structure)
    // 82 - required argument missing
    // 83 - profile not found
    // 89 - generic configuration error (fallback for all other unclassified configuration related errors)
    CONFIGURATION_FILE_NOT_FOUND(80), CONFIGURATION_FILE_PARSE_ERROR(81), MISSING_REQUIRED_ARGUMENT(82), PROFILE_NOT_FOUND(83), CONFIGURATION_ERROR(
            89),

    // fallback for all other unclassified errors
    DEFAULT_ERROR(99);

    private final int code;

    YADEReturnCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static YADEReturnCode fromCode(int code) {
        for (YADEReturnCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return DEFAULT_ERROR;
    }
}
