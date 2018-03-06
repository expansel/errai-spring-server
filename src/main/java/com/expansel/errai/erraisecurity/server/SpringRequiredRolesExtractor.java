package com.expansel.errai.erraisecurity.server;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.roles.SharedRequiredRolesExtractorImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * A RequiredRolesExtractor which does not destroy provider instances.
 *
 *
 * @author Zach Visagie
 */
@Component
public class SpringRequiredRolesExtractor extends SharedRequiredRolesExtractorImpl {

    private ApplicationContext context;

    public SpringRequiredRolesExtractor(ApplicationContext context) {
        super();
        this.context = context;
    }

    @Override
    protected RequiredRolesProvider getProviderInstance(Class<? extends RequiredRolesProvider> providerType) {
        return context.getBean(providerType);
    }

    @Override
    protected void destroyProviderInstance(RequiredRolesProvider instance) {
        // Assuming their all singletons
    }

}