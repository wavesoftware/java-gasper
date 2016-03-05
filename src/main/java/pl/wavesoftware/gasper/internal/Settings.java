package pl.wavesoftware.gasper.internal;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.event.Level;
import pl.wavesoftware.gasper.Gasper;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

/**
 * This class represents a set of settings for Gasper. It is used as a POJO with settings.
 * <p>
 * <strong>CAUTION!</strong> It is internal class of Gasper, and shouldn't be used directly. Use gasper builder interface {@link Gasper#builder()} or {@link Gasper#configurations()} to set those settings.
 *
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 * @see Gasper#builder()
 * @see Gasper#configurations()
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Settings {
    private final String packaging;
    private final String classifier;
    private final int port;
    private final Map<String, String> javaOptions;
    private final Map<String, String> environment;
    private final boolean inheritIO;
    private final String context;
    private final Function<HttpEndpoint, Boolean> contextChecker;
    private final int portAvailableMaxTime;
    private final int deploymentMaxTime;
    private final Path pomfile;
    private final Level level;
    @Setter(AccessLevel.NONE)
    private HttpEndpoint endpoint;

    /**
     * Gets Java options as map
     * @return a map for Java options
     */
    public Map<String, String> getJavaOptions() {
        return ImmutableMap.copyOf(javaOptions);
    }

    /**
     * Gets environment variables as a map
     * @return a map for environment variables
     */
    public Map<String, String> getEnvironment() {
        return ImmutableMap.copyOf(environment);
    }

    public HttpEndpoint getEndpoint() {
        ensureHttpEndpoint();
        return endpoint;
    }

    private void ensureHttpEndpoint() {
        if (endpoint == null) {
            endpoint = new HttpEndpoint(
                HttpEndpoint.DEFAULT_SCHEME,
                HttpEndpoint.DEFAULT_DOMAIN,
                port,
                context,
                HttpEndpoint.DEFAULT_QUERY
            );
        }
    }
}
