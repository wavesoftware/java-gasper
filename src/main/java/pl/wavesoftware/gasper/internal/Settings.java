package pl.wavesoftware.gasper.internal;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import pl.wavesoftware.gasper.Gasper;

import java.util.Map;
import java.util.function.Function;

/**
 * This class represents a set of settings for Gasper. It is used as a POJO with settings.
 * <p>
 * <strong>CAUTION!</strong> It is internal class of Gasper, and shouldn't be used directly. Use gasper builder interface {@link Gasper#builder()} or {@link Gasper#builderPreconfigured()} to set those settings.
 *
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 * @see Gasper#builder()
 * @see Gasper#builderPreconfigured()
 */
@Data
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

}
