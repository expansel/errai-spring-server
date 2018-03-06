package com.expansel.errai.springsecurity.server;

import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import com.expansel.errai.spring.server.ErraiApplicationListener;
import com.expansel.errai.spring.server.MessageCallbackWrapper;

/**
 * <p>Wraps a {@link SpringSecurityMessageCallback} so that the {@link ErraiApplicationListener}
 * can wrap MessageCallbacks in order to handle spring exception mapping.</p>
 *
 *
 * @author Zach Visagie
 */
public class SpringSecurityMessageCallbackWrapper implements MessageCallbackWrapper {

    @Override
    public MessageCallback wrap(MessageCallback messageCallback) {
        return new SpringSecurityMessageCallback(messageCallback);
    }
    
}
