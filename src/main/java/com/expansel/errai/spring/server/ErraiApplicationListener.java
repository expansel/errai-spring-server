package com.expansel.errai.spring.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.RPCEndpointFactory;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.io.ServiceInstanceProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.util.NotAService;
import org.jboss.errai.bus.server.util.ServiceTypeParser;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Spring {@link BeanFactoryPostProcessor} and Application Event listener to subscribe
 * Spring managed @Service objects with the Errai MessageBus. This is based on 
 * CDIExtensionPoints and ServiceProcessor classes that are used in CDI and 
 * non CDI environments respectively.
 * </p>
 * 
 * <p>
 * Typically one needs to setup the Errai DefaultBlockingServlet to not auto
 * register services with the <code>auto-discover-services</code> init param set
 * to <code>false</code> and not have the Errai CDI dependencies loaded.
 * </p>
 * 
 * <p>
 * It currently only supports classes annotated with @Service and not methods.
 * It does support service classes where the methods are annotated
 * with @Command.
 * </p>
 * 
 * <p>
 * Example for spring boot servlet registration:
 * </p>
 * <pre>
 * &#64;Bean
 * public ServletRegistrationBean servletRegistrationBean() {
 *     logger.info("Registering Errai Servlet");
 *     ServletRegistrationBean registration = new ServletRegistrationBean(new DefaultBlockingServlet(), "*.erraiBus");
 *     registration.addInitParameter("auto-discover-services", "false");
 *     registration.setLoadOnStartup(1);
 *     return registration;
 * }
 *
 * </pre>
 * 
 * 
 * @author Zach Visagie
 *
 */
@Component
public class ErraiApplicationListener implements BeanFactoryPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ErraiApplicationListener.class);
    private List<ServiceImplementation> services = new ArrayList<ServiceImplementation>();
    private MessageCallbackWrapper messageCallbackWrapper;
    
    public ErraiApplicationListener() {
        this(null); // null uses default
    }
    
    public ErraiApplicationListener(MessageCallbackWrapper messageCallbackWrapper) {
        this.messageCallbackWrapper = messageCallbackWrapper;
        if(messageCallbackWrapper == null) {
            messageCallbackWrapper = new NoWrapMessageCallbackWrapper();
        }
    }
    
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("ContextClosedEvent");
        unsubscribeAll();
    }

    private void unsubscribeAll() {
        MessageBus bus = ErraiServiceSingleton.getService().getBus();
        if (bus != null) {
            for (ServiceImplementation serviceImplementation : services) {
                String subject = serviceImplementation.getSubject();
                logger.info("Unsubscribing " + subject);
                bus.unsubscribeAll(subject);
            }
        }
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("ContextRefreshedEvent");

        ApplicationContext applicationContext = event.getApplicationContext();
        logger.info("Found " + services.size() + " services in " + Integer.toHexString(System.identityHashCode(applicationContext)) + ". Is it already active? " + ErraiServiceSingleton.isActive());
        
        if(ErraiServiceSingleton.isActive()) {
            logger.info("Already initialized so first unsubscribing current services, before reregistering");
            unsubscribeAll();
        }
        ErraiServiceSingleton.registerInitCallback(new ErraiServiceSingleton.ErraiInitCallback() {
            @SuppressWarnings("rawtypes")
            @Override
            public void onInit(ErraiService service) {
                logger.info("Subscribing " + services.size() + " services.");
                for (ServiceImplementation serviceImplementation : services) {
                    String subject = serviceImplementation.getSubject();
                    MessageBus bus = ErraiServiceSingleton.getService().getBus();
                    if(bus.isSubscribed(subject)) {
                        logger.info("Unsubscribing " + subject);
                        bus.unsubscribeAll(subject);
                    }
                    subscribe(applicationContext, service, serviceImplementation);
                }
            }
        });
    }

    public static class ServiceImplementation {
        private ServiceTypeParser serviceTypeParser;
        private String objectName;

        public ServiceImplementation(ServiceTypeParser serviceTypeParser, String objectName) {
            super();
            this.serviceTypeParser = serviceTypeParser;
            this.objectName = objectName;
        }

        public ServiceTypeParser getServiceTypeParser() {
            return serviceTypeParser;
        }

        public String getBeanName() {
            return objectName;
        }

        public boolean isRPC() {
            return serviceTypeParser.getRemoteImplementation() != null;
        }

        public String getSubject() {
            // not sure why ServiceTypeParser does not return correct RPC name
            Class<?> remoteInterface = serviceTypeParser.getRemoteImplementation();
            if (remoteInterface != null) {
                return remoteInterface.getName() + ":RPC";
            }
            return serviceTypeParser.getServiceName();
        }
    }

    @SuppressWarnings("rawtypes")
    private void subscribe(final ApplicationContext applicationContext, final ErraiService service,
            final ServiceImplementation serviceImplementation) {
        final ServerMessageBus bus = ErraiServiceSingleton.getService().getBus();
        ServiceTypeParser serviceTypeParser = serviceImplementation.getServiceTypeParser();

        if (!serviceImplementation.isRPC()) {
            String subject = serviceImplementation.getSubject();
            logger.info("Subscribing MessageCallback " + subject);
            // All the Errai supporting classes seem to be geared to having a
            // singleton here and Errai's CDI implementation also only supports
            // singletons for non-rpc's
            Object instance = applicationContext.getBean(serviceImplementation.getBeanName());
            final MessageCallback callback = serviceTypeParser.getCallback(instance);
            final MessageCallback wrappedCallback = messageCallbackWrapper.wrap(callback);
            if (callback != null) {
                if (serviceTypeParser.isLocal()) {
                    bus.subscribeLocal(subject, wrappedCallback);
                } else {
                    bus.subscribe(subject, wrappedCallback);
                }
            }
        } else {
            String subject = serviceImplementation.getSubject();
            logger.info("Subscribing RPC " + subject);
            final Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();
            final ServiceInstanceProvider serviceInstanceProvider = new ServiceInstanceProvider() {
                @Override
                public Object get(Message message) {
                    Object obj = applicationContext.getBean(serviceImplementation.getBeanName());
                    logger.info("obj instance: " + obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj)));
                    return obj;
                }
            };

            Class<?> remoteInterface = serviceTypeParser.getRemoteImplementation();
            for (final Method method : remoteInterface.getMethods()) {
                if (ProxyUtil.isMethodInInterface(remoteInterface, method)) {
                    MessageCallback rpcCallback = RPCEndpointFactory.createEndpointFor(serviceInstanceProvider, method, bus);
                    MessageCallback wrappedCallback = messageCallbackWrapper.wrap(rpcCallback);
                    epts.put(ProxyUtil.createCallSignature(remoteInterface, method), wrappedCallback);
                }
            }

            final RemoteServiceCallback delegate = new RemoteServiceCallback(epts);
            bus.subscribe(subject, new MessageCallback() {
                @Override
                public void callback(final Message message) {
                    delegate.callback(message);
                }
            });

            DefaultRemoteCallBuilder.setProxyFactory(Assert.notNull(new ProxyFactory() {
                @Override
                public <T> T getRemoteProxy(final Class<T> proxyType) {
                    throw new RuntimeException(
                            "There is not yet an available Errai RPC implementation for the server-side environment.");
                }
            }));
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("Look for Errai Service definitions");
        String[] beanNames = beanFactory.getBeanNamesForAnnotation(Service.class);
        for (String beanName : beanNames) {
            if(beanName.startsWith("scopedTarget.")) {
                logger.debug("Skipping scopedTarget bean: " + beanName);
                // scoped proxy classes are registered with an internal name and we should only register the proxies as services
                // See org.springframework.aop.scope.ScopedProxyUtils
                continue;
            }
            Class<?> beanType = beanFactory.getType(beanName);
            try {
                ServiceTypeParser serviceTypeParser = new ServiceTypeParser(beanType);
                services.add(new ServiceImplementation(serviceTypeParser, beanName));
                logger.debug("Found Errai Service definition: beanName=" + beanName + ", beanType=" + beanType);
            } catch (NotAService e) {
                logger.warn("Service annotation present but threw NotAServiceException", e);
            }
        }
    }
}