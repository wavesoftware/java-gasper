package pl.wavesoftware.gasper;

import org.slf4j.event.Level;
import pl.wavesoftware.gasper.internal.HttpEndpoint;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-06
 */
public interface GasperBuilder extends Gasper.RunnerCreator {
    GasperBuilder withArtifactPackaging(String packaging);

    GasperBuilder withArtifactClassifier(String classifier);

    GasperBuilder withEnvironmentVariable(String key, String value);

    GasperBuilder withSystemProperty(String key, String value);

    GasperBuilder withJVMOptions(String... options);

    GasperBuilder withPort(int port);

    GasperBuilder usingSystemPropertyForPort(String systemPropertyForPort);

    GasperBuilder usingPomFile(Path pomfile);

    GasperBuilder withServerLoggingOnConsole();

    GasperBuilder withServerLoggingOnConsole(boolean inheritIO);

    GasperBuilder withMaxStartupTime(int seconds);

    GasperBuilder withMaxDeploymentTime(int seconds);

    GasperBuilder waitForWebContext(String context);

    GasperBuilder usingWebContextChecker(Function<HttpEndpoint, Boolean> contextChecker);

    GasperBuilder silentGasperMessages();

    GasperBuilder usingLogLevel(Level level);

    Gasper build();
}
