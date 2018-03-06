package com.expansel.errai.erraisecurity.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jboss.errai.security.server.ServerSecurityRoleInterceptor;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.springframework.stereotype.Component;

/**
 * This aspectj aspect is used to handle the Errai @RestrictedAccess annotation. It defers handling to the  
 * ServerSecurityRoleInterceptor to ensure handling happens the same way.
 * 
 * @author Zach Visagie
 */
@Aspect
@Component
public class RestrictedAccessAspect {
    private AuthenticationService authenticationService;
    private RequiredRolesExtractor roleExtractor;

    public RestrictedAccessAspect(AuthenticationService authenticationService, RequiredRolesExtractor roleExtractor) {
        super();
        this.authenticationService = authenticationService;
        this.roleExtractor = roleExtractor;
    }


    @Around("@annotation(org.jboss.errai.security.shared.api.annotation.RestrictedAccess)")
    public Object restrictAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        ServerSecurityRoleInterceptor interceptor = new ServerSecurityRoleInterceptor(authenticationService, roleExtractor);
        return interceptor.aroundInvoke(new ProxyInvocationContext(joinPoint));
    }
    
    public static class ProxyInvocationContext implements InvocationContext {
        private ProceedingJoinPoint joinPoint;
        private Object[] parametersToPass;
        
        public ProxyInvocationContext(ProceedingJoinPoint joinPoint) {
            super();
            this.joinPoint = joinPoint;
            parametersToPass = joinPoint.getArgs();
        }

        @Override
        public Object getTarget() {
            return joinPoint.getTarget();
        }

        @Override
        public Method getMethod() {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            return methodSignature.getMethod();
        }

        @Override
        public Object[] getParameters() {
            return parametersToPass;
        }

        @Override
        public void setParameters(Object[] params) {
            parametersToPass = params;
        }

        @Override
        public Map<String, Object> getContextData() {
            return new HashMap<String, Object>(0);
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Object proceed() throws Exception {
            try {
                return joinPoint.proceed(parametersToPass);
            } catch (Exception e) {
                throw e;
            } catch (Throwable t) {
                throw new Exception(t);
            }
        }
        
    }

}