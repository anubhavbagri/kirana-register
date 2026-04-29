package com.jarapplication.kiranastore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarapplication.kiranastore.filters.JwtFilter;
import com.jarapplication.kiranastore.response.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/login", "/register", "/actuator/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        exceptionHandlingConfigurer -> {
                            // if the user role is not matched, the exception is handled from here
                            exceptionHandlingConfigurer.accessDeniedHandler(
                                    (request, res, e) -> {
                                        String bearerToken = request.getHeader("Authorization");
                                        ApiResponse apiResponse = new ApiResponse();
                                        apiResponse.setSuccess(false);
                                        apiResponse.setErrorCode("403a");
                                        apiResponse.setErrorMessage("Access Denied");
                                        res.setStatus(403);
                                        ObjectMapper mapper = new ObjectMapper();
                                        res.getWriter()
                                                .write(mapper.writeValueAsString(apiResponse));
                                    });
                            // if the user auth fails, the exception is handled from here
                            exceptionHandlingConfigurer.authenticationEntryPoint(
                                    (request, res, e) -> {
                                        ApiResponse apiResponse = new ApiResponse();
                                        apiResponse.setSuccess(false);
                                        apiResponse.setErrorCode("403b");
                                        apiResponse.setErrorMessage("User is not authorized");
                                        ObjectMapper mapper = new ObjectMapper();
                                        res.setStatus(403);
                                        res.getWriter()
                                                .write(mapper.writeValueAsString(apiResponse));
                                    });
                        })
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
