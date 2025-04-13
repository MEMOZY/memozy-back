package com.memozy.memozy_back.global.config;

import com.memozy.memozy_back.domain.user.domain.UserRole;
import com.memozy.memozy_back.global.filter.JwtAccessDeniedHandler;
import com.memozy.memozy_back.global.filter.JwtAuthenticationFilter;
import com.memozy.memozy_back.global.filter.JwtExceptionFilter;
import com.memozy.memozy_back.global.http.HeaderTokenExtractor;
import com.memozy.memozy_back.global.jwt.JwtResolver;
import com.memozy.memozy_back.global.security.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtExceptionFilter jwtExceptionFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final HeaderTokenExtractor headerTokenExtractor;
    private final PrincipalDetailsService principalDetailsService;
    private final JwtResolver jwtResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(principalDetailsService, headerTokenExtractor, jwtResolver);

        http
                .formLogin(AbstractHttpConfigurer::disable) // ⛔ 기본 로그인 폼 제거
                .httpBasic(AbstractHttpConfigurer::disable) // ⛔ 기본 인증 방식 제거
                .csrf(AbstractHttpConfigurer::disable)      // ⛔ CSRF 제거 (JWT 기반이므로)
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // ✅ CORS 적용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 세션 상태 유지 X
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler) // ❗ 권한 없는 접근 처리
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // ✅ JWT 인증 필터
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class) // ✅ 예외 필터
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/admin/**"))
                        .hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(new AntPathRequestMatcher("/manager/**"))
                        .hasAnyAuthority(UserRole.ADMIN.name())
                        .anyRequest().permitAll() // 그 외 모든 요청 허용
                );

        return http.build();
    }
}