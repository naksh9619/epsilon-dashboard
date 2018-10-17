package com.inmobi.platform.elasticyarn.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user1").password("password1").roles("USER", "ADMIN")
                .and()
                .withUser("user2").password("password2").roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().disable().formLogin().permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/babar").hasAnyRole("USER", "ADMIN")
                .antMatchers("/countfetch").hasAnyRole("USER", "ADMIN")
                .antMatchers("/submityarnjar").hasAnyRole("USER", "ADMIN")
                .antMatchers("/submitsparkjar").hasAnyRole("USER", "ADMIN")
                .antMatchers("/adduser").hasRole("ADMIN")
                .antMatchers("/addgroup").hasRole("ADMIN")
                .anyRequest().authenticated().and().csrf().disable();
    }
}