package com.memozy.memozy_back.domain.memory.repository.querydsl;

import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.CalendarFilter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemoryRepositoryCustom {
    Page<Memory> searchByKeyword(Long ownerId, SearchType searchType, String keyword, Pageable pageable);
    Page<Long> findAccessibleMemoryIds(Long userId, Pageable pageable);
    List<Long> findAccessibleMemoryIdsAll(Long userId, CalendarFilter filter);
}
