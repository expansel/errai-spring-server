package com.expansel.errai.springsecurity.server;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * <p>This class provides a simple AuthenticationService implementation. It can serve as 
 * an example or used as is.</p>
 * 
 * <p>It uses the default Errai UserImpl and RoleImpl classes for users and roles, 
 * but you can use your own by subclassing and overriding the userFromAuthentication() 
 * and authoritiesToErraiRoles() methods.</p>
 * 
 * <p>This is also meant as an example, as it is simple enough to create a completely new 
 * implementation.</p>
 *
 *
 * @author Zach Visagie
 */
@Service
@Component
public class SpringSecurityAuthenticationService implements AuthenticationService {
    private AuthenticationManager authenticationManager;
    private HttpSession session;
    
    public SpringSecurityAuthenticationService(AuthenticationManager authenticationManager, HttpSession session) {
        super();
        this.authenticationManager = authenticationManager;
        this.session = session;
    }

    @Override
    public User login(String username, String password) {
        if (isLoggedIn()) {
            return getUser();
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(token);
        return userFromAuthentication(auth);
    }

    /**
     * This method can be overridden if there is a need for a different errrai User implementation class.
     * 
     * @param auth
     * @return
     */
    protected User userFromAuthentication(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        Collection<? extends Role> erraiRoles = authoritiesToErraiRoles(authorities);
        User user = new UserImpl(auth.getName(), erraiRoles);
        return user;
    }

    /**
     * Subclass and override this method to provide your own Errai Role implementation. 
     * Note overriding userFromAuthentication will result in this method not being called for 
     * role mapping.
     *  
     * @param authorities
     * @return
     */
    protected Collection<? extends Role> authoritiesToErraiRoles(Collection<? extends GrantedAuthority> authorities) {
        ArrayList<RoleImpl> erraiRoles = new ArrayList<RoleImpl>(authorities.size());
        for (GrantedAuthority grantedAuthority : authorities) {
            erraiRoles.add(new RoleImpl(grantedAuthority.getAuthority()));
        }
        return erraiRoles;
    }

    @Override
    public boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    @Override
    public void logout() {
        // clear spring security authentication which effectively logs one out
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        session.invalidate();
        throw new UnauthenticatedException();
    }

    @Override
    public User getUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return userFromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        }
        return User.ANONYMOUS;
    }
}
