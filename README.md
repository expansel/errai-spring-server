# errai-spring-server
This library is aimed at making Errai and Spring play together on the server side as in without a CDI environment. Currently it has mainly been tested in a Spring boot environment and has not seen production use.

Supported features:
1. Register @Service classes managed and wired by Spring with the ServerMessageBus. This includes MessageCallback implementations, RPC classes and @Command annotated methods. Methods annotated with @Service are not currently supported. Note however that all services are treated as Singletons except for RPC classes which may use other Spring scopes such as prototype and session.
2. The Errai ServerMessageBus and RequestDispatcher can be autowired by registering the relevant FactoryBean classes with Spring. Note however that the MessageBus has to have been initialized before accessing these singletons.
3. A simple Spring Security authentication service is provided that delegates to the configured AuthenticationManager. Only basic user properties are transfered thus this is more suited for testing and discovery rather than production use.

