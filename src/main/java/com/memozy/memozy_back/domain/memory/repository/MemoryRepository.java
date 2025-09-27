package com.memozy.memozy_back.domain.memory.repository;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.repository.querydsl.MemoryRepositoryCustom;
import com.memozy.memozy_back.domain.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface MemoryRepository extends JpaRepository<Memory, Long>, MemoryRepositoryCustom {

    @Query("""
        select distinct m
        from Memory m
        left join fetch m.memoryItems i
        where m.owner.id = :userId
        """)
    List<Memory> findAllByOwnerIdWithItems(@Param("userId") Long userId);

    @Query("""
        select distinct m
        from Memory m
        left join fetch m.memoryItems i
        left join fetch m.accesses a
        left join fetch a.user u
        where m.id = :memoryId
    """)
    Optional<Memory> findByIdWithAccessesAndItems(@Param("memoryId") Long memoryId);

    void deleteByOwner(User user);

    @Query("""
      select m from Memory m
      left join fetch m.memoryItems mi
      where m.owner.id = :ownerId
      order by m.createdAt desc
    """)
    List<Memory> findLatestWithItemsByOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}