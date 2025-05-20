package com.memozy.memozy_back.domain.memory.repository;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import java.util.List;
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

}