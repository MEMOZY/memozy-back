package com.memozy.memozy_back.domain.memory.domain;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
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
    private List<MemoryShared> sharedUsers = new ArrayList<>();

    public static Memory init(
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

    public static Memory initWithoutBasicInfo(User owner) {
        return Memory.builder()
                .owner(owner)
                .memoryItems(new ArrayList<>())
                .sharedUsers(new ArrayList<>())
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
        this.memoryItems.add(item);
    }

    public void addSharedUser(MemoryShared memoryShared) {
        sharedUsers.add(memoryShared);
    }

}