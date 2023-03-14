package com.advanced.restapi.configs;

import com.advanced.restapi.account.AccountService;
import com.advanced.restapi.filter.CustomAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    AccountService accountService;
    @Autowired
    Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
                .antMatchers("/*/login", "/*/signup").permitAll()
                .and()
                .addFilter(getAuthenticationFilter())
                .addFilterBefore(new CustomAuthenticationFilter(authenticationManager(), env),
                                UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().disable();
    }

    private CustomAuthenticationFilter getAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter authenticationFilter =
                new CustomAuthenticationFilter(authenticationManager(), env);
        return authenticationFilter;
    }

}