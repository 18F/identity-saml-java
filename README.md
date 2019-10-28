# Login.gov Sample SP — Java / Spring

<aside class="warning">
<br>This sample SP has been retired.  It was used for early prototyping for integrations with login.gov and has not been maintained. It has confirmed vulnerabilities and should not be used for production itegrations.</br> 
<br>For maintained examples of SAML integrations with login.gov please refer to:
<ul>https://github.com/18F/identity-saml-rails</ul>
<ul>https://github.com/18F/identity-saml-sinatra</ul>
</br>
</aside>

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
