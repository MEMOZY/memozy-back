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
import jakarta.persistence.Transient;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = {"memory"})
public class MemoryItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_item_id")
    private Long id;

    @Transient // DB에 저장되지 않음
    private String tempId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_id", nullable = false)
    private Memory memory;

    @Column(length = 2048)
    private String fileKey;

    @Column(length = 1000)
    private String content;

    @Column
    private Integer sequence;

    public static MemoryItem create(Memory memory, String fileKey, String content, int sequence) {
        return MemoryItem.builder()
                .fileKey(fileKey)
                .content(content)
                .sequence(sequence)
                .memory(memory)
                .build();
    }

    public static MemoryItem createTempMemoryItem(Memory memory, String fileKey, String content, int sequence) {
        return MemoryItem.builder()
                .tempId(UUID.randomUUID().toString())
                .fileKey(fileKey)
                .content(content)
                .sequence(sequence)
                .memory(memory)
                .build();
    }

    public static MemoryItem fromTempDto(
            Memory memory,
            String fileKey,
            String content,
            int sequence,
            String tempId
    ) {
        return MemoryItem.builder()
                .tempId(tempId)
                .fileKey(fileKey)
                .content(content)
                .sequence(sequence)
                .memory(memory)
                .build();
    }



    public void updateContent(String content) {
        this.content = content;
    }

    public void updateFileKey(String movedFileKey) {
        this.fileKey = movedFileKey;
    }
}