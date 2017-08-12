package com.expansel.errai.spring.server;

import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean to enable Autowiring of the Errai RequestDispatcher.
 * 
 * Note that it will only be available once the Errai MessageBus has been
 * initialized.
 * 
 * @author Zach Visagie
 *
 */
public class ErraiRequestDispatcherFactoryBean implements FactoryBean<RequestDispatcher> {
    private static final Logger logger = LoggerFactory.getLogger(ErraiRequestDispatcherFactoryBean.class);

    @Override
    public RequestDispatcher getObject() throws Exception {
        logger.trace("Getting Errai RequestDispatcher");
        return ErraiServiceSingleton.getService().getDispatcher();
    }

    @Override
    public Class<?> getObjectType() {
        return RequestDispatcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
