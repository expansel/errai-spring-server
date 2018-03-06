package com.expansel.errai.springsecurity.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * <p>
 * This class can be used as an {@link AuthenticationEntryPoint} that will write
 * Errai {@link UnauthenticatedException} if directed from message bus requests.
 * This avoids Spring Security redirecting to login page which would return html
 * to requests from the client message bus.
 * </p>
 *
 * <p>
 * Example registration in spring security:
 * </p>
 * 
 * <pre>
 *       .exceptionHandling()
 *       // normal html login entry point
 *       .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
 *               notErraiBusMatcher)
 *       // client bus json response to generate security error client side
 *       .defaultAuthenticationEntryPointFor(new ErraiClientBusAuthenticationEntryPoint(),
 *               clientBusMatcher)
 *       // rest client json response to generate security error client side
 *       .defaultAuthenticationEntryPointFor(new ErraiRestClientAuthenticationEntryPoint(),
 *               restClientMatcher);
 * </pre>
 * 
 * @author Zach Visagie
 */
public class ErraiClientBusAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        String errorMsg = authException.getMessage() == null ? "" : authException.getMessage();
        String throwable = ServerMarshalling.toJSON(new UnauthenticatedException());
        String body = "[{\"ErrorTo\":\"ClientBusErrors\", \"ToSubject\":\"ClientBusErrors\", \"Throwable\":" + throwable + ", \"ErrorMessage\":\"" + errorMsg + "\"}]";
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(body);
        out.flush();
        out.close();
    }

}