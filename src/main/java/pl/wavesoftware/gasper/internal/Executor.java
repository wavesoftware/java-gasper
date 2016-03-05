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
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_DOMAIN = "localhost";
    public static final String DEFAULT_QUERY = null;
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private final List<String> command;
    private final File workingDirectory;
    private final Settings settings;
    private Process process;

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
        process = pb.start();

        startAndWaitForPort();
        waitForHttpContext();
    }

    public void stop() {
        log.info("Stopping server.");
        process.destroy();
    }

    private void waitForHttpContext() {
        Integer port = settings.getPort();
        String context = settings.getContext();
        int maxWait = settings.getDeploymentMaxTime();
        log.info(format("Waiting for deployment for context: \"%s\" to happen...", context));
        boolean ok = waitForContextToBecomeAvailable(port, context, maxWait);
        if (!ok) {
            throw new EidIllegalStateException(new Eid("20160305:123206"),
                "Context %s in not available after waiting %s seconds, aborting!",
                context, maxWait
            );
        }
    }

    private boolean waitForContextToBecomeAvailable(int port, String context, int maxSeconds) {
        for (int i = 1; i <= maxSeconds * WAIT_STEPS_IN_SECOND; i++) {
            try {
                process.waitFor(WAIT_STEP, TimeUnit.MILLISECONDS);
                if (isContextAvailable(port, context)) {
                    int waited = WAIT_STEP * i;
                    log.info(format("Context \"%s\" became available after ~%dms!", context, waited));
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("Tried to wait " + WAIT_STEP + "ms, failed: " + e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    private static Boolean check(HttpEndpoint endpoint) {
        String address = format("%s://%s:%d%s",
            endpoint.getScheme(),
            endpoint.getDomain(),
            endpoint.getPort(),
            endpoint.getContext()
        );
        if (endpoint.getQuery() != null) {
            address += "?" + endpoint.getQuery();
        }
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

    private boolean isContextAvailable(int port, String context) {
        HttpEndpoint endpoint = new HttpEndpoint(
            DEFAULT_SCHEME, DEFAULT_DOMAIN, port, context, DEFAULT_QUERY
        );
        return settings.getContextChecker().apply(endpoint);
    }

    private void startAndWaitForPort() {
        Integer port = settings.getPort();
        log.info(format("Starting server, waiting for port: %d to became active...", port));
        boolean ok = waitForPortToBecomeAvailable(process, port, settings.getPortAvailableMaxTime());
        if (!ok) {
            throw new EidIllegalStateException(new Eid("20160305:003452"),
                "Process %s probably didn't started well after maximum wait time is reached: %s",
                command.toString(), settings.getPortAvailableMaxTime()
            );
        }
    }

    private static boolean waitForPortToBecomeAvailable(Process process, int port, int maxSeconds) {
        for (int i = 1; i <= maxSeconds * WAIT_STEPS_IN_SECOND; i++) {
            try {
                process.waitFor(WAIT_STEP, TimeUnit.MILLISECONDS);
                if (isPortTaken(port)) {
                    int waited = WAIT_STEP * i;
                    log.info(format("Port %d became available after ~%dms!", port, waited));
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("Tried to wait " + WAIT_STEP + "ms, failed: " + e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    private void logToFile(ProcessBuilder pb) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        Path logFile = tempDir.toPath().resolve("gasper-server.log");
        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile.toFile());
        log.info(format("Logging server messages to: %s", logFile));
    }

    private static boolean isPortTaken(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException ex) {
            return true;
        }
    }
}
