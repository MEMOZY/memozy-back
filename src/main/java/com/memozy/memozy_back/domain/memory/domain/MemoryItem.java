package com.memozy.memozy_back.domain.memory.domain;

import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemoryItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_id", nullable = false)
    private Memory memory;

    @Column(length = 2048)
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    @Column
    private Integer sequence;

    public MemoryItem(Memory memory) {
        this.memory = memory;
    }

    public static MemoryItem create(String imageUrl, String description, int sequence, Memory memory) {
        return MemoryItem.builder()
                .imageUrl(imageUrl)
                .description(description)
                .sequence(sequence)
                .memory(memory)
                .build();
    }

    public MemoryItem update(String imageUrl, String description, int sequence) {
        return MemoryItem.builder()
                .id(this.id)
                .memory(this.memory)
                .imageUrl(imageUrl)
                .description(description)
                .sequence(sequence)
                .build();
    }
}