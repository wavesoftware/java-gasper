# Gasper!

[![Build Status](https://travis-ci.org/wavesoftware/java-gasper.svg?branch=develop)](https://travis-ci.org/wavesoftware/java-gasper) [![Coverage Status](https://coveralls.io/repos/github/wavesoftware/java-gasper/badge.svg?branch=develop)](https://coveralls.io/github/wavesoftware/java-gasper?branch=develop) [![Codacy Badge](https://api.codacy.com/project/badge/grade/5c4d1180812e438ebe872f9121ec4368)](https://www.codacy.com/app/krzysztof-suszynski/java-gasper) [![SonarQube Tech Debt](https://img.shields.io/sonar/http/sonar-ro.wavesoftware.pl/pl.wavesoftware:gasper/tech_debt.svg)](https://sonar.wavesoftware.pl/dashboard/index/2858)

Gasper is a very simple integration testing JUnit harness for `java -jar` servers like [WildFly Swarm](http://wildfly-swarm.io/) and [Spring Boot](http://projects.spring.io/spring-boot/).

[![WildFly Swarm](https://avatars3.githubusercontent.com/u/11523816?v=3&s=100)](http://wildfly-swarm.io/) [![Spring Boot](https://avatars2.githubusercontent.com/u/317776?v=3&s=100)](http://projects.spring.io/spring-boot/)

Gasper provides a simple to use JUnit `TestRule` that can be used to build integration tests with simple apps, like micro-services. You can configure Gasper with easy to use builder interface.

## Usage

Best to use with libraries like [Unirest](http://unirest.io/java.html) and [JSON Assert](https://github.com/marcingrzejszczak/jsonassert)

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

### Example test method (Unirest + JSONAssert)

```java
@Test
public void testGetRoot() throws UnirestException {
  // given
  String address = gasper.getAddress();
  String expectedMessage = "WildFly Swarm!";

  // when
  HttpResponse<String> response = Unirest.get(address).asString();

  // then
  assertThat(response.getStatus()).isEqualTo(200);
  assertThat(response.getBody()).field("hello").isEqualTo(expectedMessage);
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
  .withServerLoggingOnConsole()
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

## Requirements

Gasper requires Java 8. Tested on Travis CI.

