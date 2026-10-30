package com.sos.commons.credentialstore.keepass;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;

public class SOSKeePassDatabaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassDatabaseTest.class);
    private static final String DIR = "src/test/resources";

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void getMainUriKeePass1CompositeKeyTest() throws Exception {
        String uri = "cs://server/SFTP/my_server@user?file=" + DIR + "/keepassX-test.kdb";
        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getMainUriPropertyTest() throws Exception {
        String databaseName = "kdbx-p-f.kdbx";

        String uri = "cs://server/SFTP/my_server@my test?file=" + DIR + "/" + databaseName + "&password=test";
        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getMainUriAttachmentTest() throws Exception {
        String databaseName = "kdbx-p-f.kdbx";

        String uri = "cs://server/SFTP/my_server@attachment?file=" + DIR + "/" + databaseName + "&password=test";
        uri = "cs://server/SFTP/my_server@cloud.jpg?attachment=1&file=" + DIR + "/" + databaseName + "&password=test";
        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getDefaultKeyFileTest() throws Exception {
        String databaseName = "kdbx-p-f.kdbx";
        Path key = SOSKeePassDatabase.getDefaultKeyFile(Paths.get(DIR).resolve(databaseName));
        LOGGER.info(SOSKeePassDatabase.getFilePath(key));
    }

    @Ignore
    @Test
    public void load255() throws Exception {
        Path kpf = Paths.get(DIR).resolve("keepass_2.55-p-f.kdbx");
        Path kpk = Paths.get(DIR).resolve("keepass_2.55-p-f.keyx");

        Instant start = Instant.now();
        SOSKeePassDatabase kb = new SOSKeePassDatabase(kpf);
        kb.load("test", kpk);
        LOGGER.info("[load]" + SOSDate.getDuration(start, Instant.now()));

        Entry e = kb.getEntryByPath("/server/SFTP/my_server");
        // LOGGER.info(SOSString.toString(e));
        LOGGER.info("[total]" + SOSDate.getDuration(start, Instant.now()));
        LOGGER.info("[username]" + e.getUsername());
    }

    @Ignore
    @Test
    public void loadMultiThreads() throws Exception {
        LOGGER.info("[loadMultiThreads]start");

        int threads = 5;

        List<Supplier<Integer>> tasks = new ArrayList<>();

        for (int i = 1; i <= threads; i++) {
            final int nr = i;
            Supplier<Integer> task = new Supplier<Integer>() {

                @Override
                public Integer get() {
                    Instant start = Instant.now();
                    String logPrefix = "[" + nr + "]";
                    try {
                        Path kpf = Paths.get(DIR).resolve("keepass_2.55-p-f.kdbx");
                        Path kpk = Paths.get(DIR).resolve("keepass_2.55-p-f.keyx");

                        SOSKeePassDatabase kb = new SOSKeePassDatabase(kpf);
                        kb.load("test", kpk);
                        LOGGER.info(logPrefix + "[load]" + SOSDate.getDuration(start, Instant.now()));

                        Entry e = kb.getEntryByPath("/server/SFTP/my_server");
                        LOGGER.info(logPrefix + "[username]" + e.getUsername());
                    } catch (Throwable e) {
                        LOGGER.error(logPrefix + e.toString(), e);
                    } finally {
                        LOGGER.info(logPrefix + "[total]" + SOSDate.getDuration(start, Instant.now()));
                    }
                    return 1;
                }
            };
            tasks.add(task);
        }

        ExecutorService es = Executors.newFixedThreadPool(tasks.size());
        List<CompletableFuture<Integer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        es.shutdown();
        LOGGER.info("[loadMultiThreads]end");
    }

    @Ignore
    @Test
    public void loadBinaryKey() throws Exception {
        Path kpf = Paths.get(DIR).resolve("keepass_2.50-p-binary.key.kdbx");
        Path kpk = Paths.get(DIR).resolve("keepass_2.50-p-binary.key.png");

        Instant start = Instant.now();
        SOSKeePassDatabase kb = new SOSKeePassDatabase(kpf);
        kb.load("test", kpk);
        LOGGER.info(String.format("[load]%s", SOSDate.getDuration(start, Instant.now())));

        Entry e = kb.getEntryByPath("/server/SFTP/my_server");

        Path copy = kpf.getParent().resolve(kpf.getFileName() + ".copy.kdbx");
        SOSPath.deleteIfExists(copy);

        kb.getHandler().setProperty(e, "custom_field", "xxx");
        kb.getHandler().createEntry("/server/new_path/new_sub_path/new_entry");
        kb.saveAs(copy);

        // kb.exportAttachment2File(e, Paths.get(DIR).resolve("logo.png"));
        LOGGER.info(String.format("[total]%s", SOSDate.getDuration(start, Instant.now())));
        LOGGER.info(String.format("%s", SOSString.toString(e)));
        LOGGER.info(String.format("%s", e.getProperty("custom_field")));
    }

    @Ignore
    @Test
    public void testWithCustomData() throws Exception {
        // see https://change.sos-berlin.com/browse/YADE-539
        Path kpf = Paths.get(DIR).resolve("keepass-p-custom.data.kdbx");

        Instant start = Instant.now();
        SOSKeePassDatabase kb = new SOSKeePassDatabase(kpf);
        kb.load("12345");
        LOGGER.info(String.format("[load]%s", SOSDate.getDuration(start, Instant.now())));

        Entry e = kb.getEntryByPath("/YADE/yade/myEntry");

        Path copy = kpf.getParent().resolve(kpf.getFileName() + ".copy.kdbx");
        SOSPath.deleteIfExists(copy);

        kb.getHandler().setProperty(e, "custom_field", "xxx");
        kb.getHandler().createEntry("server/new_path/new_sub_path/new_entry");
        kb.saveAs(copy);

        // kb.exportAttachment2File(e, Paths.get(DIR).resolve("logo.png"));
        LOGGER.info(String.format("[total]%s", SOSDate.getDuration(start, Instant.now())));
        LOGGER.info(String.format("%s", SOSString.toString(e)));
        LOGGER.info(String.format("%s", e.getProperty("custom_field")));
    }
}
