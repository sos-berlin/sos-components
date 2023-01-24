package com.sos.yade.commons.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferHistoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferHistoryTest.class);

    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

    @Ignore
    @Test
    public void testMainAsJava() {
        String[] args = new String[] { "--help" };
        TransferHistory.main(args);
    }

    @Ignore
    @Test
    public void testMainAsJavaProcess() throws IOException, Exception {
        Path cp = Paths.get("target/classes");
        Path returnValues = Paths.get("src/test/resources/transfer_history_return_values.txt");

        Map<String, String> env = new HashMap<>();
        env.put("JS7_RETURN_VALUES", returnValues.toAbsolutePath().toString());

        List<String> args = new ArrayList<>();
        args.add("--xxx");
        args.add("--dry-run");
        // args.add("--target-host=my_target_host --target-port=1 --target-account=ta --target-protocol=webdav");
        // args.add("--target-host=ftp://test@testhost:1234");
        // args.add("--transfer-file=1.txt --operation=remove");
        // args.add("--transfer-file=1.txt --target-host --start-time=\"2023-01-23 12:00:00+0200\" -h --xxx -display-args -display-result --operation=getlist");

        // args.add(
        // "-display-args -display-result --source-account=jobscheduler --target-account=jobscheduler --target-host=agent-2-0-standalone --target-protocol=local
        // --transfer-file=\"/tmp/files_345/source/a/a1.txt,/tmp/files_345/target/a/a1.txt,0\"
        // --transfer-file=\"/tmp/files_345/source/a/a2.txt,/tmp/files_345/target/a/a2.txt,0,cp: can't stat 'file_does_not_exist': No such file or directory\"
        // --transfer-file=\"/tmp/files_345/source/a/a3.txt,/tmp/files_345/target/a/a3.txt,0\" --error=\"transfer failed with errors: 1\"");

        // args.add("--transfer-file=1.txt,2.txt");
        // args.add("--transfer-file=1.txt,2.txt,10");

        executeCommand(String.format("java -classpath \"%s\" %s %s", cp.toAbsolutePath(), TransferHistory.class.getName(), String.join(" ", args)),
                env);
    }

    @Ignore
    @Test
    public void testURL() {
        String s = "ftp://test_user@source_host:123";
        s = "file:///test/1.txt";
        try {
            URL u = new URL(s);
            LOGGER.info("UserInfo=" + u.getUserInfo());
            LOGGER.info("Host=" + u.getHost());
            LOGGER.info("Port=" + u.getPort());
            LOGGER.info("Protocol=" + u.getProtocol());
            LOGGER.info("Path=" + u.getPath());

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private void executeCommand(String cmd, Map<String, String> env) {
        LOGGER.info("[cmd]" + cmd);

        try {
            Charset charset = Charset.forName("UTF-8");

            StringBuilder stdOut = new StringBuilder();
            StringBuilder stdErr = new StringBuilder();

            ProcessBuilder pb = new ProcessBuilder(getCommand(cmd));
            if (env != null && env.size() > 0) {
                pb.environment().putAll(env);
            }

            final Process p = pb.start();
            CompletableFuture<Boolean> out = redirect(p.getInputStream(), stdOut::append, charset);
            CompletableFuture<Boolean> err = redirect(p.getErrorStream(), stdErr::append, charset);

            p.waitFor();
            out.join();
            err.join();

            if (stdOut.length() > 0) {
                LOGGER.info("[stdout]" + stdOut);
            }
            if (stdErr.length() > 0) {
                LOGGER.info("[stderr]" + stdErr);
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private CompletableFuture<Boolean> redirect(final InputStream is, final Consumer<String> consumer, final Charset charset) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(is, charset); BufferedReader br = new BufferedReader(isr);) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line + System.lineSeparator());
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    private String[] getCommand(String script) {
        String[] command = new String[2 + 1];
        if (IS_WINDOWS) {
            command[0] = System.getenv("comspec");
            command[1] = "/C";
            command[2] = script;
        } else {
            String shell = System.getenv("SHELL");
            if (shell == null) {
                shell = "/bin/sh";
            }
            command[0] = shell;
            command[1] = "-c";
            command[2] = script;
        }
        return command;
    }

}
