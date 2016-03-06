/*
 * Copyright (c) 2016 Wave Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.wavesoftware.gasper.internal.maven;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński
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
    public void testGetBuildArtifactForOtherPom() throws Exception {
        // given
        MavenResolver resolver = new MavenResolver("pom.xml");

        // when
        Path artifact = resolver.getBuildArtifact();

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
        assertThat(directory.toString()).isEqualTo("target");
    }
}
