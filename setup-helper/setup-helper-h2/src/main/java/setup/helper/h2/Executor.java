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
     * 1) Relative path of the source project<br/>
     * -- sos-joc/sos-joc-db/src/main/java<br/>
     * 2) Path of the generated sources directory of the current project - target/generated-sources<br/>
     * -- ${generated.sources.dir} (${project.build.directory}/generated-sources)<br/>
     * 
     * @param args
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new Exception("[missing arguments]sourceProject/generatedSources");
        }
        Path sourceProject = Paths.get(args[0]);// without toAbsolutePath
        Path currentProjectGeneratedSources = Paths.get(args[1]).toAbsolutePath();

        Path source = getSourceDir(currentProjectGeneratedSources, sourceProject);
        Path target = currentProjectGeneratedSources;

        // is not really necessary, since the target directory is deleted/recreated with Maven clean etc.
        prepareTarget(target);

        System.out.println("[source]" + source);
        System.out.println("[target]" + target);

        Files.walk(source).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).forEach(path -> processDBItemFile(source,
                path, target));

    }

    private static Path getSourceDir(Path currentProjectGeneratedSources, Path sourceProject) throws Exception {
        // <sos-components> / setup-helper / setup-helper-h2 / target / generated-sources
        // build environment uses e.g.:
        // -- ... / 020.sos-components.v2.7.centos8 / setup-helper / setup-helper-h2 /...
        Path dir = currentProjectGeneratedSources.getParent();
        while (dir != null) {
            Path sp = dir.resolve(sourceProject);
            if (Files.exists(sp)) {
                return sp;
            }
            dir = dir.getParent();
        }
        throw new Exception("[currentProjectGeneratedSources=" + currentProjectGeneratedSources + "][sourceProject=" + sourceProject
                + "]sourceProject cannot be found in any parent path of currentProjectGeneratedSources");
    }

    private static void prepareTarget(Path dir) throws Exception {
        SOSPath.cleanupDirectory(dir);
        Files.createDirectories(dir);
    }

    private static void processDBItemFile(Path sourceBase, Path sourcePath, Path targetBase) {
        try {
            String content = SOSPath.readFile(sourcePath);
            if (content.contains(TO_REPLACE)) {
                content = content.replace(TO_REPLACE, REPLACEMENT);

                Path targetPath = targetBase.resolve(sourceBase.relativize(sourcePath));
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, content.getBytes());

                System.out.println("[processed]" + targetPath);
            }
        } catch (Throwable e) {
            new RuntimeException(e);
        }
    }
}
