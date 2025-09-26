package com.memozy.memozy_back.global.dto;

import java.util.List;

public record PagedResponse<T> (
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}