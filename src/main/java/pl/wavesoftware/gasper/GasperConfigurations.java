package pl.wavesoftware.gasper;

/**
 * This class holds supported and tested configurations for servers.
 * <p>
 * Configuration in this class are fairly tested and can serve as a base for configuration.
 * You shouldn't try to use this class directly. Use instead {@link Gasper#configurations()} for entry point.
 * <p>
 * Example:
 * <pre>
 * &#064;ClassRule
 * public Gasper runner = Gasper.configurations()
 *     .wildflySwarm()
 *     .build();
 * </pre>
 *
 * More info in {@link Gasper} javadoc
 *
 * @author Krzysztof Suszyński <krzysztof.suszynski@wavesoftware.pl>
 * @since 2016-03-05
 */
public final class GasperConfigurations {
    public static final String WILDFLY_SWARM = "swarm.http.port";
    public static final String SPRING_BOOT = "server.port";
    protected GasperConfigurations() {}

    /**
     * This method returns pre-configured Gasper configuration to use with WildFly Swarm.
     * <p>
     * You can use it directly or use {@link GasperBuilder} interface to re-configure it to you needs.
     * <p>
     * To use it in JUnit execute method {@link GasperBuilder#build()}
     * @return pre-configured {@link GasperBuilder} to use with WildFly Swarm.
     */
    public GasperBuilder wildflySwarm() {
        return Gasper.configure()
            .withArtifactPackaging("jar")
            .withArtifactClassifier("swarm")
            .usingSystemPropertyForPort(GasperConfigurations.WILDFLY_SWARM);
    }

    /**
     * This method returns pre-configured Gasper configure to use with Spring Boot.
     * <p>
     * You can use it directly or use {@link GasperBuilder} interface to re-configure it to you needs.
     * <p>
     * To use it in JUnit execute method {@link GasperBuilder#build()}
     * @return pre-configured {@link GasperBuilder} to use with Spring Boot.
     */
    public GasperBuilder springBoot() {
        return Gasper.configure()
            .withArtifactPackaging("jar")
            .usingSystemPropertyForPort(GasperConfigurations.SPRING_BOOT);
    }
}
