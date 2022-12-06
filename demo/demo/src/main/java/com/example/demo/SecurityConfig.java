package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private MyUserDetailsService myUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.authorizeRequests()
				.antMatchers("/", "/index", "/login","/register").permitAll()	
				.anyRequest().authenticated()
				.and()
            .formLogin()
				.loginPage("/login")
				.defaultSuccessUrl("/me", true)
				.permitAll()
				.and()
			.logout();
      
        return http.build();
    }

	@Bean
	public AuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider = 
		new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(this.myUserDetailsService);
    return provider;
  	}
  
  	@Bean
  	public PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
  	}
    
}
