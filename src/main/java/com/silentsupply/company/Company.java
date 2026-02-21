package com.silentsupply.company;

import com.silentsupply.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a registered business in the SilentSupply marketplace.
 * Each company has a role (SUPPLIER or BUYER) and holds contact information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    /** Business name of the company. */
    @Column(nullable = false)
    private String name;

    /** Contact email address, used as the login credential. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. */
    @Column(nullable = false)
    private String password;

    /** Role of this company in the marketplace. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyRole role;

    /** Contact phone number. */
    @Column(name = "contact_phone")
    private String contactPhone;

    /** Business address. */
    private String address;

    /** Whether the company has been verified by an admin. */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;
}
