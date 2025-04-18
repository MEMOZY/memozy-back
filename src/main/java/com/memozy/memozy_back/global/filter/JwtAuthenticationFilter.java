package com.memozy.memozy_back.global.filter;

import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.http.HeaderTokenExtractor;
import com.memozy.memozy_back.global.jwt.JwtResolver;
import com.memozy.memozy_back.global.security.PrincipalDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final PrincipalDetailsService principalDetailsService;
    private final HeaderTokenExtractor headerTokenExtractor;
    private final JwtResolver jwtResolver;

    private static final List<AntPathRequestMatcher> whiteListPatterns = List.of(
            new AntPathRequestMatcher("/actuator/**"),
            new AntPathRequestMatcher("/swagger-resources/**"),
            new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/api-docs/**"),
            new AntPathRequestMatcher("/webjars/**"),
            new AntPathRequestMatcher("/docs/**"),
            new AntPathRequestMatcher("/h2-console/**"),
            new AntPathRequestMatcher("/favicon.ico")
    );

    private static final List<AntPathRequestMatcher> whiteListPatternsForApi = List.of(
            new AntPathRequestMatcher("/auth/social/**")
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String accessToken = headerTokenExtractor.extractAccessToken(request);
        checkAccessTokenValidation(accessToken);
        setAuthenticationInSecurityContext(accessToken);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        List<AntPathRequestMatcher> skipList = new ArrayList<>();
        skipList.addAll(whiteListPatterns);
        skipList.addAll(whiteListPatternsForApi);

        OrRequestMatcher orRequestMatcher = new OrRequestMatcher(new ArrayList<>(skipList));
        return orRequestMatcher.matches(request);
    }

    private void checkAccessTokenValidation(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN_EXCEPTION);
        }
        if (!jwtResolver.validateAccessToken(accessToken)) {
            log.warn("JWT Token is not validate : [{}]", accessToken);
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN_EXCEPTION);
        }
    }

    private void setAuthenticationInSecurityContext(String accessToken) {
        try {
            Long userId = jwtResolver.getUserIdFromAccessToken(accessToken);
            UserDetails userDetails =
                    principalDetailsService.loadUserByUsername(userId.toString());
            Authentication authentication = getAuthenticationFromUserDetails(userDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException e) {
            throw new BusinessException(e, ErrorCode.INVALID_ACCESS_TOKEN_EXCEPTION);
        }
    }

    private static UsernamePasswordAuthenticationToken getAuthenticationFromUserDetails(
            UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities());
    }
}

