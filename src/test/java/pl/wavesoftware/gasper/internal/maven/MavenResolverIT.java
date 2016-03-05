package pl.wavesoftware.gasper.internal.maven;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-04
 */
public class MavenResolverIT {

    @Test
    public void testGetBuildArtifact() throws Exception {
        // given
        MavenResolver resolver = new MavenResolver();

        // when
        Path artifact = resolver.getBuildArtifact("jar", "");

        // then
        assertThat(artifact).exists().isRegularFile();
    }

    @Test
    public void testGetBuildDirectory() throws Exception {
        // given
        MavenResolver resolver = new MavenResolver();

        // when
        File directory = resolver.getBuildDirectory();

        // then
        assertThat(directory.getPath()).isEqualTo("target");
    }
}
