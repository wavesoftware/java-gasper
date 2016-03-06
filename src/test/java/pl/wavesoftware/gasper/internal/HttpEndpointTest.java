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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
public class HttpEndpointTest {

    @Test
    public void testFullAddress() throws Exception {
        HttpEndpoint endpoint = new HttpEndpoint("http", "example.org", 8080, "/", "a=7");
        String address = endpoint.fullAddress();
        assertThat(address).isEqualTo("http://example.org:8080/?a=7");
    }
}
