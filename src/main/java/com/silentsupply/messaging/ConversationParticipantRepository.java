package com.silentsupply.messaging;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link ConversationParticipant} entities.
 */
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    /**
     * Finds all participants in a conversation, eagerly loading company data.
     *
     * @param conversationId the conversation ID
     * @return list of participants
     */
    @EntityGraph(attributePaths = "company")
    List<ConversationParticipant> findByConversationId(Long conversationId);

    /**
     * Finds a participant record for a specific company in a specific conversation.
     *
     * @param conversationId the conversation ID
     * @param companyId      the company ID
     * @return the participant if found
     */
    Optional<ConversationParticipant> findByConversationIdAndCompanyId(Long conversationId, Long companyId);

    /**
     * Checks whether a company is a participant in a conversation.
     *
     * @param conversationId the conversation ID
     * @param companyId      the company ID
     * @return true if the company participates in the conversation
     */
    boolean existsByConversationIdAndCompanyId(Long conversationId, Long companyId);
}
