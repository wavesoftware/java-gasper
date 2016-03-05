package pl.wavesoftware.gasper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import pl.wavesoftware.eid.exceptions.EidIllegalStateException;
import pl.wavesoftware.gasper.internal.Executor;
import pl.wavesoftware.gasper.internal.Settings;
import pl.wavesoftware.gasper.internal.maven.MavenResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-04
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Gasper implements TestRule {

    public static final int DEFAULT_PORT_AVAILABLE_MAX_SECONDS = 60;
    public static final int DEFAULT_DEPLOYMENT_MAX_SECONDS = 30;
    public static final String DEFAULT_CONTEXT = "/";

    private final Settings settings;
    private Path artifact;
    private Executor executor;

    public static GasperBuilder builder() {
        return new GasperBuilder();
    }

    public static GasperConfigurations configurations() {
        return new GasperConfigurations();
    }

    public Integer getPort() {
        return settings.getPort();
    }

    protected abstract static class RunnerCreator {
        public Gasper create(Settings settings) {
            return new Gasper(settings);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            setup();
            before();
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } finally {
                        after();
                    }
                }
            };
        } catch (IOException e) {
            throw new EidIllegalStateException("20160305:004035", e);
        }
    }

    private void setup() {
        log.info(format(
            "%s simple integration test pl.wavesoftware.gasper starting!", this.getClass().getSimpleName()
        ));
        MavenResolver resolver = new MavenResolver();
        artifact = resolver.getBuildArtifact(settings.getPackaging(), settings.getClassifier());
        File workingDirectory = resolver.getBuildDirectory();
        List<String> command = buildCommand();
        log.info(format(
            "Command to be executed: \"%s\"", command.stream().collect(Collectors.joining(" "))
        ));
        executor = new Executor(command, workingDirectory, settings);
    }

    private void before() throws IOException {
        executor.start();
        log.info("All looks ready, running tests...");
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
        command.addAll(settings.getJavaOptions().entrySet().stream()
            .map(entry -> format("-D%s=%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.toList())
        );
    }

    private void after() {
        log.info("Testing on server completed.");
        executor.stop();
    }
}
