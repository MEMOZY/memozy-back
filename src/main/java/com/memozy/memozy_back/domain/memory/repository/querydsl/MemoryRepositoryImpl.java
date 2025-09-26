package com.memozy.memozy_back.domain.memory.repository.querydsl;


import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.QMemory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoryRepositoryImpl implements MemoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public List<Memory> findMemoriesByKeyword(String keyword) {
        QMemory memory = QMemory.memory;
        return List.of();
    }
}