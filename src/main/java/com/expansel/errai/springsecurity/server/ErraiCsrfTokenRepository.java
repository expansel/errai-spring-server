package com.expansel.errai.springsecurity.server;

import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ErraiCsrfTokenRepository implements CsrfTokenRepository {

    private final static Logger logger = LoggerFactory.getLogger(ErraiCsrfTokenRepository.class);

    private String sessionAttributeName = ErraiCsrfTokenRepository.class
            .getName().concat(".CSRF_TOKEN");

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.web.csrf.CsrfTokenRepository#saveToken(org.
     * springframework .security.web.csrf.CsrfToken,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void saveToken(CsrfToken token, HttpServletRequest request,
                          HttpServletResponse response) {
        if (token == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(this.sessionAttributeName);
            }
        }
        else {
            HttpSession session = request.getSession();
            session.setAttribute(this.sessionAttributeName, token);

            // Assign the token the way Errai expects it
            // TODO: Replace "errai.bus.csrf_token" with CSRFTokenCheck.CSRF_TOKEN_ATTRIBUTE_NAME in 4.1.1.Final
            session.setAttribute("errai.bus.csrf_token", token.getToken());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.security.web.csrf.CsrfTokenRepository#loadToken(javax.servlet
     * .http.HttpServletRequest)
     */
    public CsrfToken loadToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (CsrfToken) session.getAttribute(this.sessionAttributeName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.web.csrf.CsrfTokenRepository#generateToken(javax.
     * servlet .http.HttpServletRequest)
     */
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(Constants.ERRAI_CSRF_TOKEN_HEADER, Constants.ERRAI_CSRF_TOKEN_VAR,
                SecureHashUtil.nextSecureHash());
    }
}
