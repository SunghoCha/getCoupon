package com.sungho.letterpick.newsletter.adapter.persistence;

import com.sungho.letterpick.newsletter.domain.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewslettersRepository extends JpaRepository<Newsletter, Long>, CustomNewslettersRepository {

    Optional<Newsletter> findByEmailAddress(String address);
}
