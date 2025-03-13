package com.sos.commons.util;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSPathUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPathUtilTest.class);

    @Ignore
    @Test
    public void testPaths() throws Exception {
        Path source = Paths.get("./src/test/resources/log4j2.xml");
        LOGGER.info("[" + source + "]size=" + SOSPath.getFileSize(source));
        LOGGER.info("[" + source.getParent() + "]size=" + SOSPath.getFileSize(source.getParent()));
        LOGGER.info("basename=" + SOSPath.getBasename(source));
        LOGGER.info("withoutExtension=" + SOSPath.getFileNameWithoutExtension(source));

        paths("/");
        paths("/xyz");
        paths("C:");
        paths("C://");
        paths("C://xyz");
        paths("C://xyz/tmp");

        LOGGER.info("--------------------------------");
        appendPath("/tmp", "/xyz/1.txt");
        appendPath("/", "/xyz/1.txt");
        appendPath("", "/xyz/1.txt");
    }

    @Ignore
    @Test
    public void testTopDeepestLevelPaths() throws Exception {
        List<String> stringPaths = new ArrayList<>();
        stringPaths.add("/tmp/x/1/2");
        stringPaths.add("/tmp/x/1/1/");
        stringPaths.add("/tmp/x/2");
        stringPaths.add("/tmp/x/1");
        stringPaths.add("/tmp/x/2/1");
        stringPaths.add("/tmp/x/2/ä/nbg");
        stringPaths.add("/tmp/x/2/ä");
        stringPaths.add("/tmp/y/2/ä");
        stringPaths.add("/var/1");

        LOGGER.info("[SOSPathUtil][String]--------------");
        LOGGER.info("    [input][stringPaths]" + stringPaths);
        LOGGER.info("    [selectTopLevelPaths][stringPaths]" + SOSPathUtil.selectTopLevelPaths(stringPaths, "/"));
        LOGGER.info("    [selectDeepestLevelPaths]" + SOSPathUtil.selectDeepestLevelPaths(stringPaths, "/"));

        List<Path> paths = stringPaths.stream().map(p -> Path.of(p)).collect(Collectors.toList());
        LOGGER.info("[SOSPath][Path]--------------");
        LOGGER.info("    [input][paths]" + paths);
        LOGGER.info("    [selectTopLevelPaths][stringPaths]" + SOSPath.selectTopLevelPaths(paths));
        LOGGER.info("    [selectDeepestLevelPaths]" + SOSPath.selectDeepestLevelPaths(paths));
    }

    @Ignore
    @Test
    public void testNormalize() throws Exception {
        List<String> stringPaths = new ArrayList<>();
        stringPaths.add("folder/./file.txt");
        stringPaths.add("/home/user/./docs/../a");
        stringPaths.add("a/b/./c/../d");
        stringPaths.add("/../");
        stringPaths.add("a/b/c/../../../");
        stringPaths.add("/a/../b/./c/.");
        stringPaths.add("C:\\\\Users\\test\\..\\file.txt");
        stringPaths.add("C:/Users/test/../file.txt");
        // Windows OpenSSH
        stringPaths.add("/C:/Users/test/../file.txt");
        // stringPaths.add("C:/Users/test/../file.txt");
        stringPaths.add("C:\\..\\Windows\\System32");
        // UNC
        stringPaths.add("\\\\server\\share\\x\\..\\1.txt");
        // URI
        stringPaths.add("https://server/x/../1.txt");

        for (String path : stringPaths) {
            LOGGER.info("[" + path + "]");
            LOGGER.info("    [SOSPathUtil.normalize]" + SOSPathUtil.normalize(path));
            LOGGER.info("    [Path.normalize]       " + normalizeToString(path));
        }

    }

    @Ignore
    @Test
    public void testURI() throws Exception {
        String baseURL = "http://example.com/test/";
        URI uri = new URI(baseURL);
        LOGGER.info("[URI]" + uri.toString());
        String toResolve = "http://example.com/test/1/a b/2";// manual .replace(" ", "%20");
        try {
            LOGGER.info(uri.resolve(toResolve).normalize().toString());
        } catch (Exception e) {
            LOGGER.error("[URI][resolve][" + uri + "]" + e);
        }

        URL url = new URL(baseURL);
        LOGGER.info("[URL]" + url.toString());
        try {
            LOGGER.info(url.toURI().resolve(new URI(toResolve)).normalize().toString());
        } catch (Exception e) {
            LOGGER.error("[URL][resolve][" + uri + "]" + e);
        }

        URI uri2 = new URI(null, toResolve, null);
        LOGGER.info("[URI2][resolve][" + uri2 + "]" + uri.resolve(uri2).normalize().toString());
    }

    @Ignore
    @Test
    public void testPath() throws Exception {
        LOGGER.info("[SOSPath.toAbsoluteNormalizedPath]" + SOSPath.toAbsoluteNormalizedPath("/test").toString());
        LOGGER.info("[Path.normalize]" + Path.of("/test/2/../1").normalize().toString());
        LOGGER.info("[Path.normalize]" + Path.of("\\\\server/2/../1").normalize().toString());
    }

    private String normalizeToString(String path) {
        try {
            return Path.of(path).normalize().toString();
        } catch (Throwable e) {
            return e.toString();
        }
    }

    private void paths(String path) {
        LOGGER.info("[" + path + "]");
        LOGGER.info("    [SOSPathUtil.getName=" + SOSPathUtil.getName(path) + "][Path.getFileName=" + Path.of(path).getFileName() + "]");
        LOGGER.info("    [SOSPathUtil.getParentPath=" + SOSPathUtil.getParentPath(path) + "][Path.getParent=" + Path.of(path).getParent() + "]");
    }

    private void appendPath(String path1, String path2) {
        LOGGER.info("[" + path1 + "]append[" + path2 + "]" + SOSPathUtil.appendPath(path1, path2));
    }
}
