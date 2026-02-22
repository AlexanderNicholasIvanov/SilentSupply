package com.silentsupply.messaging;

import com.silentsupply.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation between companies, optionally scoped to an RFQ or order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    /** The type of conversation (DIRECT, RFQ, or ORDER). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    /** Reference ID for scoped conversations (RFQ ID or Order ID). Null for DIRECT. */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Optional subject line for the conversation. */
    @Column(length = 255)
    private String subject;

    /** Participants in this conversation. */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ConversationParticipant> participants = new ArrayList<>();

    /** Messages in this conversation. */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Message> messages = new ArrayList<>();
}
