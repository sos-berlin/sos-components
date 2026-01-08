package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEFileNameInfo;

public class YADEFileReplacementHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEFileReplacementHelperTest.class);

    @Ignore
    @Test
    public void testReplace() {
        // YADE1 functionality
        String replaceWhat = "(^.*$)";
        replaceWhat = ".*";
        replaceWhat = "(.*)";
        // replaceWhat = "123"; // expected result: 1abc12defX<formatted date>-1abc12def123.TXT-1ABC12DEF123.TXT.TXT

        String replaceWith = "X[date:yyyy-MM-dd-HH-mm-ss timezone:Etc/UTC]-[filename:]-[filename:uppercase]";
        replaceWith = "XYZ";
        execute("/tmp/1abc12def123.TXT", replaceWhat, replaceWith);
    }

    @Ignore
    @Test
    public void testReplaceDate() {
        // YADE1 functionality
        String replaceWhat = "(^.*$)";
        replaceWhat = ".*";

        String replaceWith = "[date:yyyy-MM-dd-HH-mm-ss]";
        execute("/tmp/1abc12def123.TXT", replaceWhat, replaceWith);
    }

    @Ignore
    @Test
    public void testReplaceDateTimeZone() {
        // YADE JS7 New
        String replaceWhat = "(^.*$)";
        replaceWhat = ".*";

        String replaceWith = "[date:yyyy-MM-dd-HH-mm-ss timezone:Etc/UTC]";
        replaceWith = "[date:yyyy-MM-dd-HH-mm-ss timezone:-5:30]";
        execute("/tmp/1abc12def123.TXT", replaceWhat, replaceWith);
    }

    @Ignore
    @Test
    public void testReplaceFilename() {
        // YADE1 functionality
        String replaceWhat = "(^.*$)";
        replaceWhat = ".*";
        execute("/tmp/1abc12def123.TXT", replaceWhat, "[filename:uppercase]");
    }

    @Ignore
    @Test
    public void testReplaceChangePath() {
        // YADE1 functionality
        String replaceWith = "/sub/$1";
        replaceWith = "../$1";
        execute("/tmp/1abc12def123.TXT", "(^.*$)", replaceWith);
    }

    @Ignore
    @Test
    public void testReplaceGroups() {
        // YADE1 functionality
        execute("/tmp/1abc12def123.TXT", "(1)abc(12)def(.*)", "A;BB;CCC");
        // expected: AabcBBdefCCC
    }

    @Ignore
    @Test
    public void testReplacePlaceHolders() {
        // YADE JS7 New: dynamic replacement of all place holders ($1, $2, ...)
        String replaceWhat = "(1)abc(12)def(.*)";
        // regex = "(\\.[a-zA-Z0-9]+)$";
        execute("/tmp/1abc12def123.TXT", replaceWhat, "X$1");
    }

    private static void execute(String yadeProviderFilePath, String replaceWhat, String replaceWith) {

        try {
            LocalProviderArguments pargs = new LocalProviderArguments();
            pargs.applyDefaultIfNullQuietly();
            YADESourceArguments sargs = new YADESourceArguments();
            sargs.applyDefaultIfNullQuietly();
            sargs.setProvider(pargs);
            AYADEProviderDelegator delegator = new YADESourceProviderDelegator(new LocalProvider(new SLF4JLogger(), pargs), sargs);

            YADEProviderFile file = new YADEProviderFile(delegator, yadeProviderFilePath, 0, 0, null, false);
            Optional<YADEFileNameInfo> result = YADEFileReplacementHelper.getReplacementResultIfDifferent(delegator, file.getName(), replaceWhat,
                    replaceWith);
            LOGGER.info("[RESULT]" + (result.isPresent() ? SOSString.toString(result.get()) : "false"));

            // Pattern pattern = Pattern.compile(replaceWhat);
            // Matcher matcher = pattern.matcher(file.getName());
            // if (matcher.find()) {
            // System.out.println("[RESULT][replaceAll]" + file.getName().replaceAll(replaceWhat, replacement));
            // }

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
