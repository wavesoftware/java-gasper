package pl.wavesoftware.gasper;

import com.mashape.unirest.http.Unirest;
import org.junit.Test;
import pl.wavesoftware.eid.utils.EidPreconditions;
import pl.wavesoftware.gasper.internal.HttpEndpoint;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.wavesoftware.eid.utils.EidPreconditions.tryToExecute;

/**
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
public class GasperBuilderTest {

    @Test
    public void testBuild() throws Exception {
        GasperBuilder builder = new GasperBuilder();
        Gasper gasper = builder.withJavaOption("swarm.context.path", "/sub")
            .maxStartupTime(100)
            .maxDeploymentTime(20)
            .withEnvironmentVariable("jdbc.password", "S3CreT!1")
            .inheritIO()
            .silent()
            .usePomFile(Paths.get("pom.xml"))
            .withPackaging("jar")
            .waitForContext("/sub")
            .withClassifier("swarm")
            .useContextChecker(GasperBuilderTest::checkContext)
            .withPort(11909)
            .build();

        assertThat(gasper).isNotNull();
    }

    private static Boolean checkContext(HttpEndpoint endpoint) {
        return tryToExecute((EidPreconditions.UnsafeSupplier<Boolean>) () ->
            Unirest.get(endpoint.fullAddress()).asBinary().getStatus() == 200, "20160305:215916");
    }
}
