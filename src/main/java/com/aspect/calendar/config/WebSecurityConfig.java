package com.aspect.calendar.config;

import com.aspect.calendar.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    private final UserDetailsServiceImpl userDetailsService;

    WebSecurityConfig(UserDetailsServiceImpl userDetailsService ){
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();

        // The pages does not require login
        http.authorizeRequests()
                .antMatchers("/login", "/assets/**").permitAll()
                .anyRequest().authenticated()
                .and()

                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/j_spring_security_check")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error")
                .usernameParameter("login")
                .passwordParameter("password");

        // Config for Logout Page
        http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403");
        http.logout().
                permitAll()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true);

    }
}
