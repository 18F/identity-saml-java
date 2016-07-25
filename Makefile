# Makefile for building and running the project.
# The purpose of this Makefile is to avoid developers having to remember
# project-specific commands for building, running, etc.  Recipes longer
# than one or two lines should live in script files of their own in the
# bin/ directory.

all: build

build:
	mvn compile install

run: build
	mvn spring-boot:run

test:
	mvm test

.PHONY: build run test
