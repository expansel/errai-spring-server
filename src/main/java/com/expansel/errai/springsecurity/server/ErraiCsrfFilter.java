package com.expansel.errai.springsecurity.server;

import org.jboss.errai.bus.server.servlet.CSRFTokenCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErraiCsrfFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private RequestMatcher requireCsrfProtectionMatcher;

    public ErraiCsrfFilter(RequestMatcher csrfProtectionMatcher) {
        Assert.notNull(csrfProtectionMatcher, "csrfProtectionMatcher cannot be null");
        this.requireCsrfProtectionMatcher = csrfProtectionMatcher;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if(requireCsrfProtectionMatcher.matches(request)) {
            if (CSRFTokenCheck.INSTANCE.isInsecure(request, logger)) {
                CSRFTokenCheck.INSTANCE.prepareResponse(request, response, logger);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Specifies a {@link RequestMatcher} that is used to determine if CSRF protection
     * should be applied. If the {@link RequestMatcher} returns true for a given request,
     * then CSRF protection is applied.
     *
     * <p>
     * The default is to apply CSRF protection for any HTTP method other than GET, HEAD,
     * TRACE, OPTIONS.
     * </p>
     *
     * @param requireCsrfProtectionMatcher the {@link RequestMatcher} used to determine if
     * CSRF protection should be applied.
     */
    public void setRequireCsrfProtectionMatcher(
            RequestMatcher requireCsrfProtectionMatcher) {
        Assert.notNull(requireCsrfProtectionMatcher, "requireCsrfProtectionMatcher cannot be null");
        this.requireCsrfProtectionMatcher = requireCsrfProtectionMatcher;
    }
}