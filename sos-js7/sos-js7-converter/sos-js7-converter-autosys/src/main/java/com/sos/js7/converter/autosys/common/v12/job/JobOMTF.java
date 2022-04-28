package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attributes.AJobArguments;

public class JobOMTF extends ACommonMachineJob {

    /** encoding - Specify the Character Encoding of a File<br/>
     * This attribute is optional for the Text File Reading and Monitoring (OMTF) job type.<br/>
     * 
     * Format:encoding: char_set_name<br/>
     * char_set_name - Specifies the name of the character set used to encode the data in the file.<br/>
     * Default: US-ASCII<br/>
     * Limits: Up to 42 characters<br/>
     * Examples: ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16<br/>
     * NOTE - The supported character sets depends on your operating system and JVM.<br/>
     * <br/>
     * 
     * JS7 - 0% - Feature requires development for iteration 3<br/>
     */
    private SOSArgument<String> encoding = new SOSArgument<>("encoding", false);

    /** text_file_filter - Specify a Text String to Search For<br/>
     * This attribute is required for the OMTF job type.<br/>
     * 
     * Format: text_file_filter: textstring<br/>
     * <br/>
     * 
     * JS7 - 0% - This is a single feature from text_filter, text_file_filter_exists, text_file_mode, text_file_name.<br/>
     * Implementation can be provided by a template jobThis is a single feature from text_filter, text_file_filter_exists, text_file_mode, text_file_name.<br/>
     * Implementation can be provided by a template job<br/>
     */
    private SOSArgument<String> textFileFilter = new SOSArgument<>("text_file_filter", true);

    /** text_file_filter_exists - Specify Whether to Monitor for the Existence of Text<br/>
     * This attribute is optional for the OMTF job type<br/>
     * 
     * Format: text_file_filter_exists: TRUE | FALSE<br/>
     * Default: TRUE<br/>
     */
    private SOSArgument<Boolean> textFileFilterExists = new SOSArgument<>("text_file_filter_exists", false);

    /** text_file_mode - Specify the Search Mode<br/>
     * This attribute is optional for the OMTF job type<br/>
     * 
     * Format: text_file_mode: LINE | REGEX | DATETIME<br/>
     * <br/>
     */
    private SOSArgument<String> textFileMode = new SOSArgument<>("text_file_mode", false);

    /** text_file_name - Specify a Text File Name and Location<br/>
     * This attribute is required for the OMTF job type.<br/>
     */
    private SOSArgument<String> textFileName = new SOSArgument<>("ext_file_name", true);

    public JobOMTF() {
        super(JobType.OMTF);
    }

    public SOSArgument<String> getEncoding() {
        return encoding;
    }

    public void setEncoding(String val) {
        encoding.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getTextFileFilter() {
        return textFileFilter;
    }

    public void setTextFileFilter(String val) {
        textFileFilter.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<Boolean> getTextFileFilterExists() {
        return textFileFilterExists;
    }

    public void setTextFileFilterExists(String val) {
        textFileFilterExists.setValue(AJobArguments.booleanValue(val, true));
    }

    public SOSArgument<String> getTextFileMode() {
        return textFileMode;
    }

    public void setTextFileMode(String val) {
        textFileMode.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getTextFileName() {
        return textFileName;
    }

    public void setTextFileName(String val) {
        textFileName.setValue(AJobArguments.stringValue(val));
    }
}
