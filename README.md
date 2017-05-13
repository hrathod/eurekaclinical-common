# Eureka! Clinical Common Web Application Framework
Eureka! Clinical's web application development framework

## Version history

### 1.0
Implemented partial functionality, and only some parts of Eureka! Clinical used it.

## What does it do?

It implements a custom web application development framework that uses best of breed components:
* Google Guice 3.0 (dependency injection)
* Jersey 1.17.1 (REST)
* Hibernate 5.0.6.Final (object-relational mapping)
* Tomcat 7

It supports calling these components through the following standard APIs:
* JPA 2.1 (Java Persistence)
* Servlet API 3.0.1
* JAX-RS 2.0.1 (Java API for RESTful Web Services)
* JSR-330 (standard @Inject and @Provider annotations)

See [Structure of Eureka! Clinical microservices](https://github.com/eurekaclinical/dev-wiki/wiki/Structure-of-Eureka%21-Clinical-microservices) for how to use the framework. Every Eureka! Clinical web application component uses this framework.

## Building it
The project uses the maven build tool. Typically, you build it by invoking `mvn clean install` at the command line. For simple file changes, not additions or deletions, you can usually use `mvn install`. See https://github.com/eurekaclinical/dev-wiki/wiki/Building-Eureka!-Clinical-projects for more details.

## Releasing it
First, ensure that there is no uncommitted code in your repo. Release it by invoking `mvn release:prepare` followed by `mvn release:perform`. See https://github.com/eurekaclinical/dev-wiki/wiki/Project-release-process for more details.
