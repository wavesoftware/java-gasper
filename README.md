# Gasper!

[![Join the chat at https://gitter.im/wavesoftware/java-gasper](https://badges.gitter.im/wavesoftware/java-gasper.svg)](https://gitter.im/wavesoftware/java-gasper?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/wavesoftware/java-gasper.svg?branch=develop)](https://travis-ci.org/wavesoftware/java-gasper) [![Coverage Status](https://coveralls.io/repos/github/wavesoftware/java-gasper/badge.svg?branch=develop)](https://coveralls.io/github/wavesoftware/java-gasper?branch=develop) [![Codacy Badge](https://api.codacy.com/project/badge/grade/5c4d1180812e438ebe872f9121ec4368)](https://www.codacy.com/app/krzysztof-suszynski/java-gasper) [![SonarQube Tech Debt](https://img.shields.io/sonar/http/sonar-ro.wavesoftware.pl/pl.wavesoftware:gasper/tech_debt.svg)](https://sonar.wavesoftware.pl/dashboard/index/2858) [![Maven Central](https://img.shields.io/maven-central/v/pl.wavesoftware/gasper.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.wavesoftware%22%20AND%20a%3A%22gasper%22) [![GitHub license](https://img.shields.io/github/license/wavesoftware/java-gasper.svg)](https://github.com/wavesoftware/java-gasper/blob/master/LICENSE)

Gasper is a very simple integration testing JUnit harness for `java -jar` servers like [WildFly Swarm](http://wildfly-swarm.io/) and [Spring Boot](http://projects.spring.io/spring-boot/).

[![WildFly Swarm](https://avatars3.githubusercontent.com/u/11523816?v=3&s=100)](http://wildfly-swarm.io/) [![Spring Boot](https://avatars2.githubusercontent.com/u/317776?v=3&s=100)](http://projects.spring.io/spring-boot/)

Gasper provides a simple to use JUnit `TestRule` that can be used to build integration tests with simple apps, like REST micro-services. You can configure Gasper easily with a builder interface. Gasper will start the application before test class and stop it after tests completes.

Gasper supports currently only [Maven](https://maven.apache.org/). The `pom.xml` file is used to read project configuration achieving zero configuration operation.

## Usage


Gasper utilize your packaged application. It It means it should be used in integration tests that run after application is being packaged by build tool (Maven). Add this code to your `pom.xml` file (if you didn't done that before):

```xml
<build>
[..]
<plugins>
[..]
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>2.19.1</version>
    <executions>
      <execution>
        <goals>
          <goal>integration-test</goal>
          <goal>verify</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
[..]
</plugins>
[..]
</build>
```


Place your integration tests in classes that ends with `*IT` or `*ITest`.

### WildFly Swarm configuration

```java
@ClassRule
public static Gasper gasper = Gasper.configurations()
  .wildflySwarm()
  .build();
```

### Spring Boot configuration

```java
@ClassRule
public static Gasper gasper = Gasper.configurations()
  .springBoot()
  .build();
```

Before running `GasperBuilder.build()` method, you can reconfigure those default configurations to your needs.

### Example test method (Unirest + JSONAssert)

Gasper is best to use with libraries like [Unirest](http://unirest.io/java.html) for fetching data and asserting HTTP/S statuses and [JSON Assert](https://github.com/marcingrzejszczak/jsonassert) to validate correctness of JSON output for REST services.

```java
@Test
public void testGetRoot() throws UnirestException {
  // given
  String address = gasper.getAddress(); // Address to deployed app, running live on random port
  String expectedMessage = "WildFly Swarm!";

  // when
  HttpResponse<String> response = Unirest.get(address).asString();

  // then
  assertThat(response.getStatus()).isEqualTo(200);
  assertThat(response.getBody()).field("hello").isEqualTo(expectedMessage); // JSON Assert
}
```

### Additional configuration

To configure Gasper use `GasperBuilder` interface, for ex.:

```java
private final int port = 11909;
private final String webContext = "/test";
private final String systemPropertyForPort = "swarm.http.port";

@ClassRule
public static Gasper gasper = Gasper.configure()
  .silentGasperMessages()
  .usingSystemPropertyForPort(systemPropertyForPort)
  .withSystemProperty("swarm.context.path", webContext)
  .withSystemProperty(systemPropertyForPort, String.valueOf(port))
  .withJVMOptions("-server", "-Xms1G", "-Xmx1G", "-XX:+UseConcMarkSweepGC")
  .withMaxStartupTime(100)
  .withMaxDeploymentTime(20)
  .withEnvironmentVariable("jdbc.password", "S3CreT!1")
  .withTestApplicationLoggingOnConsole()
  .usingPomFile(Paths.get("pom.xml"))
  .withArtifactPackaging("jar")
  .waitForWebContext(webContext)
  .withArtifactClassifier("swarm")
  .usingWebContextChecker(GasperBuilderTest::checkContext)
  .withPort(port)
  .build();
```

## Installation

### Maven

```xml
<dependency>
    <groupId>pl.wavesoftware</groupId>
    <artifactId>gasper</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

## Contributing

Contributions are welcome!

To contribute, follow the standard [git flow](http://danielkummer.github.io/git-flow-cheatsheet/) of:

1. Fork it
1. Create your feature branch (`git checkout -b feature/my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin feature/my-new-feature`)
1. Create new Pull Request

Even if you can't contribute code, if you have an idea for an improvement please open an [issue](https://github.com/wavesoftware/java-gasper/issues).

## Requirements

* Java 8
* Maven 3

## Releases

* `1.0.0` - codename: *SkyMango*
	* First publicly available release
