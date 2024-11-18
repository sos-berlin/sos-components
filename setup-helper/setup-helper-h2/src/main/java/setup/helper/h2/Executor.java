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
     * -- sos-joc/sos-joc-db/src/main/java/com<br/>
     * 
     * @param args
     * @throws Exception */
    public static void main(String[] args) throws Exception {
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
        // <sos-components> / setup-helper / setup-helper-h2
        // build environment uses e.g. ../020.sos-components.v2.7.centos8/setup-helper/setup-helper-h2
        Path mainProjectDir = currentProject.getParent();
        if (mainProjectDir == null) {
            throw new Exception("[currentProject=" + currentProject + "][1]parent is null");
        }
        mainProjectDir = mainProjectDir.getParent();
        if (mainProjectDir == null) {
            throw new Exception("[currentProject=" + currentProject + "][2]parent is null");
        }

        Path sourceProjectDir = mainProjectDir.resolve(sourceProject);
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
