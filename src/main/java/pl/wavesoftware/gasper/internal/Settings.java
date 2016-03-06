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

package pl.wavesoftware.gasper.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import pl.wavesoftware.gasper.Gasper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class represents a set of settings for Gasper. It is used as a POJO with settings.
 * <p>
 * <strong>CAUTION!</strong> It is internal class of Gasper, and shouldn't be used directly. Use gasper configure interface {@link Gasper#configure()} or {@link Gasper#configurations()} to set those settings.
 *
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 * @see Gasper#configure()
 * @see Gasper#configurations()
 */
@Getter
@RequiredArgsConstructor
public class Settings {
    private final String packaging;
    private final String classifier;
    private final int port;
    private final Map<String, String> systemProperties;
    private final List<String> jvmOptions;
    private final Map<String, String> environment;
    private final boolean inheritIO;
    private final String context;
    private final Function<HttpEndpoint, Boolean> contextChecker;
    private final int portAvailableMaxTime;
    private final int deploymentMaxTime;
    private final Path pomfile;
    private final Level level;
    private HttpEndpoint endpoint;

    /**
     * Retrieves Java <code>-D</code> style options as map
     * @return a map for Java options
     */
    public Map<String, String> getSystemProperties() {
        return ImmutableMap.copyOf(systemProperties);
    }

    /**
     * Retrieves Java VM options as list
     * @return a map for Java options
     */
    public List<String> getJvmOptions() {
        return ImmutableList.copyOf(jvmOptions);
    }

    /**
     * Retrieves environment variables as a map
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
