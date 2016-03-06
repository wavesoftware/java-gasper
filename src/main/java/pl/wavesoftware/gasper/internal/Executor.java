package pl.wavesoftware.gasper.internal;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wavesoftware.eid.exceptions.Eid;
import pl.wavesoftware.eid.exceptions.EidIllegalStateException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
@Slf4j
@RequiredArgsConstructor
public class Executor {
    public static final int WAIT_STEP = 125;
    public static final int WAIT_STEPS_IN_SECOND = 8;
    public static final Function<HttpEndpoint, Boolean> DEFAULT_CONTEXT_CHECKER = Executor::check;
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private final List<String> command;
    private final File workingDirectory;
    private final Settings settings;
    private Process process;
    private Logger logger;

    public void start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDirectory);
        if (settings.isInheritIO()) {
            pb.inheritIO();
        } else {
            logToFile(pb);
        }
        if (!settings.getEnvironment().isEmpty()) {
            pb.environment().putAll(settings.getEnvironment());
        }
        log("Starting server process");
        process = pb.start();

        startAndWaitForPort();
        waitForHttpContext();
    }

    public void stop() {
        log("Stopping server process");
        process.destroy();
    }

    private void waitForHttpContext() {
        String context = settings.getContext();
        int maxWait = settings.getDeploymentMaxTime();
        log("Waiting for deployment for context: \"%s\" to happen...", context);
        boolean ok = waitForContextToBecomeAvailable(context, maxWait);
        if (!ok) {
            throw new EidIllegalStateException(new Eid("20160305:123206"),
                "Context %s in not available after waiting %s seconds, aborting!",
                context, maxWait
            );
        }
    }

    private boolean waitForContextToBecomeAvailable(String context, int maxSeconds) {
        return waitOnProcess(maxSeconds, (step) -> {
            if (isContextAvailable()) {
                int waited = WAIT_STEP * step;
                log("Context \"%s\" became available after ~%dms!", context, waited);
                return true;
            }
            return false;
        });
    }

    private boolean waitForPortToBecomeAvailable(int port, int maxSeconds) {
        return waitOnProcess(maxSeconds, (step) -> {
            if (isPortTaken(port)) {
                int waited = WAIT_STEP * step;
                log("Port %d became available after ~%dms!", port, waited);
                return true;
            }
            return false;
        });
    }

    private boolean waitOnProcess(int maxSeconds, Function<Integer, Boolean> supplier) {
        for (int i = 1; i <= maxSeconds * WAIT_STEPS_IN_SECOND; i++) {
            try {
                process.waitFor(WAIT_STEP, TimeUnit.MILLISECONDS);
                if (supplier.apply(i)) {
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("Tried to wait " + WAIT_STEP + "ms, failed: " + e.getLocalizedMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    private static Boolean check(HttpEndpoint endpoint) {
        String address = endpoint.fullAddress();
        try {
            HttpResponse<InputStream> response = Unirest.head(address).asBinary();
            int status = response.getStatus();
            return status >= HTTP_OK && status < HTTP_BAD_REQUEST;
        } catch (UnirestException e) {
            EidIllegalStateException ex = new EidIllegalStateException(new Eid("20160305:125410"), e);
            log.error(ex.getEid().makeLogMessage("Can't make http request - %s", e.getLocalizedMessage()), ex);
            return false;
        }
    }

    private boolean isContextAvailable() {
        HttpEndpoint endpoint = settings.getEndpoint();
        return settings.getContextChecker().apply(endpoint);
    }

    private void startAndWaitForPort() {
        Integer port = settings.getPort();
        log("Waiting for port: %d to became active...", port);
        boolean ok = waitForPortToBecomeAvailable(port, settings.getPortAvailableMaxTime());
        if (!ok) {
            throw new EidIllegalStateException(new Eid("20160305:003452"),
                "Process %s probably didn't started well after maximum wait time is reached: %s",
                command.toString(), settings.getPortAvailableMaxTime()
            );
        }
    }

    private void logToFile(ProcessBuilder pb) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        Path logFile = tempDir.toPath().resolve("gasper.log");
        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile.toFile());
        log("Logging server messages to: %s", logFile);
    }

    private static boolean isPortTaken(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException ex) {
            log.trace(format("Port %d taken", port), ex);
            return true;
        }
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
