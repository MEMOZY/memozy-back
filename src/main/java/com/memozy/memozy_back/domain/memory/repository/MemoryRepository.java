package com.memozy.memozy_back.domain.memory.repository;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findAllByOwnerId(Long ownerId);

    @Query("""
        SELECT m FROM Memory m
        JOIN m.sharedUsers s
        WHERE s.user.id = :userId
    """)
    List<Memory> findAllSharedByUser(Long userId);

    void deleteByOwner(User user);

    @Query("""
      select m from Memory m
      left join fetch m.memoryItems mi
      where m.owner.id = :ownerId
      order by m.createdAt desc
    """)
    List<Memory> findLatestWithItemsByOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}