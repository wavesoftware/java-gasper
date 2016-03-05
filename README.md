# Gasper!

[![Build Status](https://travis-ci.org/wavesoftware/java-gasper.svg?branch=develop)](https://travis-ci.org/wavesoftware/java-gasper) [![Coverage Status](https://coveralls.io/repos/github/wavesoftware/java-gasper/badge.svg?branch=develop)](https://coveralls.io/github/wavesoftware/java-gasper?branch=develop)

Gasper is a very simple integration testing JUnit harness for `java -jar` servers like [WildFly Swarm](http://wildfly-swarm.io/) and [Spring Boot](http://projects.spring.io/spring-boot/).

[![WildFly Swarm](https://avatars3.githubusercontent.com/u/11523816?v=3&s=100)](http://wildfly-swarm.io/) [![Spring Boot](https://avatars2.githubusercontent.com/u/317776?v=3&s=100)](http://projects.spring.io/spring-boot/)

Gasper provides a simple to use JUnit `TestRule` that can be used to build integration tests with simple apps, like microservices. You can configure Gasper with easy to use builder interface.

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

### Addtional configuration

To configure Gasper use `GasperBuilder` interface, for ex.:

```java
@ClassRule
public static Gasper gasper = Gasper.configurations()
  .wildflySwarm()
  .usePomFile(Paths.get("target", "it", "wildfly-swarm-tester", "pom.xml"))
  .inheritIO()
  .maxStartupTime(120)
  .maxDeploymentTime(20)
  .useContextChecker(MyTestClass::contextChecker)
  .withEnvironmentVariable("jdbc.password", DEV_PASSWORD)
  .withJavaOption("my.minus-d.option", "true")
  .silent()
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

### Gradle

```groovy
testCompile 'pl.wavesoftware:gasper:1.0.0'
```

## Requirements

Gasper requires Java 8. Tested on Travis CI.

