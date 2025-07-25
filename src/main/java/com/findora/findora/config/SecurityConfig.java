package com.findora.findora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.findora.findora.auth.filter.JwtAuthenticationFilter;
import com.findora.findora.common.CustomAccessDeniedHandler;
import com.findora.findora.common.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/check-nickname").permitAll()
                .requestMatchers("/api/users/check-loginid").permitAll()
                .requestMatchers("/api/users/send-email-code").permitAll()
                .requestMatchers("/api/users/*/verify-email").permitAll()
                .requestMatchers("/api/email/send-code").permitAll()
                .requestMatchers("/api/email/verify-code").permitAll()
                // 카테고리, 게시글 조회만 허용
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/category/{categoryId}").permitAll()
                // 게시글 작성/수정/삭제 인증 필요
                .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated()
                // 즐겨찾기 인증 필요
                .requestMatchers(HttpMethod.POST, "/api/bookmarks").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/bookmarks").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/bookmarks/user/me").authenticated()

                // 좋아요 인증 필요
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/likes/toggle").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/likes/status").authenticated()  // 로그인한 사용자의 좋아요 상태 조회
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/likes/count").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/likes/comments/{commentId}/count").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/likes/comments/{commentId}/toggle").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/likes/comments/{commentId}/status").authenticated() // 로그인한 사용자의 댓글 좋아요 상태 조회
                // Swagger, 정적 리소스 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs", "/swagger-ui.html", "/api-docs/**", "/swagger-resources/**", "/webjars/**", "/swagger").permitAll()
                .requestMatchers("/*.html", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(customAccessDeniedHandler)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            );
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin 명시적으로 설정 (개발 환경)
        configuration.addAllowedOrigin("http://localhost:3000");  // React 기본 포트
        configuration.addAllowedOrigin("http://localhost:3001");  // React 대체 포트
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:3001");
        configuration.addAllowedOrigin("http://localhost:5173");  // Vite 기본 포트
        configuration.addAllowedOrigin("http://127.0.0.1:5173");
        
        // 허용할 HTTP 메서드
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("OPTIONS");
        
        // 허용할 헤더
        configuration.addAllowedHeader("*");
        
        // 자격 증명 허용 (JWT 토큰 전송을 위해 필요)
        configuration.setAllowCredentials(true);
        
        // preflight 요청 캐시 시간 설정
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 