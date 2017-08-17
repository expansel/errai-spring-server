package com.expansel.errai.springsecurity.server;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Service
@Scope("session")
public class SpringSecurityAuthenticationService implements AuthenticationService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpSession session;

    @Override
    public User login(String username, String password) {
        if (isLoggedIn()) {
            return getUser();
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(token);
        return getUser(auth);
    }

    private User getUser(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        ArrayList<RoleImpl> erraiRoles = new ArrayList<RoleImpl>(authorities.size());
        for (GrantedAuthority grantedAuthority : authorities) {
            erraiRoles.add(new RoleImpl(grantedAuthority.getAuthority().replace("ROLE_", "")));
        }
        User user = new UserImpl(auth.getName(), erraiRoles);
        return user;
    }

    @Override
    public boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    @Override
    public void logout() {
        // logout is probably best handled with SpringSecurity logout url
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        session.invalidate();
    }

    @Override
    public User getUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return getUser(SecurityContextHolder.getContext().getAuthentication());
        }
        return User.ANONYMOUS;
    }
}
