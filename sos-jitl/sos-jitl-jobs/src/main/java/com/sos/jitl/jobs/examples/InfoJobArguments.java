package com.sos.jitl.jobs.examples;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class InfoJobArguments extends JobArguments {

    public enum InfoJobArgumentsEnum {
        TEST1, TEST2
    }

    private JobArgument<Boolean> showEnv = new JobArgument<>("show_env", false, false);
    private JobArgument<Boolean> redefineShowEnv = new JobArgument<>("redefine_show_env", false, false);
    private JobArgument<String> stringArgument = new JobArgument<>("string_argument", false);
    private JobArgument<Path> pathArgument = new JobArgument<>("path_argument", false);
    private JobArgument<String> returnVariables = new JobArgument<>("return_variables", false);
    private JobArgument<String> password = new JobArgument<>("password", false, DisplayMode.MASKED);
    private JobArgument<String> shellCommand = new JobArgument<>("shell_command", false);

    private JobArgument<List<String>> listStringValues = new JobArgument<>("list_string_values", false);
    private JobArgument<List<Integer>> listIntegerValues = new JobArgument<>("list_integer_values", false);
    private JobArgument<List<Long>> listLongValues = new JobArgument<>("list_long_values", false);
    private JobArgument<List<BigDecimal>> listBigDecimalValues = new JobArgument<>("list_bigdecimal_values", false);
    private JobArgument<List<Path>> listPathValues = new JobArgument<>("list_path_values", false);
    private JobArgument<List<File>> listFileValues = new JobArgument<>("list_file_values", false);
    private JobArgument<List<URI>> listUriValues = new JobArgument<>("list_uri_values", false);
    private JobArgument<List<Charset>> listCharsetValues = new JobArgument<>("list_charset_values", false);
    private JobArgument<List<Boolean>> listBooleanValues = new JobArgument<>("list_boolean_values", false);
    private JobArgument<List<InfoJobArgumentsEnum>> listEnumValues = new JobArgument<>("list_enum_values", false);

    private JobArgument<Set<String>> setStringValues = new JobArgument<>("set_string_values", false);
    private JobArgument<Set<Integer>> setIntegerValues = new JobArgument<>("set_integer_values", false);

    private JobArgument<Map<String, String>> mapStringValues = new JobArgument<>("map_string_values", false);
    private JobArgument<Map<String, Integer>> mapIntegerValues = new JobArgument<>("map_integer_values", false);
    private JobArgument<Map<String, Path>> mapPathValues = new JobArgument<>("map_path_values", false);
    private JobArgument<Map<String, Object>> mapObjectValues = new JobArgument<>("map_object_values", false);
    private JobArgument<Map<String, ?>> mapWildcardValues = new JobArgument<>("map_wildcard_values", false);

    public InfoJobArguments() {
        super(new CredentialStoreArguments());
    }

    public JobArgument<List<String>> getListStringValues() {
        return listStringValues;
    }

    public JobArgument<List<Integer>> getListIntegerValues() {
        return listIntegerValues;
    }

    public JobArgument<List<Long>> getListLongValues() {
        return listLongValues;
    }

    public JobArgument<List<BigDecimal>> getListBigDecimalValues() {
        return listBigDecimalValues;
    }

    public JobArgument<List<Path>> getListPathValues() {
        return listPathValues;
    }

    public JobArgument<List<File>> getListFileValues() {
        return listFileValues;
    }

    public JobArgument<List<URI>> getListUriValues() {
        return listUriValues;
    }

    public JobArgument<List<Charset>> getListCharsetValues() {
        return listCharsetValues;
    }

    public JobArgument<List<Boolean>> getListBooleanValues() {
        return listBooleanValues;
    }

    public JobArgument<List<InfoJobArgumentsEnum>> getListEnumValues() {
        return listEnumValues;
    }

    public JobArgument<Set<String>> getSetStringValues() {
        return setStringValues;
    }

    public JobArgument<Set<Integer>> getSetIntegerValues() {
        return setIntegerValues;
    }

    public JobArgument<Map<String, String>> getMapStringValues() {
        return mapStringValues;
    }

    public JobArgument<Map<String, Integer>> getMapIntegerValues() {
        return mapIntegerValues;
    }

    public JobArgument<Map<String, Path>> getMapPathValues() {
        return mapPathValues;
    }

    public JobArgument<Map<String, Object>> getMapObjectValues() {
        return mapObjectValues;
    }

    public JobArgument<Map<String, ?>> getMapWildcardValues() {
        return mapWildcardValues;
    }

    public JobArgument<Boolean> getShowEnv() {
        return showEnv;
    }

    public JobArgument<Boolean> getRedefineShowEnv() {
        return redefineShowEnv;
    }

    public JobArgument<String> getStringArgument() {
        return stringArgument;
    }

    public JobArgument<Path> getPathArgument() {
        return pathArgument;
    }

    public JobArgument<String> getReturnVariables() {
        return returnVariables;
    }

    public JobArgument<String> getPassword() {
        return password;
    }

    public JobArgument<String> getShellCommand() {
        return shellCommand;
    }
}
