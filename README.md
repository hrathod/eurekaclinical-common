# Eureka! Clinical Common Web Application Framework
Eureka! Clinical's web application development framework

## Version 2.0 development series
Latest release: [![Latest release](https://maven-badges.herokuapp.com/maven-central/org.eurekaclinical/eurekaclinical-common/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eurekaclinical/eurekaclinical-common)

The goal of the 2.0 series is to have sufficient functionality for all Eureka! Clinical web applications to depend on eurekaclinical-common.

## Version history

### Version 1.0
Implemented partial functionality, and only some parts of Eureka! Clinical used it.

## What does it do?

It implements a custom web application development framework that uses best of breed components:
* Google Guice 3.0 (dependency injection)
* Jersey 1.17.1 (REST)
* Hibernate 5.0.6.Final (object-relational mapping)
* Tomcat 7

It supports calling these components through the standard APIs specified in and provided by the [Eureka! Clinical Web Application Standard APIs project](https://github.com/eurekaclinical/eurekaclinical-standard-apis).

It extends the functionality in the [Eureka! Clinical Web Application Standard APIs project](https://github.com/eurekaclinical/eurekaclinical-standard-apis) with the following features and implementations:
* REST communication objects for users, roles, groups and authorization templates (`org.eurekaclinical.common.comm`)


See [Structure of Eureka! Clinical microservices](https://github.com/eurekaclinical/dev-wiki/wiki/Structure-of-Eureka%21-Clinical-microservices) for how to use the framework. The goal for the version 2.0 release is for all Eureka! Clinical web application components to use this framework.

## Building it
The project uses the maven build tool. Typically, you build it by invoking `mvn clean install` at the command line. For simple file changes, not additions or deletions, you can usually use `mvn install`. See https://github.com/eurekaclinical/dev-wiki/wiki/Building-Eureka!-Clinical-projects for more details.

## Maven dependency
```
<dependency>
    <groupId>org.eurekaclinical</groupId>
    <artifactId>eurekaclinical-common</artifactId>
    <version>version</version>
</dependency>
```

## Development documentation
* [Javadoc for latest development release](http://javadoc.io/doc/org.eurekaclinical/eurekaclinical-common) [![Javadocs](http://javadoc.io/badge/org.eurekaclinical/eurekaclinical-common.svg)](http://javadoc.io/doc/org.eurekaclinical/eurekaclinical-common)
* [Javadoc for version 1.0](http://javadoc.io/doc/org.eurekaclinical/eurekaclinical-common/1.0)

## Getting help
Feel free to contact us at help@eurekaclinical.org.
