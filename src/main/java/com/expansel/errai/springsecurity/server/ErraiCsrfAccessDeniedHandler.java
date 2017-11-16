package com.expansel.errai.springsecurity.server;

import org.jboss.errai.bus.server.servlet.CSRFTokenCheck;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.jboss.errai.common.client.framework.Constants.ERRAI_CSRF_TOKEN_HEADER;

public class ErraiCsrfAccessDeniedHandler extends AccessDeniedHandlerImpl {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // When missing or providing an invalid CSRF token we will prompt a challenge.
        if (accessDeniedException instanceof InvalidCsrfTokenException) {
            final HttpSession session = request.getSession(false);
            if (session == null) {
                throw new IllegalStateException("Cannot create CSRF token challenge when session is null.");
            }

            // TODO: Replace with CSRFTokenCheck.CSRF_TOKEN_ATTRIBUTE_NAME in 4.1.1.Final
            final String token = (String) session.getAttribute("errai.bus.csrf_token"/*CSRFTokenCheck.CSRF_TOKEN_ATTRIBUTE_NAME*/);
            response.setHeader(ERRAI_CSRF_TOKEN_HEADER, token);
        }

        super.handle(request, response, accessDeniedException);
    }
}
