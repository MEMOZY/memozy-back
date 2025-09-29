package com.memozy.memozy_back.global.jwt;

import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperty jwtProperty;
    private Key accesskey;
    private Key refreshKey;
    private Integer accessExpired;
    private Integer refreshExpired;

    @PostConstruct
    public void init() {
        byte[] accessKeyBytes = jwtProperty.getAccessKey().getBytes(StandardCharsets.UTF_8);
        byte[] refreshKeyBytes = jwtProperty.getRefreshKey().getBytes(StandardCharsets.UTF_8);
        accesskey = Keys.hmacShaKeyFor(accessKeyBytes);
        refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        accessExpired = jwtProperty.getAccessExpiredMin();
        refreshExpired = jwtProperty.getRefreshExpiredDay();
    }

    public TokenCollection createTokenCollection(TokenInfo tokenInfo) {
        return TokenCollection.of(
                createAccessToken(tokenInfo),
                createRefreshToken(tokenInfo)
        );
    }

    public String createAccessToken(TokenInfo tokenInfo) {
        return Jwts.builder()
                .setSubject("access_token")
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(tokenInfo.getPayload())
                .setExpiration(Date.from(Instant.now().plus(accessExpired, ChronoUnit.MINUTES)))
                .signWith(accesskey)
                .compact();
    }

    public String createRefreshToken(TokenInfo tokenInfo) {
        return Jwts.builder()
                .setSubject("refresh_token")
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(tokenInfo.getPayload())
                .setExpiration(Date.from(Instant.now().plus(refreshExpired, ChronoUnit.DAYS)))
                .signWith(refreshKey)
                .compact();
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        try {
            Claims claims = getRefreshTokenBody(refreshToken);
            return Long.parseLong(claims.get("userId").toString());
        } catch (ExpiredJwtException e) {
            throw new GlobalException(e, ErrorCode.EXPIRED_REFRESH_TOKEN_EXCEPTION);
        } catch (Exception e) {
            throw new GlobalException(e, ErrorCode.INVALID_REFRESH_TOKEN_EXCEPTION);
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            return !getRefreshTokenBody(refreshToken)
                    .getExpiration()
                    .before(new Date());
        } catch (SecurityException | MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            throw new GlobalException(e, ErrorCode.INVALID_REFRESH_TOKEN_EXCEPTION);
        } catch (UnsupportedJwtException e) {
            throw new GlobalException(e, ErrorCode.UNSUPPORTED_JWT_TOKEN_EXCEPTION);
        } catch (ExpiredJwtException e) {
            throw new GlobalException(e, ErrorCode.EXPIRED_REFRESH_TOKEN_EXCEPTION);
        }
    }

    private Claims getRefreshTokenBody(String refreshToken) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
    }

}