package com.silentsupply.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Conversation} entities.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Finds a scoped conversation by type and reference ID.
     *
     * @param type        the conversation type (RFQ or ORDER)
     * @param referenceId the reference entity ID
     * @return the conversation if found
     */
    Optional<Conversation> findByTypeAndReferenceId(ConversationType type, Long referenceId);

    /**
     * Finds all conversations that a company participates in, ordered by most recent activity.
     *
     * @param companyId the company's ID
     * @return list of conversations
     */
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE p.company.id = :companyId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantCompanyId(@Param("companyId") Long companyId);

    /**
     * Finds a DIRECT conversation between two specific companies.
     *
     * @param type      must be DIRECT
     * @param companyA  first company ID
     * @param companyB  second company ID
     * @return the conversation if found
     */
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.participants p1 " +
           "JOIN c.participants p2 " +
           "WHERE c.type = :type " +
           "AND p1.company.id = :companyA " +
           "AND p2.company.id = :companyB")
    Optional<Conversation> findDirectConversation(
            @Param("type") ConversationType type,
            @Param("companyA") Long companyA,
            @Param("companyB") Long companyB);
}
