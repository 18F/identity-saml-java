Sample Java SP
==============

[![Build Status](https://travis-ci.org/18F/identity-sp-java.svg?branch=master)](https://travis-ci.org/18F/identity-sp-java)

An example service provider (SP) written in Java that integrates with 18F's
identity-idp.

This is a very simply app based the `spring` (specifically `spring-boot`)
and `java-saml` which supports SAML-based SSO and SLO.

## Dependecies

1. JDK 8+
2. Apache maven

## Building

    $ mvn compile

## Running

    $ mvn spring-boot:run

## Testing

    $ mvm test
