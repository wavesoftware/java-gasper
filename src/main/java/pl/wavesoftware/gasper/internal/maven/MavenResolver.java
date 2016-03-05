package pl.wavesoftware.gasper.internal.maven;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static pl.wavesoftware.eid.utils.EidPreconditions.*;


/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-04
 */
public class MavenResolver {

    public static final String DEFAULT_POM = "./pom.xml";
    public static final String DEFAULT_BUILD_DIR = "target";
    public static final String DEFAULT_PACKAGING = "jar";
    private static final Path CURRENT_DIR = Paths.get("./");
    private final Model model;
    private final Path pomDirectory;

    public MavenResolver() {
        this(DEFAULT_POM);
    }

    public MavenResolver(String pomfile) {
        this(Paths.get(pomfile));
    }

    public MavenResolver(Path pomfile) {
        checkArgument(pomfile.toFile().isFile(), "20160305:181005");
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        model = tryToExecute((UnsafeSupplier<Model>) () -> {
            FileReader reader = new FileReader(pomfile.toString());
            return mavenReader.read(reader);
        }, "20160305:203232");
        checkNotNull(model, "20160305:203551").setPomFile(pomfile.toFile());
        pomDirectory = pomfile.getParent() == null ? CURRENT_DIR : pomfile.getParent();
        checkArgument(pomDirectory.toFile().isDirectory(), "20160305:181211");
    }

    public Path getBuildArtifact() {
        return getBuildArtifact("", "");
    }

    public Path getBuildArtifact(String classifier) {
        return getBuildArtifact("", classifier);
    }

    public Path getBuildArtifact(String packaging, String classifier) {
        String artifact;
        Path dir = getBuildDirectory().toPath();
        String pack = Objects.equals(packaging, "") ? getModelPackaging() : packaging;
        if (Objects.equals(classifier, "")) {
            artifact = String.format("%s-%s.%s",
                model.getArtifactId(), model.getVersion(), pack);
        } else {
            artifact = String.format("%s-%s-%s.%s",
                model.getArtifactId(), model.getVersion(), classifier, pack);
        }
        Path artifactPath = dir.resolve(Paths.get(artifact));
        checkState(artifactPath.toFile().isFile(), "20160305:181432", "Is not a file: %s", artifactPath);
        checkState(artifactPath.toFile().canRead(), "20160305:181456", "Can't read file: %s", artifactPath);
        return artifactPath;
    }

    public File getBuildDirectory() {
        String set = model.getBuild().getOutputDirectory();
        Path directory = pomDirectory.resolve(Paths.get(set == null ? DEFAULT_BUILD_DIR : set));
        checkState(directory.toFile().isDirectory(), "20160304:230811");
        return directory.normalize().toFile();
    }

    public String getModelPackaging() {
        return model.getPackaging() == null ? DEFAULT_PACKAGING : model.getPackaging();
    }
}
