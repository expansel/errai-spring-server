package com.expansel.errai.springsecurity.server;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Ben Dol
 */
public class ErraiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .csrfTokenRepository(new ErraiCsrfTokenRepository())
        .and()
            .exceptionHandling().accessDeniedHandler(new ErraiCsrfAccessDeniedHandler());
    }
}
