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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import pl.wavesoftware.eid.utils.EidPreconditions;
import pl.wavesoftware.gasper.internal.Executor;
import pl.wavesoftware.gasper.internal.Logger;
import pl.wavesoftware.gasper.internal.Settings;
import pl.wavesoftware.gasper.internal.maven.MavenResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static pl.wavesoftware.eid.utils.EidPreconditions.tryToExecute;

/**
 * <h2>About</h2>
 * Gasper is a very simple integration testing JUnit harness for <code>java -jar</code> servers like <a href="http://wildfly-swarm.io/">WildFly Swarm</a> and <a href="http://projects.spring.io/spring-boot/">Spring Boot</a>.
 * <p>
 * Gasper provides a simple to use JUnit {@link TestRule} that can be used to build integration tests with simple apps, like REST micro-services. You can configure Gasper easily with a builder interface. Gasper will start the application before test class and stop it after tests completes.
 * <p>
 * Gasper supports currently only <a href="https://maven.apache.org/">Maven</a>. The <code>pom.xml</code> file is used to read project configuration achieving zero configuration operation.
 *
 * <h3>Usage</h3>
 *
 * Gasper utilize your packaged application. It  It means it should be used in integration tests that run after application is being packaged by build tool (Maven). Add this code to your <code>pom.xml</code> file (if you didn't done that before):
 *
 * <pre>
 * &lt;build&gt;
 * [..]
 * &lt;plugins&gt;
 * [..]
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
 *   &lt;artifactId&gt;maven-failsafe-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;2.19.1&lt;/version&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;integration-test&lt;/goal&gt;
 *         &lt;goal&gt;verify&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * [..]
 * &lt;/plugins&gt;
 * [..]
 * &lt;/build&gt;
 * </pre>
 *
 * Place your integration tests in classes that ends with <code>*IT</code> or <code>*ITest</code>.
 *
 * <h4>WildFly Swarm default configuration</h4>
 *
 * <pre>
 * &#064;ClassRule
 * public static Gasper gasper = Gasper.configurations()
 *   .wildflySwarm()
 *   .build();
 * </pre>
 *
 * <h4>Spring Boot default configuration</h4>
 *
 * <pre>
 * &#064;ClassRule
 * public static Gasper gasper = Gasper.configurations()
 *   .springBoot()
 *   .build();
 * </pre>
 * <p>
 * Before running {@link GasperBuilder#build()} method, you can reconfigure those default configurations to your needs.
 *
 * <h4>Example test method (Unirest + JSONAssert)</h4>
 *
 * Gasper is best to use with libraries like <a href="http://unirest.io/java.html">Unirest</a> for fetching
 * data and asserting HTTP/S statuses and <a href="https://github.com/marcingrzejszczak/jsonassert">JSON
 * Assert</a> to validate correctness of JSON output for REST services.
 *
 * <pre>
 * &#064;Test
 * public void testGetRoot() throws UnirestException {
 *   // given
 *   String address = gasper.getAddress(); // Address to deployed app, running live on random port
 *   String expectedMessage = "WildFly Swarm!";
 *   // when
 *   HttpResponse&lt;String&gt; response = Unirest.get(address).asString();
 *   // then
 *   assertThat(response.getStatus()).isEqualTo(200);
 *   assertThat(response.getBody()).field("hello").isEqualTo(expectedMessage); // JSON Assert
 * }
 * </pre>
 *
 * <h4>Additional configuration</h4>
 *
 * To configure Gasper use {@link GasperBuilder} interface, for ex.:
 *
 * <pre>
 * private final int port = 11909;
 * private final String webContext = "/test";
 * private final String systemPropertyForPort = "swarm.http.port";
 *
 * &#064;ClassRule
 * public static Gasper gasper = Gasper.configure()
 *   .silentGasperMessages()
 *   .usingSystemPropertyForPort(systemPropertyForPort)
 *   .withSystemProperty("swarm.context.path", webContext)
 *   .withSystemProperty(systemPropertyForPort, String.valueOf(port))
 *   .withJVMOptions("-server", "-Xms1G", "-Xmx1G", "-XX:+UseConcMarkSweepGC")
 *   .withMaxStartupTime(100)
 *   .withMaxDeploymentTime(20)
 *   .withEnvironmentVariable("jdbc.password", "S3CreT!1")
 *   .withTestApplicationLoggingOnConsole()
 *   .usingPomFile(Paths.get("pom.xml"))
 *   .withArtifactPackaging("jar")
 *   .waitForWebContext(webContext)
 *   .withArtifactClassifier("swarm")
 *   .usingWebContextChecker(GasperBuilderTest::checkContext)
 *   .withPort(port)
 *   .build();
 * </pre>
 *
 * <h4>Requirements</h4>
 *
 * <ul>
 *     <li>Java 8</li>
 *     <li>Maven 3</li>
 * </ul>
 *
 * @author Krzysztof Suszyński &lt;krzysztof suszynski@wavesoftware.pl&gt;
 * @since 2016-03-04
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Gasper implements TestRule {

    public static final int DEFAULT_PORT_AVAILABLE_MAX_SECONDS = 60;
    public static final int DEFAULT_DEPLOYMENT_MAX_SECONDS = 30;
    public static final String DEFAULT_CONTEXT = "/";
    private static final String FIGLET;

    private final Settings settings;
    private Path artifact;
    private Executor executor;
    private Logger logger;

    static {
        InputStream is = Gasper.class.getClassLoader().getResourceAsStream("gasper.txt");
        FIGLET = tryToExecute((EidPreconditions.UnsafeSupplier<String>) () ->
            CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)), "20160305:201329");
    }

    /**
     * Creates a builder interface {@link GasperBuilder} that can be used to configure Gasper.
     * <p>
     * You can also use, already created, configurations by using method {@link #configurations()} for
     * convenience.
     * @return a configure interface for configuration purposes
     */
    public static GasperBuilder configure() {
        return new GasperBuilder();
    }

    /**
     * Retrieves {@link GasperConfigurations} which hold some pre configured {@link GasperBuilder} instances
     * and can be used for convenience.
     * @return a pre configured configurations
     */
    public static GasperConfigurations configurations() {
        return new GasperConfigurations();
    }

    /**
     * Use this method to get port on which Gasper runs your test application.
     * @return a usually random port on which Gasper runs your application
     */
    public Integer getPort() {
        return settings.getPort();
    }

    /**
     * Use this method to get full address to your test application that Gasper runs. It usually
     * contains a random port.
     * @return a full address to running application
     */
    public String getAddress() {
        return settings.getEndpoint().fullAddress();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return tryToExecute((EidPreconditions.UnsafeSupplier<Statement>) () -> {
            setup();
            before();
            return new GasperStatement(base, this::after);
        }, "20160305:004035");
    }

    protected interface RunnerCreator {
        default Gasper create(Settings settings) {
            return new Gasper(settings);
        }
    }

    private void setup() {
        log(FIGLET);
        MavenResolver resolver = new MavenResolver(settings.getPomfile());
        artifact = resolver.getBuildArtifact(settings.getPackaging(), settings.getClassifier());
        File workingDirectory = resolver.getBuildDirectory();
        List<String> command = buildCommand();
        log("Command to be executed: \"%s\"", command.stream().collect(Collectors.joining(" ")));
        executor = new Executor(command, workingDirectory, settings);
    }

    private void before() throws IOException {
        executor.start();
        log("All looks ready, running tests...");
    }

    private void after() {
        log("Testing on server completed.");
        executor.stop();
    }

    @RequiredArgsConstructor
    private static class GasperStatement extends Statement {
        private final Statement base;
        private final Procedure procedure;

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                procedure.execute();
            }
        }
    }

    @FunctionalInterface
    private interface Procedure {
        void execute();
    }

    private List<String> buildCommand() {
        List<String> command = new ArrayList<>();
        command.add("java");
        buildJavaOptions(command);
        command.add("-jar");
        command.add(artifact.toAbsolutePath().toString());
        return command;
    }

    private void buildJavaOptions(List<String> command) {
        command.addAll(settings.getJvmOptions());
        command.addAll(settings.getSystemProperties().entrySet().stream()
            .map(entry -> format("-D%s=%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.toList())
        );
    }

    private void log(String frmt, Object... args) {
        ensureLogger();
        logger.info(format(frmt, args));
    }

    private void ensureLogger() {
        if (logger == null) {
            logger = new Logger(log, settings);
        }
    }
}
