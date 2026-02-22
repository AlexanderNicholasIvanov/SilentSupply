package com.silentsupply.config;

import java.security.Principal;

/**
 * Principal implementation for WebSocket connections.
 * Spring uses {@link #getName()} for user-destination resolution (e.g., /user/queue/messages).
 */
public class CompanyPrincipal implements Principal {

    private final Long companyId;
    private final String email;

    /**
     * Creates a principal from authenticated company details.
     *
     * @param companyId the company's ID
     * @param email     the company's email
     */
    public CompanyPrincipal(Long companyId, String email) {
        this.companyId = companyId;
        this.email = email;
    }

    /**
     * Returns the company ID as a string. Spring STOMP uses this
     * for routing messages to {@code /user/{name}/queue/messages}.
     *
     * @return the company ID as a string
     */
    @Override
    public String getName() {
        return companyId.toString();
    }

    /**
     * Returns the company ID.
     *
     * @return the company ID
     */
    public Long getCompanyId() {
        return companyId;
    }

    /**
     * Returns the company email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }
}
