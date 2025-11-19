package quantify.BoticaSaid.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import quantify.BoticaSaid.jwt.JwtAuthenticationFilter;
import quantify.BoticaSaid.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TurnoCajaFilter turnoCajaFilter
    ) throws Exception {
        log.info("Cargando configuración de seguridad");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(java.util.List.of(
                            "http://localhost:3000", "http://37.60.233.86:6969", "http://localhost:8080",
                            "http://192.168.1.11:3000", "http://192.168.56.1:3000",
                            "http://51.161.10.179:3000", "http://51.161.10.179:5000", "http://51.161.10.179",
                            "https://109.199.106.139:3000", "http://109.199.106.139:3000",
                            "http://109.199.106.139", "https://109.199.106.139",
                            "http://109.199.106.139:5000", "http://boticasaid.quantify.net.pe", "https://boticasaid.quantify.net.pe","http://nuevaesperanza.quantify.net.pe","https://nuevaesperanza.quantify.net.pe"
                    ));
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                // Permitir que el H2 console sea renderizado en iframe
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .exceptionHandling(e -> e
                        .accessDeniedHandler((req, res, ex) -> {
                            var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            org.slf4j.LoggerFactory.getLogger("SEC-ACCESSDENIED").warn(
                                    "Denied {} {} principal={} authorities={}",
                                    req.getMethod(), req.getRequestURI(),
                                    a != null ? a.getName() : "null",
                                    a != null ? a.getAuthorities() : "null",
                                    ex
                            );
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Acceso denegado\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Reportes: solo autenticado (cualquier rol)
                        .requestMatchers("/api/reports/**").authenticated()

                        // Públicos
                        .requestMatchers(
                                "/h2-console/**",
                                "/auth/*",
                                "/rico/*",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yml",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html"
                        ).permitAll()

                        // Resto autenticado
                        .anyRequest().authenticated()
                )
                // Filtros
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(turnoCajaFilter, JwtAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}