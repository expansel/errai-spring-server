package com.expansel.errai.spring.server;

import org.jboss.errai.bus.client.api.messaging.MessageCallback;

public class NoWrapMessageCallbackWrapper implements MessageCallbackWrapper {

    @Override
    public MessageCallback wrap(MessageCallback messageCallback) {
        return messageCallback;
    }

}
