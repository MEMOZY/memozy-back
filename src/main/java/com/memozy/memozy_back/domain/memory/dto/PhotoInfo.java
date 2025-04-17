package com.memozy.memozy_back.domain.memory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PhotoInfo(
        @NotBlank String imageUrl,
        @NotNull LocalDateTime takenAt,
        @NotNull Integer sequence
) {

}
