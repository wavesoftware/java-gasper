package pl.wavesoftware.gasper.internal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
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
