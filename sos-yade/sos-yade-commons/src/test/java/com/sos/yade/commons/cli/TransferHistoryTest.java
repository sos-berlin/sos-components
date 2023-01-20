package com.sos.yade.commons.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
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

import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

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
        // args.add("--xxx");
        // args.add("--target-host=my_target_host --target-port=1 --target-account=ta --target-protocol=webdav");
        // args.add("--target-host=ftp://test@testhost:1234");
        // args.add("--transfer-file=1.txt --operation=remove");
        args.add("--transfer-file=1.txt --operation=getlist");
        // args.add("--transfer-file=1.txt,2.txt");
        // args.add("--transfer-file=1.txt,2.txt,10");

        executeCommand(String.format("java -classpath \"%s\" %s %s", cp.toAbsolutePath(), TransferHistory.class.getName(), String.join(" ", args)),
                env);

        if (Files.exists(returnValues)) {
            YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<>();
            String serialized = new String(Files.readAllBytes(returnValues.toAbsolutePath()));
            serialized = serialized.substring("yade_return_values=".length());

            YadeTransferResult r = serializer.deserialize(serialized);

            LOGGER.info("");
            LOGGER.info("Operation=" + r.getOperation());
            LOGGER.info("StartTime=" + r.getStart());
            LOGGER.info("EndTime=" + r.getEnd());
            LOGGER.info("ErrorMessage=" + r.getErrorMessage());
            LOGGER.info("");

            printProtocol("Source---", r.getSource());
            printProtocol("Target---", r.getTarget());
            LOGGER.info("");

            printEntries("Entries---", r.getEntries());
        } else {
            LOGGER.info(returnValues + " not found");
        }
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

    private void printProtocol(String header, YadeTransferResultProtocol p) {
        if (p != null) {
            LOGGER.info(header);
            LOGGER.info(String.format("   protocol=%s,host=%s,port=%s,account=%s", p.getProtocol(), p.getHost(), p.getPort(), p.getAccount()));
        }
    }

    private void printEntries(String header, List<YadeTransferResultEntry> l) {
        if (l != null && l.size() > 0) {
            LOGGER.info(header);
            int i = 0;
            for (YadeTransferResultEntry e : l) {
                i++;
                LOGGER.info(String.format("   %s)source=%s,target=%s,size=%s,error=%s,state=%s", i, e.getSource(), e.getTarget(), e.getSize(), e
                        .getErrorMessage(), e.getState()));
            }
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
