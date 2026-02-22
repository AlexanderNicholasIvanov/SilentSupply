package com.silentsupply.messaging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Spring Data repository for {@link Message} entities.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Finds messages in a conversation, paginated and ordered by creation time descending.
     *
     * @param conversationId the conversation ID
     * @param pageable       pagination parameters
     * @return page of messages
     */
    @EntityGraph(attributePaths = "senderCompany")
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /**
     * Counts all messages in a conversation (used when participant has never read it).
     *
     * @param conversationId the conversation ID
     * @return total message count
     */
    long countByConversationId(Long conversationId);

    /**
     * Counts messages created after a given timestamp (unread messages).
     *
     * @param conversationId the conversation ID
     * @param after          the timestamp to count messages after
     * @return count of messages after the timestamp
     */
    long countByConversationIdAndCreatedAtAfter(Long conversationId, LocalDateTime after);

    /**
     * Finds the most recent message in a conversation.
     *
     * @param conversationId the conversation ID
     * @return the latest message, or null if none
     */
    @EntityGraph(attributePaths = "senderCompany")
    Message findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
