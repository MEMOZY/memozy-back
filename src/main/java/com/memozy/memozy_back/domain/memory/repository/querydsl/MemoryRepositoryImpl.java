package com.memozy.memozy_back.domain.memory.repository.querydsl;


import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.QMemory;
import com.memozy.memozy_back.domain.memory.domain.QMemoryAccess;
import com.memozy.memozy_back.domain.memory.domain.QMemoryItem;
import com.memozy.memozy_back.domain.memory.dto.CalendarFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        QMemoryAccess maSub = QMemoryAccess.memoryAccess;      // 서브쿼리용
        QMemoryAccess maFetch = new QMemoryAccess("maFetch");  // fetch join용


        BooleanExpression accessibleByUser =
                m.owner.id.eq(userId).or(
                        JPAExpressions.selectOne()
                                .from(maSub)
                                .where(
                                        maSub.memory.id.eq(m.id),
                                        maSub.user.id.eq(userId)
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

        // 우선 id만 페이지네이션
        List<Long> ids = queryFactory
                .select(m.id)
                .from(m)
                .where(whereClause)
                .orderBy(m.createdAt.desc(), m.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 전체 개수
        Long total = queryFactory
                .select(m.id.count())
                .from(m)
                .where(whereClause)
                .fetchOne();

        // 해당 id들에 대해 Memory + accesses fetch join
        List<Memory> contents = queryFactory
                .selectDistinct(m)
                .from(m)
                .leftJoin(m.accesses, maFetch).fetchJoin()
                .where(m.id.in(ids))
                .fetch();

        // ids 순서대로 다시 정렬
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            order.put(ids.get(i), i);
        }

        contents.sort(Comparator.comparingInt(mem ->
                order.getOrDefault(mem.getId(), Integer.MAX_VALUE)
        ));

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Long> findAccessibleMemoryIds(Long userId, Pageable pageable) {
        QMemory m = QMemory.memory;
        QMemoryAccess ma = QMemoryAccess.memoryAccess;

        BooleanExpression accessible =
                m.owner.id.eq(userId)
                        .or(JPAExpressions.selectOne()
                                .from(ma)
                                .where(ma.user.id.eq(userId)
                                        .and(ma.memory.id.eq(m.id)))
                                .exists());

        List<Long> ids = queryFactory
                .select(m.id)
                .from(m)
                .where(accessible)
                .orderBy(m.endDate.desc().nullsLast(),
                        m.startDate.desc().nullsLast(),
                        m.id.desc())
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

    // 캘린더 화면에서 조회
    @Override
    public List<Long> findAccessibleMemoryIdsAll(Long userId, CalendarFilter f) {
        QMemory m = QMemory.memory;
        QMemoryAccess ma = QMemoryAccess.memoryAccess;

        BooleanExpression accessible =
                m.owner.id.eq(userId)
                        .or(JPAExpressions.selectOne()
                                .from(ma)
                                .where(ma.user.id.eq(userId)
                                        .and(ma.memory.id.eq(m.id)))
                                .exists());

        BooleanExpression month = (f.from() != null && f.to() != null)
                ? m.endDate.goe(f.from()).and(m.startDate.loe(f.to()))
                : null;

        BooleanExpression category = (f.memoryCategory() != null)
                ? m.category.eq(f.memoryCategory())
                : null;

        return queryFactory
                .select(m.id)
                .from(m)
                .where(accessible, month, category)
                .orderBy(m.endDate.desc().nullsLast(),
                        m.startDate.desc().nullsLast(),
                        m.id.desc())
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