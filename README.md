# Login.gov Sample SP — Java / Spring

An example service provider (SP) written in Java integrated with [Login.gov](https://login.gov). This simple web app is based on [Spring Boot](https://projects.spring.io/spring-boot/) and [OneLogin's SAML Java Toolkit](https://github.com/onelogin/java-saml), which supports SAML-based SSO and SLO.

## Prerequisites

- JDK 8+
- Apache Maven

On macOS, install Java and Maven using Homebrew:

```bash
brew tap caskroom/cask
brew cask install java
brew install maven
```

## Build and run

```bash
mvn compile
mvn spring-boot:run
```

View at `http://localhost:4567`
