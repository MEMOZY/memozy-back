package com.memozy.memozy_back.domain.memory.domain;

import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "memory_accesses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemoryAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_id", nullable = false)
    private Memory memory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionLevel permissionLevel; // VIEW, EDIT

    public void changeLevel(PermissionLevel newLevel) {
        this.permissionLevel = newLevel;
    }

    public static MemoryAccess create(Memory memory, User user, PermissionLevel level) {
        return MemoryAccess.builder()
                .memory(memory)
                .user(user)
                .permissionLevel(level)
                .build();
    }

    public boolean isForUser(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    public boolean canEdit() {
        return permissionLevel == PermissionLevel.EDITOR
                || permissionLevel == PermissionLevel.OWNER;
    }

    public boolean canView() {
        return canEdit() || permissionLevel == PermissionLevel.VIEWER;
    }
}