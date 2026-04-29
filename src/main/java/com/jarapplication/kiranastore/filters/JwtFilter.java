package com.jarapplication.kiranastore.filters;

import static com.jarapplication.kiranastore.constants.LogConstants.*;
import static com.jarapplication.kiranastore.constants.SecurityConstants.AUTHORIZATION;
import static com.jarapplication.kiranastore.constants.SecurityConstants.TOKEN_PREFIX;

import com.jarapplication.kiranastore.feature_users.service.CustomUserDetailsService;
import com.jarapplication.kiranastore.response.ApiResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (request.getServletPath().equals("/login")
                    || request.getServletPath().equals("/register")
                    || request.getServletPath().startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }

            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_NO_JWT);
                return;
            }

            String token = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isValidateToken(token)) {
                    List<SimpleGrantedAuthority> authorities =
                            roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_INVALID_JWT);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(HttpStatus.METHOD_NOT_ALLOWED.name());
            apiResponse.setError(INVALID_OR_EXPIRED_JWT);
            response.getWriter().write(apiResponse.toString());
        }
    }
}
