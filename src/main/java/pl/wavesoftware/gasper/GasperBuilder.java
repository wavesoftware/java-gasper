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

import org.slf4j.event.Level;
import pl.wavesoftware.eid.utils.EidPreconditions;
import pl.wavesoftware.gasper.internal.Executor;
import pl.wavesoftware.gasper.internal.HttpEndpoint;
import pl.wavesoftware.gasper.internal.Settings;
import pl.wavesoftware.gasper.internal.maven.MavenResolver;

import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pl.wavesoftware.eid.utils.EidPreconditions.tryToExecute;

/**
 * This is builder interface for {@link Gasper}. You can use it to configure it to your needs.
 * <p>
 * Methods implements fluent interface for ease of use.
 *
 * <h2>Example</h2>
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
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
public final class GasperBuilder implements Gasper.RunnerCreator {

    private String packaging = MavenResolver.DEFAULT_PACKAGING;
    private String classifier = MavenResolver.DEFAULT_CLASSIFIER;
    private Map<String, String> systemProperties = new LinkedHashMap<>();
    private List<String> jvmOptions = new ArrayList<>();
    private Map<String, String> environment = new LinkedHashMap<>();
    private String systemPropertyForPort;
    private Integer port;
    private boolean inheritIO = false;
    private String context = Gasper.DEFAULT_CONTEXT;
    private int portAvailableMaxTime = Gasper.DEFAULT_PORT_AVAILABLE_MAX_SECONDS;
    private int deploymentMaxTime = Gasper.DEFAULT_DEPLOYMENT_MAX_SECONDS;
    private Function<HttpEndpoint, Boolean> contextChecker = Executor.DEFAULT_CONTEXT_CHECKER;
    private Path pomfile = Paths.get(MavenResolver.DEFAULT_POM);
    private Level level = Level.INFO;

    protected GasperBuilder() {}

    /**
     * Change the artifact packaging. By default it is read from your <code>pom.xml</code> file. Use it to point
     * to other artifact.
     *
     * @param packaging a Java packaging, can be something like <code>jar</code> or <code>war</code>.
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withArtifactPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    /**
     * Change the artifact classifier. By default it is read from your <code>pom.xml</code> file. Use it to point
     * to other artifact.
     *
     * @param classifier a Maven classifier, can be something like <code>shade</code> or <code>swarm</code>.
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withArtifactClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    /**
     * Sets a environment variable to be set for your test application
     *
     * @param key an environment key
     * @param value an environment value
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withEnvironmentVariable(String key, String value) {
        environment.put(key, value);
        return this;
    }

    /**
     * Sets a Java system property (for ex.: <code>-Dserver.ssl=true</code>) variable to be set for
     * your test application.
     *
     * @param key a system property key without <code>-D</code> sign
     * @param value a system property value
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withSystemProperty(String key, String value) {
        systemProperties.put(key, value);
        return this;
    }

    /**
     * Sets a JVM options (for ex.: <code>-Xmx2G</code>) to be set for your test application.
     *
     * @param options a list of JVM options in the same form as they will be given to process
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withJVMOptions(String... options) {
        Collections.addAll(jvmOptions, options);
        return this;
    }

    /**
     * Sets the port to be used for starting your test application. The port must be available and user
     * must have permission to use it. By default port is automatically calculated to be random, free
     * one. Use this only if you don't like automatic port lookup.
     *
     * @param port a port to be used
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Configures what system property use to set port in your test application. For WildFly Swarm
     * and Sprint Boot this is already configured in {@link GasperConfigurations} methods
     * {@link GasperConfigurations#wildflySwarm()} and {@link GasperConfigurations#springBoot()}.
     * Use it if you must pass other system property.
     *
     * @param systemPropertyForPort a system property to be used to change te port on which your
     *                              test application will run.
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder usingSystemPropertyForPort(String systemPropertyForPort) {
        this.systemPropertyForPort = systemPropertyForPort;
        return this;
    }

    /**
     * Change the <code>pom.xml</code> to be used. Gasper will read your project settings from it,
     * like <code>artifactId</code>, <code>packaging</code>, <code>classifier</code>, <code>version</code>
     * and <code>build directory</code> to locate artifact to be run as test application.
     *
     * @param pomfile a custom <code>pom.xml</code> to be used to read configuration properties
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder usingPomFile(Path pomfile) {
        this.pomfile = pomfile;
        return this;
    }

    /**
     * Configures your test application to logs it's messages on console instead of log file.
     *
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withTestApplicationLoggingOnConsole() {
        return withTestApplicationLoggingOnConsole(true);
    }

    /**
     * Configures your test application whether to logs it's messages on console instead
     * of log file or not.
     *
     * @param inheritIO if true, the test application will logs it's messages on console,
     *                  if not messages will be forwarder to <code>[system-temp]/gasper.log</code>
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withTestApplicationLoggingOnConsole(boolean inheritIO) {
        this.inheritIO = inheritIO;
        return this;
    }

    /**
     * Sets maximum wait time for your test application to open HTTP port. Tests
     * will fail if your test application will not open requested port in that time.
     *
     * @param seconds maximum wait time for open port in seconds
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withMaxStartupTime(int seconds) {
        this.portAvailableMaxTime = seconds;
        return this;
    }

    /**
     * Sets maximum wait time for your test application to deploy expected web context.
     * Tests will fail if your test application will not deploy web context in time.
     *
     * @param seconds maximum wait time for deployment in seconds
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder withMaxDeploymentTime(int seconds) {
        this.deploymentMaxTime = seconds;
        return this;
    }

    /**
     * Sets te web context to wait for. Gasper by default will try to execute
     * <code>HEAD</code> request to that address until it became available. By
     * default te web context id just <code>"/"</code>.
     *
     * @param context the web context to wait for, by default <code>"/"</code>
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder waitForWebContext(String context) {
        this.context = context;
        return this;
    }

    /**
     * Change web context checker that will be used to check if web context is
     * deployed. Gasper by default will try to execute <code>HEAD</code> request
     * to that address until it became available.
     *
     * @param contextChecker a function to use to test if web context id up
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder usingWebContextChecker(Function<HttpEndpoint, Boolean> contextChecker) {
        this.contextChecker = contextChecker;
        return this;
    }

    /**
     * Silent Gasper log messages.
     *
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder silentGasperMessages() {
        usingLogLevel(Level.WARN);
        return this;
    }

    /**
     * Sets log level for Gasper to limit logging.
     * @param level a SLF log level
     * @return fluent interface returning self for chaining
     */
    public GasperBuilder usingLogLevel(Level level) {
        this.level = level;
        return this;
    }

    /**
     * Builds final Gasper instance with all given variables
     * @return a Gasper {@link org.junit.rules.TestRule}
     */
    public Gasper build() {
        if (port == null) {
            port = findNotBindedPort();
        }
        if (systemPropertyForPort != null) {
            withSystemProperty(systemPropertyForPort, port.toString());
        }
        Settings settings = new Settings(
            packaging, classifier, port,
            systemProperties, jvmOptions, environment,
            inheritIO, context, contextChecker,
            portAvailableMaxTime, deploymentMaxTime,
            pomfile, level
        );
        return create(settings);
    }

    private static Integer findNotBindedPort() {
        return tryToExecute((EidPreconditions.UnsafeSupplier<Integer>) () -> {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            }
        }, "20160305:202934");

    }
}
