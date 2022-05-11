package com.sos.js7.converter.commons;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Deployable & releasable objects<br/>
 * <br/>
 * Deployable: see com.sos.inventory.model.deploy.DeployType<br/>
 * Releasable: see com.sos.joc.model.inventory.common.ConfigurationType<br/>
 * -------------- Calendar(WORKINGDAYSCALENDAR,NONWORKINGDAYSCALENDAR), SCHEDULE, INCLUDESCRIPT?<br/>
 */
public class JS7ExportObjects<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ExportObjects.class);

    private static final int UNIQUE_NAME_MAX_VALUE = 9999;
    private static final int UNIQUE_NAME_MAX_DIGITS = String.valueOf(UNIQUE_NAME_MAX_VALUE).length();
    private static final String UNIQUE_NAME_DELIMITER = "-";
    private static final String UNIQUE_NAME_REGEX = ".*" + UNIQUE_NAME_DELIMITER + "[0-9]{" + UNIQUE_NAME_MAX_DIGITS + "}$";

    private List<JS7ExportObject> items;

    // TODO temporary solution - unique should be checked before write to file ...
    private boolean useUniquePath = false;

    public JS7ExportObjects() {
        this.items = new ArrayList<>();
    }

    public JS7ExportObject addItem(Path path, T o) {
        JS7ExportObject eo = new JS7ExportObject(o, path, getUniquePath(path));
        items.add(eo);
        return eo;
    }

    public JS7ExportObject addOrReplaceItem(Path path, T o) {
        JS7ExportObject newEo = new JS7ExportObject(o, path, getUniquePath(path));
        JS7ExportObject oldEo = items.stream().filter(e -> e.getOriginalPath().getPath().equals(path)).findAny().orElse(null);
        if (oldEo == null) {
            items.add(newEo);
        } else {
            items.set(items.indexOf(oldEo), newEo);
        }
        return newEo;
    }

    public List<JS7ExportObject> getItems() {
        return items;
    }

    // TODO max length 255?
    private Path getUniquePath(Path path) {
        JS7ExportObject eo = find(path);
        if (eo == null) {
            return path;
        }

        LOGGER.debug(String.format("[%s]already used", path));
        if (!useUniquePath) {
            return path;
        }

        int i = 0;
        String name = getName(path);
        String namePrefix = name;
        if (name.matches(UNIQUE_NAME_REGEX)) {
            // myWorkflow-0023: start position of 0023
            int startPosUniqueDigits = name.length() - UNIQUE_NAME_MAX_DIGITS;
            // myWorkflow-0023: i=23
            i = getUniqueIndex(name, startPosUniqueDigits);
            // myWorkflow-0023: namePrefix=myWorkflow
            namePrefix = name.substring(0, startPosUniqueDigits - UNIQUE_NAME_DELIMITER.length());
            LOGGER.debug(String.format("    [%s][name=%s]matches, namePrefix=%s", path, name, namePrefix));
        } else {
            LOGGER.debug(String.format("    [%s][name=%s]not matches", path, name));
        }

        boolean run = true;
        while (run) {
            i++;
            path = getUniquePath(path, namePrefix, i);
            eo = find(path);
            if (eo == null) {
                return path;
            }
            if (i > UNIQUE_NAME_MAX_VALUE) {
                run = false;
            }
        }
        return path;
    }

    private int getUniqueIndex(String name, int startPosUniqueDigits) {
        String s = name.substring(startPosUniqueDigits).replace(".json", "").replace("0", "");
        return Integer.parseInt(s);
    }

    /** @return e.g. myWorkflow-0001.json */
    private Path getUniquePath(Path originalPath, String namePrefix, int counter) {
        StringBuilder sb = new StringBuilder(namePrefix);
        sb.append(UNIQUE_NAME_DELIMITER);
        // sb.append(String.format("%0" + UNIQUE_NAME_MAX_DIGITS + "x", counter));
        sb.append(String.format("%0" + UNIQUE_NAME_MAX_DIGITS + "d", counter));
        sb.append(".json").toString();

        Path parent = originalPath.getParent();
        return parent == null ? Paths.get(sb.toString()) : parent.resolve(sb.toString());
    }

    private JS7ExportObject find(Path path) {
        return this.getItems().stream().filter(o -> o.getUniquePath().getName().equals(getName(path))).findFirst().orElse(null);
    }

    private String getName(Path path) {
        if (path == null) {
            return null;
        }
        return path.getFileName().toString().trim().replace(".json", "");
    }

    public class JS7ExportObject {

        private final T object;
        private final JS7ExportObjectPath originalPath;
        private final JS7ExportObjectPath uniquePath;

        private JS7ExportObject(T object, Path originalPath, Path uniquePath) {
            this.object = object;
            this.originalPath = new JS7ExportObjectPath(originalPath);
            this.uniquePath = new JS7ExportObjectPath(uniquePath);

        }

        public T getObject() {
            return object;
        }

        public JS7ExportObjectPath getOriginalPath() {
            return originalPath;
        }

        public JS7ExportObjectPath getUniquePath() {
            return uniquePath;
        }

    }

    public class JS7ExportObjectPath {

        private final Path path;
        private final String name;

        private JS7ExportObjectPath(Path path) {
            this.path = normalize(path);
            this.name = JS7ExportObjects.this.getName(path);
        }

        private Path normalize(Path path) {
            if (path == null) {
                return null;
            }
            String s = path.toString().replace("\\", "/");
            if (s.startsWith("/")) {
                return Paths.get(s.substring(1));
            }
            return path;
        }

        public Path getPath() {
            return path;
        }

        public String getName() {
            return name;
        }
    }
}
