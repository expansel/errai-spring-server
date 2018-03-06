package com.expansel.errai.spring.server;

import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import com.expansel.errai.springsecurity.server.SpringSecurityMessageCallbackWrapper;

/**
 * <p>This enables wrapping message callbacks created by the {@link ErraiApplicationListener}
 * allowing one to intercept calls to them. See the {@link SpringSecurityMessageCallbackWrapper} 
 * as an example which enables Spring Security annotations on Errai bus services by converting 
 * Spring security exceptions to Errai security exceptions.</p> 
 *
 * @author Zach Visagie
 */
public interface MessageCallbackWrapper {

    public MessageCallback wrap(MessageCallback messageCallback);
}
