package pl.wavesoftware.gasper.internal.maven;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import pl.wavesoftware.eid.exceptions.EidRuntimeException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-04
 */
public class MavenResolver {

    public static final String DEFAULT_POM = "pom.xml";
    public static final String DEFAULT_BUILD_DIR = "target";
    public static final String DEFAULT_PACKAGING = "jar";
    private final Model model;

    public MavenResolver() {
        this(DEFAULT_POM);
    }

    public MavenResolver(String pomfile) {
        FileReader reader;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(pomfile);
            model = mavenReader.read(reader);
        } catch (IOException | XmlPullParserException e) {
            throw new EidRuntimeException("20160304:230502", e);
        }
        model.setPomFile(new File(pomfile));
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
        return dir.resolve(artifact);
    }

    public File getBuildDirectory() {
        String set = model.getBuild().getOutputDirectory();
        File directory = new File(set == null ? DEFAULT_BUILD_DIR : set);
        checkState(directory.isDirectory(), "20160304:230811");
        return directory;
    }

    public String getModelPackaging() {
        return model.getPackaging() == null ? DEFAULT_PACKAGING : model.getPackaging();
    }
}
