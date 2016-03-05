package pl.wavesoftware.gasper.internal;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
@RequiredArgsConstructor
public class Logger {
    private final org.slf4j.Logger logger;
    private final Settings settings;

    public void info(String message) {
        if (settings.getLevel().toInt() <= Level.INFO.toInt()) {
            logger.info(message);
        }
    }
}
