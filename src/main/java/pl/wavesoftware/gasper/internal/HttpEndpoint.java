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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static java.lang.String.format;

/**
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
@Getter
@Setter
@RequiredArgsConstructor
public class HttpEndpoint {
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_DOMAIN = "localhost";
    public static final String DEFAULT_QUERY = null;

    private final String scheme;
    private final String domain;
    private final int port;
    private final String context;
    private final String query;

    public String fullAddress() {
        String address = format("%s://%s:%s%s",
            getScheme(),
            getDomain(),
            getPort(),
            getContext()
        );
        if (getQuery() != null) {
            address += "?" + getQuery();
        }
        return address;
    }
}
