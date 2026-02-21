package com.silentsupply.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Company} entities.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Finds a company by its email address.
     *
     * @param email the email to search for
     * @return the company if found
     */
    Optional<Company> findByEmail(String email);

    /**
     * Checks whether a company with the given email already exists.
     *
     * @param email the email to check
     * @return true if a company with this email exists
     */
    boolean existsByEmail(String email);
}
