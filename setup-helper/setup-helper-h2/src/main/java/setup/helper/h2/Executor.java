package setup.helper.h2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.commons.util.SOSPath;

/** The Executor looks for TO_REPLACE in a source project (configured as the second argument in the project pom.xml)<br/>
 * and copies the found files in the same structure to the current project<br/>
 * , replaces TO_REPLACE with REPLACEMENT<br/>
 * , compiles and creates the resulting .jar file. */
public class Executor {

    private static final String TO_REPLACE = "@ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)";
    private static final String REPLACEMENT = "@ColumnTransformer(write = \"" + SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_H2 + "\")";

    /** Arguments:<br/>
     * 1) Path of the base directory of the current project<br/>
     * -- ${project.basedir}<br/>
     * 2) Relative path of the source project (including the root package)<br/>
     * -- sos-components/sos-joc/sos-joc-db/src/main/java/com<br/>
     * 
     * @param args
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        // <argument>sos-components/sos-joc/sos-joc-db/src/main/java/com</argument>
        if (args.length < 2) {
            throw new Exception("[missing arguments]currentProject/sourceProject");
        }
        Path currentProject = Paths.get(args[0]).toAbsolutePath();
        Path sourceProject = Paths.get(args[1]);// without toAbsolutePath

        Path source = getSourceDir(currentProject, sourceProject);
        Path target = getTargetDir(currentProject);
        Files.createDirectories(target);

        Files.walk(source).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).forEach(path -> processDBItemFile(source,
                path, target));

    }

    private static Path getSourceDir(Path currentProject, Path sourceProject) throws Exception {
        Path mainProjectName = sourceProject.getName(0);
        Path mainProjectDir = findMainProjectDirectory(currentProject, mainProjectName);
        if (mainProjectDir == null) {
            throw new Exception("[main project='" + mainProjectName + "' not found in the current path]" + currentProject);
        }

        Path sourceProjectDir = mainProjectDir.getParent().resolve(sourceProject);
        if (!Files.exists(sourceProjectDir)) {
            throw new Exception("[source][" + sourceProjectDir + "]not found");
        }
        return sourceProjectDir;
    }

    public static Path getTargetDir(Path currentProject) throws Exception {
        Path dir = currentProject.resolve("src/main/java/com");
        SOSPath.deleteIfExists(dir);
        return dir;
    }

    private static Path findMainProjectDirectory(Path currentDir, Path mainDirName) {
        Path dir = currentDir;
        while (dir != null) {
            Path fn = dir.getFileName();
            System.out.println("[findMainProjectDirectory][" + dir + "]" + fn);
            if (fn != null && fn.equals(mainDirName)) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static void processDBItemFile(Path sourceBase, Path sourcePath, Path targetBase) {
        try {
            String content = SOSPath.readFile(sourcePath);
            if (content.contains(TO_REPLACE)) {
                content = content.replace(TO_REPLACE, REPLACEMENT);

                Path targetPath = targetBase.resolve(sourceBase.relativize(sourcePath));
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, content.getBytes());
            }
        } catch (Throwable e) {
            new RuntimeException(e);
        }
    }
}
