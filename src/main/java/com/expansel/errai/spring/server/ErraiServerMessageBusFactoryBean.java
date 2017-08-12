package com.expansel.errai.spring.server;

import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean to enable Autowiring/Injection of the Errai ServerMessageBus.
 * 
 * Note that it will only be available once the Errai MessageBus has been
 * initialized.
 * 
 * @author Zach Visagie
 *
 */
public class ErraiServerMessageBusFactoryBean implements FactoryBean<ServerMessageBus> {
    private static final Logger logger = LoggerFactory.getLogger(ErraiServerMessageBusFactoryBean.class);

    @Override
    public ServerMessageBus getObject() throws Exception {
        logger.trace("Getting Errai ServerMessageBus");
        return ErraiServiceSingleton.getService().getBus();
    }

    @Override
    public Class<?> getObjectType() {
        return ServerMessageBus.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
