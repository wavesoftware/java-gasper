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

package pl.wavesoftware.gasper;

import com.mashape.unirest.http.Unirest;
import org.junit.Test;
import pl.wavesoftware.eid.utils.EidPreconditions;
import pl.wavesoftware.gasper.internal.HttpEndpoint;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.wavesoftware.eid.utils.EidPreconditions.tryToExecute;

/**
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
public class GasperBuilderTest {

    @Test
    public void testBuild() throws Exception {
        GasperBuilder builder = new GasperBuilder();
        int port = 11909;
        String webContext = "/test";
        String systemPropertyForPort = "swarm.http.port";
        Gasper gasper = builder.silentGasperMessages()
            .usingSystemPropertyForPort(systemPropertyForPort)
            .withSystemProperty("swarm.context.path", webContext)
            .withSystemProperty(systemPropertyForPort, String.valueOf(port))
            .withJVMOptions("-server", "-Xms1G", "-Xmx1G", "-XX:+UseConcMarkSweepGC")
            .withMaxStartupTime(100)
            .withMaxDeploymentTime(20)
            .withEnvironmentVariable("jdbc.password", "S3CreT!1")
            .withTestApplicationLoggingOnConsole()
            .usingPomFile(Paths.get("pom.xml"))
            .withArtifactPackaging("jar")
            .waitForWebContext(webContext)
            .withArtifactClassifier("swarm")
            .usingWebContextChecker(GasperBuilderTest::checkContext)
            .withPort(port)
            .build();

        assertThat(gasper).isNotNull();
    }

    private static Boolean checkContext(HttpEndpoint endpoint) {
        return tryToExecute((EidPreconditions.UnsafeSupplier<Boolean>) () ->
            Unirest.get(endpoint.fullAddress()).asBinary().getStatus() == 200, "20160305:215916");
    }
}
