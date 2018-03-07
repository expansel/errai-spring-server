# errai-spring-server
This library is aimed at making Errai and Spring play together on the server side without a CDI environment. Currently it has mainly been tested in a Spring boot environment and has not seen production use.


## Core features:
1. Register @Service classes managed and wired by Spring with the ServerMessageBus. This includes MessageCallback implementations, RPC classes and @Command annotated methods. Methods annotated with @Service are not currently supported. Note however that all services are treated as Singletons except for RPC classes which may use other Spring scopes such as prototype and session.
2. The Errai ServerMessageBus and RequestDispatcher can be autowired by registering the relevant FactoryBean classes with Spring. Note however that the MessageBus has to have been initialized before accessing these singletons. 
3. A simple Spring Security authentication service is provided that delegates to the configured AuthenticationManager. Only basic user properties are transfered thus this is more suited for testing and learning rather than production use, depending on your use case.
4. Spring Security annotations (@Secured, @PreAuthorize) and JSR250 annotations (@RolesAllowed) can be used on bus services and services the Errai rest client communicates with. This requires using the SpringSecurityMessageCallbackWrapper for exception translation (see below tables of classes) and registering the relevant AuthenticationEntryPoint classes if the Spring security filter also covers the ServerMessageBus (Errai servlets).


## Useful classes
| Classes  | Description |
| -------- | ----------- |
| ErraiApplicationLister  | Core integration with message bus, registering @Service annotated classes with the message bus. |
| ErraiRequestDispatcherFactoryBean  | Exposes Errai request dispatcher to Spring beans.  |
| ErraiServerMessageBusFactoryBean  | Exposes Errai ServerMessageBus to Spring beans.  |
| MessageCallbackWrapper  | Used by ErraiApplicationLister to wrap all MessageCallback implementations (all messagebus services use this base interface, even RPC). Can globally intercept calls to message callbacks which is used internally to map Spring Security Exceptions to exceptions the Bus can send to the client. |
| SpringSecurityMessageCallbackWrapper  | Handles translation of Spring Security exceptions to Errai Security exceptions. This is necessary if you want to use Spring Security annotations on Errai bus services. |
| SpringSecurityAuthenticationService | A simple Errai authentication service that delegates to Spring Security. Spring GrantedAuthority classes are converted to Errai RoleImpl, although it can be subclassed to use a different Role implementation. Only basic user properties are transfered. Serves as an example implementation. |
| ErraiClientBusAuthenticationEntryPoint | Spring security AuthenticationEntryPoint that writes a UnauthenticatedException response to the client message bus. Use in java config with .exceptionHandling().defaultAuthenticationEntryPointFor(new ErraiClientBusAuthenticationEntryPoint(),clientBusMatcher). See sample Spring war app. |
| ErraiRestClientAuthenticationEntryPoint | Spring security AuthenticationEntryPoint that writes a UnauthenticatedException response to the errai Rest client. Use in java config with .exceptionHandling().defaultAuthenticationEntryPointFor(new ErraiRestClientAuthenticationEntryPoint(),restClientMatcher). See sample Spring war app. |
| ErraiCsrfTokenRepository | An Errai CSRF friendly Spring CsrfTokenRepository. |
| ErraiCsrfAccessDeniedHandler | Will prompt a challenge when CSRF token missing. |
| RestrictedAccessAspect | A Spring applied AspectJ aspect which delegates to the Errai ServerSecurityRoleInterceptor to handle applying @RestrictedAccess. |
| SpringRequiredRolesExtractor | Used by RestrictedAccessAspect to handle Spring managed RequiredRolesProvider classes. | 


## Maven repository
The maven repository is hosted on bintray:

```xml
      <repository>
        <id>bintray-expansel-maven</id>
        <name>Expansel Bintray</name>
        <url>https://dl.bintray.com/expansel/maven</url>
      </repository>
```
Latest version:

[ ![Download](https://api.bintray.com/packages/expansel/maven/errai-spring-server/images/download.svg) ](https://bintray.com/expansel/maven/errai-spring-server/_latestVersion)


## Sample projects
### Spring war
Builds a standard java war project. This is the primary showcase for features.

<https://github.com/expansel/errai-spring-sample>


### Spring boot
A sample illustrating use in a Spring Boot app. [Status: Out of date]

<https://github.com/expansel/errai-spring-boot-sample>

