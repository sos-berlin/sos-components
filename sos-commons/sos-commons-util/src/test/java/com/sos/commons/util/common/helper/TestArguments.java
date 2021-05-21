package com.sos.commons.util.common.helper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class TestArguments extends ASOSArguments {

    SOSArgument<Boolean> myBoolean = new SOSArgument<Boolean>("my_boolean", false);
    SOSArgument<String> myPassword = new SOSArgument<String>("my_password", false, DisplayMode.MASKED);

    SOSArgument<List<String>> myList = new SOSArgument<List<String>>("my_list", false);
    SOSArgument<Map<String, String>> myMap = new SOSArgument<Map<String, String>>("my_map", false);
    SOSArgument<Path[]> myArray = new SOSArgument<Path[]>("my_array", false);

    public SOSArgument<Boolean> getMyBoolean() {
        return myBoolean;
    }

    public SOSArgument<String> getMyPassword() {
        return myPassword;
    }

    public SOSArgument<List<String>> getMyList() {
        return myList;
    }

    public SOSArgument<Map<String, String>> getMyMap() {
        return myMap;
    }

    public SOSArgument<Path[]> getMyArray() {
        return myArray;
    }

}
