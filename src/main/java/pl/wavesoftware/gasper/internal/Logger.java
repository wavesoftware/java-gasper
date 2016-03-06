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

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;

/**
 * @author Krzysztof Suszyński
 * @since 2016-03-05
 */
@RequiredArgsConstructor
public class Logger {
    private final org.slf4j.Logger slf;
    private final Settings settings;

    public void info(String message) {
        if (settings.getLevel().toInt() <= Level.INFO.toInt()) {
            slf.info(message);
        }
    }
}
