package com.memozy.memozy_back.domain.memory.domain;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "memory")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Memory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemoryCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemoryItem> memoryItems = new ArrayList<>();

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemoryAccess> accesses = new ArrayList<>();

    public static Memory create(
            User owner,
            String title,
            MemoryCategory category,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return Memory.builder()
                .owner(owner)
                .title(title)
                .category(category)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static Memory createWithoutBasicInfo(User owner) {
        return Memory.builder()
                .owner(owner)
                .memoryItems(new ArrayList<>())
                .accesses(new ArrayList<>())
                .build();
    }

    public void update(String title, MemoryCategory category,
            LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateBasicInfo(String title, MemoryCategory category,
            LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void addMemoryItem(MemoryItem item) {
        System.out.println("add " + item);
        this.memoryItems.add(item);
    }

    public void addAccess(MemoryAccess access) {
        this.accesses.add(access);
    }

    public void changeAccess(User target, PermissionLevel newLevel) {
        if (isOwner(target)) throw new BusinessException(ErrorCode.CANNOT_MANAGE_OWNER_ACCESS);
        MemoryAccess access = findAccessOrThrow(target);
        access.changeLevel(newLevel);
    }

    public void revokeAccess(User target) {
        if (isOwner(target)) throw new BusinessException(ErrorCode.CANNOT_MANAGE_OWNER_ACCESS);
        MemoryAccess access = findAccessOrThrow(target);
        this.accesses.remove(access);
    }

    private boolean isOwner(User user) {
        return owner != null && owner.equals(user);
    }

    private MemoryAccess findAccessOrThrow(User user) {
        return accesses.stream()
                .filter(a -> a.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PERMISSION));
    }


}