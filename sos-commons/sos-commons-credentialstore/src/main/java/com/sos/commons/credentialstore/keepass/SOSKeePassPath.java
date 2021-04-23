package com.sos.commons.credentialstore.keepass;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Splitter;

import com.sos.commons.util.SOSString;

public class SOSKeePassPath {

    public static final String PATH_PREFIX = "cs://";
    public static final String PROPERTY_PREFIX = "@";

    public static final String QUERY_PARAMETER_FILE = "file";
    public static final String QUERY_PARAMETER_KEY_FILE = "key_file";
    public static final String QUERY_PARAMETER_PASSWORD = "password";// '&' and '=' characters not allowed
    // not set or ignore_expired=0 - throwing an entry expired exception if an entry is expired
    public static final String QUERY_PARAMETER_IGNORE_EXPIRED = "ignore_expired";
    public static final String QUERY_PARAMETER_ATTACHMENT = "attachment";
    public static final String QUERY_PARAMETER_CREATE_ENTRY = "create_entry";
    public static final String QUERY_PARAMETER_SET_PROPERTY = "set_property"; // '&' and '=' characters not allowed
    public static final String QUERY_PARAMETER_STDOUT_ON_SET_BINARY_PROPERTY = "stdout_on_set_binary_property";

    // if a query param value contains the & or = character, this characters must be masked to avoid an query split exception
    // example: ...&set_property=X '&' Y
    // will be evaluated to ...&set_property=X & Y
    private static final String MASK_QUERY_PARAMETERS_REGEX_SPLITTER = "'&'";
    private static final String MASK_QUERY_PARAMETERS_REGEX_KEY_VALUE_SEPARATOR = "'='";
    private static final String MASK_QUERY_PARAMETERS_REPLACEMENT_SPLITTER = "_XXX_";
    private static final String MASK_QUERY_PARAMETERS_REPLACEMENT_KEY_VALUE_SEPARATOR = "_YYY_";

    private boolean _isKdbx;
    private boolean _valid;
    private String _entry;
    private String _entryPath;
    private String _propertyName;
    private String _originalPropertyName;
    private String _query;
    private Map<String, String> _queryParameters;
    private String _error;

    /** uri example read:
     * 
     * cs://server/SFTP/my_server@file.txt?file=my_file.kdbx&key_file=my_keyfile.key&password=test&ignore_expired=1&attachment=1
     * 
     * uri example set @user property and create "server/SFTP/my_server" entry if not exists:
     * 
     * cs://server/SFTP/my_server@user?file=my_file.kdbx&key_file=my_keyfile.key&password=test&set_property=my name&create_entry=1 */
    public SOSKeePassPath(final String uri) {
        if (SOSString.isEmpty(uri)) {
            _error = "missing uri";
            return;
        }
        int t = uri == null ? -1 : uri.indexOf("?");
        if (t < 0 || uri.endsWith("?")) {
            _error = "missing query parameters";
            return;
        }

        _query = uri.substring(t + 1, uri.length());
        Map<String, String> maskedMap = Collections.emptyMap();
        try {
            maskedMap = Splitter.on('&').withKeyValueSeparator("=").split(mask(_query));
        } catch (IllegalArgumentException e) {
        }
        Map<String, String> unmaskedMap = new LinkedHashMap<String, String>();
        maskedMap.forEach((k, v) -> {
            unmaskedMap.put(k, unmask(v));
        });
        _queryParameters = Collections.unmodifiableMap(unmaskedMap);

        String file = _queryParameters.get(QUERY_PARAMETER_FILE);
        if (SOSString.isEmpty(file)) {
            _error = String.format("missing query parameter '%s'", QUERY_PARAMETER_FILE);
        } else {
            parse(file.toLowerCase().endsWith(".kdbx"), uri.substring(0, t), null);
        }
    }

    private String mask(String val) {
        return val.replaceAll(MASK_QUERY_PARAMETERS_REGEX_SPLITTER, MASK_QUERY_PARAMETERS_REPLACEMENT_SPLITTER).replaceAll(
                MASK_QUERY_PARAMETERS_REGEX_KEY_VALUE_SEPARATOR, MASK_QUERY_PARAMETERS_REPLACEMENT_KEY_VALUE_SEPARATOR);
    }

    private String unmask(String val) {
        return val.replaceAll(MASK_QUERY_PARAMETERS_REPLACEMENT_SPLITTER, MASK_QUERY_PARAMETERS_REGEX_SPLITTER).replaceAll(
                MASK_QUERY_PARAMETERS_REPLACEMENT_KEY_VALUE_SEPARATOR, MASK_QUERY_PARAMETERS_REGEX_KEY_VALUE_SEPARATOR);
    }

    public SOSKeePassPath(final boolean isKdbx, final String path) {
        this(isKdbx, path, null);
    }

    public SOSKeePassPath(final boolean isKdbx, final String path, final String entryPath) {
        parse(isKdbx, path, entryPath);
    }

    private void parse(final boolean isKdbx, final String path, final String entryPath) {
        _isKdbx = isKdbx;
        if (path == null || !path.startsWith(PATH_PREFIX) || !path.contains(PROPERTY_PREFIX)) {
            _error = String.format("is empty or not starts with '%s' or not contains '%s'", PATH_PREFIX, PROPERTY_PREFIX);
            return;
        }

        String[] arr = path.substring(PATH_PREFIX.length() - 1).split(PROPERTY_PREFIX);
        switch (arr.length) {
        case 2:
            setEntryPath(arr[0], entryPath, isKdbx);
            setPropertyName(arr[1]);
            if (_entry != null) {
                _valid = true;
            }
            break;
        }
    }

    public static boolean hasKeePassVariables(String command) {
        return command != null && command.contains("${" + PATH_PREFIX);
    }

    private void setEntryPath(final String path, final String entryPath, final boolean isKdbx) {
        if (path.equals("/")) {
            _entry = SOSString.isEmpty(entryPath) ? null : entryPath;
        } else {
            _entry = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        }
        if (_entry != null) {
            _entryPath = isKdbx ? _entry : "/" + SOSKeePassDatabase.KDB_ROOT_GROUP_NAME + _entry;
        }
    }

    private void setPropertyName(final String name) {
        _originalPropertyName = name;
        _propertyName = SOSKeePassDatabase.getPropertyName(_originalPropertyName);
    }

    public boolean isKdbx() {
        return _isKdbx;
    }

    public boolean isValid() {
        return _valid;
    }

    public String getEntry() {
        return _entry;
    }

    public String getEntryPath() {
        return _entryPath;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public String getOriginalPropertyName() {
        return _originalPropertyName;
    }

    public String getQuery() {
        return _query;
    }

    public Map<String, String> getQueryParameters() {
        return _queryParameters;
    }

    public String getError() {
        return _error;
    }

    public boolean isIgnoreExpired() {
        return getBooleanValue(QUERY_PARAMETER_IGNORE_EXPIRED);
    }

    public boolean isCreateEntry() {
        return getBooleanValue(QUERY_PARAMETER_CREATE_ENTRY);
    }

    public boolean isAttachment() {
        return getBooleanValue(QUERY_PARAMETER_ATTACHMENT);
    }

    public boolean isStdoutOnSetBinaryProperty() {
        return getBooleanValue(QUERY_PARAMETER_STDOUT_ON_SET_BINARY_PROPERTY);
    }

    private boolean getBooleanValue(String paramName) {
        if (_queryParameters != null) {
            String val = _queryParameters.get(paramName);
            if (val != null && (val.equals("1") || val.equals("true"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (!_valid) {
            return "";
        }
        return _entry + PROPERTY_PREFIX + _propertyName;
    }
}
