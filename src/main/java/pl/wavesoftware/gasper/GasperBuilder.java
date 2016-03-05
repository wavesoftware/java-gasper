package pl.wavesoftware.gasper;

import com.google.common.collect.ImmutableMap;
import pl.wavesoftware.eid.exceptions.EidIllegalStateException;
import pl.wavesoftware.gasper.internal.Executor;
import pl.wavesoftware.gasper.internal.HttpEndpoint;
import pl.wavesoftware.gasper.internal.Settings;
import pl.wavesoftware.gasper.internal.maven.MavenResolver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
public final class GasperBuilder extends Gasper.RunnerCreator {

    private String packaging = MavenResolver.DEFAULT_PACKAGING;
    private String classifier = "";
    private Map<String, String> javaOptions = new LinkedHashMap<>();
    private Map<String, String> environment = new LinkedHashMap<>();
    private String portJavaOption;
    private Integer port;
    private boolean inheritIO = false;
    private String context = Gasper.DEFAULT_CONTEXT;
    private int portAvailableMaxTime = Gasper.DEFAULT_PORT_AVAILABLE_MAX_SECONDS;
    private int deploymentMaxTime = Gasper.DEFAULT_DEPLOYMENT_MAX_SECONDS;
    private Function<HttpEndpoint, Boolean> contextChecker = Executor.DEFAULT_CONTEXT_CHECKER;

    public GasperBuilder withPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public GasperBuilder withClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public GasperBuilder withEnvironmentVariable(String key, String value) {
        environment.put(key, value);
        return this;
    }

    public GasperBuilder withJavaOption(String key, String value) {
        javaOptions.put(key, value);
        return this;
    }

    public GasperBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public GasperBuilder usePortJavaOptionFor(String portJavaOption) {
        this.portJavaOption = portJavaOption;
        return this;
    }

    public GasperBuilder inheritIO() {
        return inheritIO(true);
    }

    public GasperBuilder inheritIO(boolean inheritIO) {
        this.inheritIO = inheritIO;
        return this;
    }

    public Gasper build() {
        if (port == null) {
            port = findNotBindedPort();
        }
        if (portJavaOption != null) {
            withJavaOption(portJavaOption, port.toString());
        }
        Settings settings = new Settings(
            packaging, classifier, port,
            ImmutableMap.copyOf(javaOptions), ImmutableMap.copyOf(environment),
            inheritIO, context, contextChecker,
            portAvailableMaxTime, deploymentMaxTime
        );
        return create(settings);
    }

    public GasperBuilder maxStartupTime(int seconds) {
        this.portAvailableMaxTime = seconds;
        return this;
    }

    public GasperBuilder maxDeploymentTime(int seconds) {
        this.deploymentMaxTime = seconds;
        return this;
    }

    public GasperBuilder waitForContext(String context) {
        this.context = context;
        return this;
    }

    public GasperBuilder useContextChecker(Function<HttpEndpoint, Boolean> contextChecker) {
        this.contextChecker = contextChecker;
        return this;
    }

    private static int findNotBindedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException ex) {
            throw new EidIllegalStateException("20160305:000138", ex);
        }
    }
}
