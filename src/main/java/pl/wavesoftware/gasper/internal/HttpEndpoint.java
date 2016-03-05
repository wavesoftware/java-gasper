package pl.wavesoftware.gasper.internal;

import lombok.Data;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
@Data
public class HttpEndpoint {
    private final String scheme;
    private final String domain;
    private final int port;
    private final String context;
    private final String query;
}
