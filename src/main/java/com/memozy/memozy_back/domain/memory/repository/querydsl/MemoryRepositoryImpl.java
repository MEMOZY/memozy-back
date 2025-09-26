package com.memozy.memozy_back.domain.memory.repository.querydsl;


import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.QMemory;
import com.memozy.memozy_back.domain.memory.domain.QMemoryItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoryRepositoryImpl implements MemoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Memory> searchByKeyword(final Long ownerId, final SearchType searchType, final String keyword, final Pageable pageable) {
        QMemory m = QMemory.memory;
        QMemoryItem mi = QMemoryItem.memoryItem;

        // 가드
        if (keyword == null || keyword.trim().isEmpty()) {
            List<Memory> content = queryFactory
                    .selectFrom(m)
                    .where(m.owner.id.eq(ownerId))
                    .orderBy(m.createdAt.desc(), m.id.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            Long total = queryFactory
                    .select(m.id.count())
                    .from(m)
                    .where(m.owner.id.eq(ownerId))
                    .fetchOne();

            return new PageImpl<>(content, pageable, total == null ? 0 : total);
        }

        String esc = escapeLike(keyword);
        String pattern = "%" + esc + "%";

        // 제목 LIKE (ESCAPE 필요하면 템플릿으로 유지)
        BooleanExpression titleLike =
                Expressions.booleanTemplate("{0} LIKE {1} ESCAPE '!'", m.title, pattern);

        // 내용 LIKE (item용)
        BooleanExpression contentLike =
                Expressions.booleanTemplate("{0} LIKE {1} ESCAPE '!'", mi.content, pattern);

        // ✅ 테이블명 대신 엔티티 기반 EXISTS 서브쿼리
        BooleanExpression itemExists =
                JPAExpressions
                        .selectOne()
                        .from(mi)
                        .where(
                                mi.memory.id.eq(m.id),
                                contentLike
                        )
                        .exists();

        // 검색 타입 분기
        BooleanExpression keywordPredicate = switch (searchType) {
            case TITLE   -> titleLike;
            case CONTENT -> itemExists;
            case ALL     -> titleLike.or(itemExists);
        };

        BooleanExpression whereClause = m.owner.id.eq(ownerId).and(keywordPredicate);

        // SELECT
        List<Memory> contents = queryFactory
                .selectFrom(m)
                .where(whereClause)
                .orderBy(m.createdAt.desc(), m.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // COUNT (동일 조건)
        Long total = queryFactory
                .select(m.id.count())
                .from(m)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    private String escapeLike(String s) {
        if (s == null) return "";
        return s.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .trim();
    }
}