package com.memozy.memozy_back.domain.memory.repository.querydsl;

import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemoryRepositoryCustom {
    Page<Memory> searchByKeyword(Long ownerId, SearchType searchType, String keyword, Pageable pageable);
}
