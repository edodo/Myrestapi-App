package com.advanced.restapi.filter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.advanced.restapi.account.Account;
import com.advanced.restapi.account.AccountAdapter;
import com.advanced.restapi.account.AccountService;
import com.advanced.restapi.common.RequestLogin;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
// AuthenticationFilter는 Access Token을 생성하는 역할
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AccountService accountService;
    private Environment env;

    public CustomAuthenticationFilter() {
    }

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      Environment env) {
        this.env = env;
        super.setAuthenticationManager(authenticationManager);
    }

    //http://localhost:8080/login 로그인할때 호출됨
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {
        try {
            RequestLogin requestLogin =
                    new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLogin.getEmail(),
                            requestLogin.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    //로그인 성공하면 인증토큰 생성하기기
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        AccountAdapter accountAdapter = (AccountAdapter)authResult.getPrincipal();
        Account account = accountAdapter.getAccount();
        String token = Jwts.builder()
                .setSubject(String.valueOf(account.getAccountId()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512,
                        env.getProperty("token.secret"))
                .compact();
        response.addHeader("token", token);
    }

}