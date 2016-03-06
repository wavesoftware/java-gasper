package pl.wavesoftware.gasper.internal;

import org.slf4j.event.Level;
import pl.wavesoftware.eid.utils.EidPreconditions;
import pl.wavesoftware.gasper.Gasper;
import pl.wavesoftware.gasper.GasperBuilder;
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
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
public abstract class AbstractGasperBuilder implements GasperBuilder {

    private String packaging = MavenResolver.DEFAULT_PACKAGING;
    private String classifier = "";
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

    protected AbstractGasperBuilder() {}

    @Override public GasperBuilder withArtifactPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    @Override public GasperBuilder withArtifactClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    @Override public GasperBuilder withEnvironmentVariable(String key, String value) {
        environment.put(key, value);
        return this;
    }

    @Override public GasperBuilder withSystemProperty(String key, String value) {
        systemProperties.put(key, value);
        return this;
    }

    @Override public GasperBuilder withJVMOptions(String... options) {
        Collections.addAll(jvmOptions, options);
        return this;
    }

    @Override public GasperBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    @Override public GasperBuilder usingSystemPropertyForPort(String systemPropertyForPort) {
        this.systemPropertyForPort = systemPropertyForPort;
        return this;
    }

    @Override public GasperBuilder usingPomFile(Path pomfile) {
        this.pomfile = pomfile;
        return this;
    }

    @Override public GasperBuilder withServerLoggingOnConsole() {
        return withServerLoggingOnConsole(true);
    }

    @Override public GasperBuilder withServerLoggingOnConsole(boolean inheritIO) {
        this.inheritIO = inheritIO;
        return this;
    }

    @Override public GasperBuilder withMaxStartupTime(int seconds) {
        this.portAvailableMaxTime = seconds;
        return this;
    }

    @Override public GasperBuilder withMaxDeploymentTime(int seconds) {
        this.deploymentMaxTime = seconds;
        return this;
    }

    @Override public GasperBuilder waitForWebContext(String context) {
        this.context = context;
        return this;
    }

    @Override public GasperBuilder usingWebContextChecker(Function<HttpEndpoint, Boolean> contextChecker) {
        this.contextChecker = contextChecker;
        return this;
    }

    @Override public GasperBuilder silentGasperMessages() {
        usingLogLevel(Level.WARN);
        return this;
    }

    @Override public GasperBuilder usingLogLevel(Level level) {
        this.level = level;
        return this;
    }

    @Override public Gasper build() {
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
