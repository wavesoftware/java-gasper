package pl.wavesoftware.gasper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
public class GasperForWildflySwarmIT {

    private static final Path WILDFLY_SWARM_POMFILE = Paths.get(
        "target", "it", "wildfly-swarm-tester", "pom.xml"
    );

    @ClassRule
    public static Gasper gasper = Gasper.configurations()
        .wildflySwarm()
        .usePomFile(WILDFLY_SWARM_POMFILE)
        .silent()
        .build();

    @Test
    public void testGetRoot() throws UnirestException {
        // given
        String address = gasper.getAddress();
        String expectedMessage = "Hello from WildFly Swarm!";

        // when
        HttpResponse<String> response = Unirest.get(address).asString();

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedMessage);
        assertThat(gasper.getPort()).isGreaterThanOrEqualTo(1000);
    }

}
