package com.memozy.memozy_back.domain.memory.repository;

import com.memozy.memozy_back.domain.memory.domain.MemoryAccess;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemoryAccessRepository extends JpaRepository<MemoryAccess, Long> {

    // 내가 접근 가능한(공유받은) 메모리 + 아이템까지 fetch
    @Query("""
        select distinct ma
        from MemoryAccess ma
        join fetch ma.memory m
        left join fetch m.memoryItems i
        where ma.user.id = :userId
        """)
    List<MemoryAccess> findAllByUserIdWithMemoryAndItems(@Param("userId") Long userId);
}