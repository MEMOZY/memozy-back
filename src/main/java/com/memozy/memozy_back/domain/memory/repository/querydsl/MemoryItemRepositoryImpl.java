package com.memozy.memozy_back.domain.memory.repository.querydsl;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.QMemoryItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemoryItemRepositoryImpl implements MemoryItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, MemoryItem> findFirstItemsByMemoryIds(List<Long> memoryIds) {
        QMemoryItem mi = QMemoryItem.memoryItem;
        QMemoryItem mj = new QMemoryItem("mj"); // 서브쿼리 별칭

        // 같은 memory 안에서 (sequence, id) 기준으로 mi보다 "더 앞선" 게 존재하지 않는 행만 선택
        BooleanExpression isFirst =
                JPAExpressions
                        .selectOne()
                        .from(mj)
                        .where(
                                mj.memory.id.eq(mi.memory.id),
                                mj.sequence.lt(mi.sequence)
                                        .or(
                                                mj.sequence.eq(mi.sequence)
                                                        .and(mj.id.lt(mi.id))
                                        )
                        )
                        .notExists();

        List<MemoryItem> rows = queryFactory
                .selectFrom(mi)
                .where(mi.memory.id.in(memoryIds), isFirst)
                .fetch();

        Map<Long, MemoryItem> firstMap = new HashMap<>();
        for (MemoryItem it : rows) {
            firstMap.put(it.getMemory().getId(), it);
        }
        return firstMap;
    }
}