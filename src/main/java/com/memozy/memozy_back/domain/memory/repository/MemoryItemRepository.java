package com.memozy.memozy_back.domain.memory.repository;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.repository.querydsl.MemoryItemRepositoryCustom;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemoryItemRepository extends JpaRepository<MemoryItem, Long>, MemoryItemRepositoryCustom {
    @Query("""
        select i
        from MemoryItem i
        where i.memory.id = :memoryId
        order by i.sequence asc, i.id asc
    """)
    List<MemoryItem> findAllByMemoryIdOrderBySequence(@Param("memoryId") Long memoryId);

    @Modifying
    @Query("delete from MemoryItem i where i.memory.id = :memoryId")
    void deleteByMemoryId(@Param("memoryId") Long memoryId);
}
