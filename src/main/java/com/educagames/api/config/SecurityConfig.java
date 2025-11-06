package com.educagames.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.educagames.api.filter.JwtFilter;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

/**
 * Configuração de segurança da aplicação.
 * Define políticas de autenticação e autorização.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
        CustomAuthenticationEntryPoint authenticationEntryPoint,
        CorsConfigurationSource corsConfigurationSource
    ) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Declara o bean do filtro JWT.
     *
     * @param jwtUtil utilitário para manipulação e validação de tokens
     * @param cookieUtil utilitário para leitura de cookies
     * @return instância configurada de JwtFilter
     */
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil, CookieUtil cookieUtil) {
        return new JwtFilter(jwtUtil, cookieUtil);
    }

    /**
     * Configuração da cadeia de filtros de segurança.
     * Usa {@link PublicEndpoints} para centralizar definição de rotas públicas.
     *
     * @param http HttpSecurity do Spring Security
     * @param jwtFilter filtro customizado de validação JWT
     * @return SecurityFilterChain configurado
     * @throws Exception em caso de erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Usa a lista centralizada de endpoints públicos
                .requestMatchers(PublicEndpoints.PUBLIC_ENDPOINTS.toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para criptografia de senhas usando BCrypt
     *
     * @return instância de PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
