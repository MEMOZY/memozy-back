package com.memozy.memozy_back.domain.auth.service.provider;

import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import java.net.URI;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class ApplePublicKeyProvider {

    private static final String ISSUER = "https://appleid.apple.com";
    private static final String JWK_SET_URL = "https://appleid.apple.com/auth/keys";
    private final JWKSource<SecurityContext> keySource;

    public ApplePublicKeyProvider() {
        try {
            URL jwkSetUrl = URI.create(JWK_SET_URL).toURL();

            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(
                    2000,  // connect timeout (ms)
                    2000,  // read timeout (ms)
                    1024 * 1024 // size limit (1MB)
            );

            this.keySource = new RemoteJWKSet<>(jwkSetUrl, resourceRetriever);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ApplePublicKeyProvider", e);
        }
    }

    public Payload parseAndValidate(String idToken) {
        try {
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    JWSAlgorithm.RS256, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            JWTClaimsSet claimsSet = jwtProcessor.process(idToken, null);

            if (!ISSUER.equals(claimsSet.getIssuer())) {
                throw new GlobalException(ErrorCode.APPLE_INVALID_ISSUER);
            }

            String sub = claimsSet.getSubject();
            String email = claimsSet.getStringClaim("email");

            return new Payload(sub, email);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.APPLE_INVALID_TOKEN);
        }
    }

    public record Payload(String sub, String email) { }
}