package com.memozy.memozy_back.domain.auth.dto.response;

import com.memozy.memozy_back.global.jwt.TokenCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoginResponse(

        @Schema(description = "Access 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzaXhnYWV6emFuZyIsImlhdCI6MTcwODc3OTc1OSwiZXhwIjoxNzA4NzgzMzU5LCJ1c2VyX2lkIjozfQ.KA5BjtJtyYA-SEHKn_E4hTwz6YhZ2rMXsnIflL5zoa_GjBi4KTRiYbveBhUBKq1q4Qfb1HgRg-vy4G9YIg9MVQ")
        @NotNull String accessToken,

        @Schema(description = "Refresh 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzaXhnYWV6emFuZyIsImlhdCI6MTcwODc3OTc1OSwiZXhwIjoxNzA5Mzg0NTU5LCJ1c2VyX2lkIjozfQ.lDZaPmXFWfN6z4p6pWdNMA8coTxGNJjadC_X1liEMsZRkote7NaD4cemZnU5ag3tgH5y0SOeXTJkBO11aENVnw")
        @NotNull String refreshToken) {

    public static LoginResponse from(TokenCollection tokenCollection) {
        return new LoginResponse(
                tokenCollection.getAccessToken(),
                tokenCollection.getRefreshToken());
    }
}