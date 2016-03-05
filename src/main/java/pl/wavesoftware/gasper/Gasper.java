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
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-04
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Gasper implements TestRule {

    public static final int DEFAULT_PORT_AVAILABLE_MAX_SECONDS = 60;
    public static final int DEFAULT_DEPLOYMENT_MAX_SECONDS = 30;
    public static final String DEFAULT_CONTEXT = "/";
    private static final String figlet;

    private final Settings settings;
    private Path artifact;
    private Executor executor;
    private Logger logger;

    static {
        InputStream is = Gasper.class.getClassLoader().getResourceAsStream("gasper.txt");
        figlet = tryToExecute((EidPreconditions.UnsafeSupplier<String>) () ->
            CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)), "20160305:201329");
    }

    public static GasperBuilder builder() {
        return new GasperBuilder();
    }

    public static GasperConfigurations configurations() {
        return new GasperConfigurations();
    }

    public Integer getPort() {
        return settings.getPort();
    }

    public String getAddress() {
        return settings.getEndpoint().fullAddress();
    }

    protected abstract static class RunnerCreator {
        public Gasper create(Settings settings) {
            return new Gasper(settings);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        GasperStatement afterStmt = new GasperStatement(base, this);
        return tryToExecute((EidPreconditions.UnsafeSupplier<Statement>) () -> {
            setup();
            before();
            return afterStmt;
        }, "20160305:004035");
    }

    @RequiredArgsConstructor
    private static class GasperStatement extends Statement {
        private final Statement base;
        private final Gasper gasper;

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                gasper.after();
            }
        }
    }

    private void setup() {
        log(figlet);
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
        log("Testing on server completed.");
        executor.stop();
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
