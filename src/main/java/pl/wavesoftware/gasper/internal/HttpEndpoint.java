package pl.wavesoftware.gasper.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static java.lang.String.format;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
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
