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

package pl.wavesoftware.gasper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
@Slf4j
public class GasperForSpringBootIT {

    private static final Path SPRING_BOOT_POMFILE = Paths.get(
        "target", "it", "spring-boot-tester", "pom.xml"
    );

    @ClassRule
    public static Gasper gasper = Gasper.configurations()
        .springBoot()
        .usingPomFile(SPRING_BOOT_POMFILE)
        .build();

    @Test
    public void testGetRoot() throws UnirestException {
        // given
        String address = gasper.getAddress();
        String expectedMessage = "Hello from Spring Boot!";

        // when
        HttpResponse<String> response = Unirest.get(address).asString();

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedMessage);
        log.info("Server returned: " + response.getBody());
    }

    @Test
    public void testGetNonExistent() throws UnirestException {
        // given
        String nonExistingPath = "non-existing";
        String address = gasper.getAddress() + nonExistingPath;

        // when
        HttpResponse<String> response = Unirest.get(address).asString();

        // then
        assertThat(response.getStatus()).isEqualTo(404);
    }

}
