package com.memozy.memozy_back.domain.memory.repository.querydsl;


import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.QMemory;
import com.memozy.memozy_back.domain.memory.domain.QMemoryAccess;
import com.memozy.memozy_back.domain.memory.domain.QMemoryItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoryRepositoryImpl implements MemoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Page<Memory> searchByKeyword(final Long userId, final SearchType searchType, final String keyword, final Pageable pageable) {
        QMemory m = QMemory.memory;
        QMemoryItem mi = QMemoryItem.memoryItem;
        QMemoryAccess ma = QMemoryAccess.memoryAccess;

        BooleanExpression accessibleByUser =
                m.owner.id.eq(userId).or(
                        JPAExpressions.selectOne()
                                .from(ma)
                                .where(
                                        ma.memory.id.eq(m.id),
                                        ma.user.id.eq(userId)
                                )
                                .exists()
                );

        if (keyword == null || keyword.trim().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String esc = escapeLike(keyword);
        String pattern = "%" + esc + "%";

        BooleanExpression titleLike =
                Expressions.booleanTemplate("{0} LIKE {1} ESCAPE '!'", m.title, pattern);

        BooleanExpression contentLike =
                Expressions.booleanTemplate("{0} LIKE {1} ESCAPE '!'", mi.content, pattern);

        BooleanExpression itemExists =
                JPAExpressions
                        .selectOne()
                        .from(mi)
                        .where(
                                mi.memory.id.eq(m.id),
                                contentLike
                        )
                        .exists();

        BooleanExpression keywordPredicate = switch (searchType) {
            case TITLE   -> titleLike;
            case CONTENT -> itemExists;
            case ALL     -> titleLike.or(itemExists);
        };

        BooleanExpression whereClause = accessibleByUser.and(keywordPredicate);

        List<Memory> contents = queryFactory
                .selectFrom(m)
                .where(whereClause)
                .orderBy(m.createdAt.desc(), m.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(m.id.count())
                .from(m)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Long> findAccessibleMemoryIds(Long userId, Pageable pageable) {
        QMemory m = QMemory.memory;
        QMemoryAccess ma = QMemoryAccess.memoryAccess;

        BooleanExpression accessible =
                m.owner.id.eq(userId)
                        .or(JPAExpressions
                                .selectOne()
                                .from(ma)
                                .where(ma.user.id.eq(userId)
                                        .and(ma.memory.id.eq(m.id)))
                                .exists());

        List<Long> ids = queryFactory
                .select(m.id)
                .from(m)
                .where(accessible)
                .orderBy(
                        m.endDate.desc().nullsLast(),
                        m.startDate.desc().nullsLast(),
                        m.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(m.id.countDistinct())
                        .from(m)
                        .where(accessible)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(ids, pageable, total);
    }

    @Override
    public List<Memory> findMemoriesWithItemsByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();

        QMemory m = QMemory.memory;
        QMemoryItem mi = QMemoryItem.memoryItem;

        return queryFactory
                .selectFrom(m).distinct()
                .leftJoin(m.memoryItems, mi).fetchJoin()
                .where(m.id.in(ids))
                .orderBy(
                        m.endDate.desc().nullsLast(),
                        m.startDate.desc().nullsLast(),
                        m.id.desc(),
                        mi.id.asc()
                )
                .fetch();
    }


    private String escapeLike(String s) {
        if (s == null) return "";
        return s.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .trim();
    }
}