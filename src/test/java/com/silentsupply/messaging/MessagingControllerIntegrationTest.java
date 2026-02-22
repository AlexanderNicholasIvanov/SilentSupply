package com.silentsupply.messaging;

import com.silentsupply.attachment.AttachmentRepository;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.messaging.dto.ConversationResponse;
import com.silentsupply.messaging.dto.MessageResponse;
import com.silentsupply.messaging.dto.SendMessageRequest;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.proposal.ProposalRepository;
import com.silentsupply.rfq.RfqRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MessagingController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MessagingControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long buyerCompanyId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        participantRepository.deleteAll();
        conversationRepository.deleteAll();
        notificationRepository.deleteAll();
        attachmentRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("MsgSupplier", "msg-supplier@example.com", CompanyRole.SUPPLIER);
        AuthResponse buyerAuth = registerAndGetAuth("MsgBuyer", "msg-buyer@example.com", CompanyRole.BUYER);
        buyerToken = buyerAuth.getToken();
        buyerCompanyId = buyerAuth.getCompanyId();
    }

    @Test
    void sendDirectMessage_createsConversationAndMessage() {
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(buyerCompanyId)
                .content("Hello from supplier!")
                .build();

        ResponseEntity<MessageResponse> response = restTemplate.exchange(
                "/api/messages", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("Hello from supplier!");
        assertThat(response.getBody().getConversationId()).isNotNull();
    }

    @Test
    void getConversations_returnsConversationsForUser() {
        // Send a message to create a conversation
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(buyerCompanyId)
                .content("Test message")
                .build();
        restTemplate.exchange("/api/messages", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                MessageResponse.class);

        // Supplier should see the conversation
        ResponseEntity<String> conversations = restTemplate.exchange(
                "/api/messages/conversations", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                String.class);

        assertThat(conversations.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(conversations.getBody()).contains("DIRECT");
        assertThat(conversations.getBody()).contains("Test message");
    }

    @Test
    void getMessages_returnsPaginatedMessages() {
        // Create conversation with a message
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(buyerCompanyId)
                .content("First message")
                .build();
        ResponseEntity<MessageResponse> msgResponse = restTemplate.exchange(
                "/api/messages", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                MessageResponse.class);
        Long conversationId = msgResponse.getBody().getConversationId();

        // Send another message
        SendMessageRequest request2 = SendMessageRequest.builder()
                .conversationId(conversationId)
                .content("Second message")
                .build();
        restTemplate.exchange("/api/messages", HttpMethod.POST,
                new HttpEntity<>(request2, authHeaders(supplierToken)),
                MessageResponse.class);

        // Get messages
        ResponseEntity<String> messages = restTemplate.exchange(
                "/api/messages/conversations/" + conversationId + "?page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                String.class);

        assertThat(messages.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(messages.getBody()).contains("First message");
        assertThat(messages.getBody()).contains("Second message");
    }

    @Test
    void markAsRead_updatesReadStatus() {

        // Supplier sends message
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(buyerCompanyId)
                .content("Read me")
                .build();
        ResponseEntity<MessageResponse> msgResponse = restTemplate.exchange(
                "/api/messages", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                MessageResponse.class);
        Long conversationId = msgResponse.getBody().getConversationId();

        // Buyer has unread messages
        ResponseEntity<Map> unreadBefore = restTemplate.exchange(
                "/api/messages/unread-count", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                Map.class);
        assertThat(((Number) unreadBefore.getBody().get("unreadCount")).longValue()).isGreaterThan(0);

        // Buyer marks as read
        ResponseEntity<Void> readResponse = restTemplate.exchange(
                "/api/messages/conversations/" + conversationId + "/read",
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders(buyerToken)),
                Void.class);
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Unread count should now be 0
        ResponseEntity<Map> unreadAfter = restTemplate.exchange(
                "/api/messages/unread-count", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                Map.class);
        assertThat(((Number) unreadAfter.getBody().get("unreadCount")).longValue()).isZero();
    }

    @Test
    void unreadCount_returnsZeroForNewUser() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/messages/unread-count", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) response.getBody().get("unreadCount")).longValue()).isZero();
    }

    @Test
    void sendMessage_unauthenticated_returns401() {
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(1L)
                .content("Should fail")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/messages", request, String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void bidirectionalConversation_bothPartiesCanSendAndRead() {
        // Supplier starts conversation
        SendMessageRequest request1 = SendMessageRequest.builder()
                .recipientCompanyId(buyerCompanyId)
                .content("Hey buyer!")
                .build();
        ResponseEntity<MessageResponse> msg1 = restTemplate.exchange(
                "/api/messages", HttpMethod.POST,
                new HttpEntity<>(request1, authHeaders(supplierToken)),
                MessageResponse.class);
        Long conversationId = msg1.getBody().getConversationId();

        // Buyer replies to same conversation
        SendMessageRequest request2 = SendMessageRequest.builder()
                .conversationId(conversationId)
                .content("Hey supplier!")
                .build();
        ResponseEntity<MessageResponse> msg2 = restTemplate.exchange(
                "/api/messages", HttpMethod.POST,
                new HttpEntity<>(request2, authHeaders(buyerToken)),
                MessageResponse.class);

        assertThat(msg2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(msg2.getBody().getConversationId()).isEqualTo(conversationId);

        // Both should see the conversation
        ResponseEntity<ConversationResponse[]> supplierConvs = restTemplate.exchange(
                "/api/messages/conversations", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ConversationResponse[].class);
        ResponseEntity<ConversationResponse[]> buyerConvs = restTemplate.exchange(
                "/api/messages/conversations", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ConversationResponse[].class);

        assertThat(supplierConvs.getBody()).hasSize(1);
        assertThat(buyerConvs.getBody()).hasSize(1);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        return registerAndGetAuth(name, email, role).getToken();
    }

    private AuthResponse registerAndGetAuth(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
