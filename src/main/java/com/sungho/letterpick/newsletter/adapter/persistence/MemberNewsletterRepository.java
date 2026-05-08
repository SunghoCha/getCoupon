package com.sungho.letterpick.newsletter.adapter.persistence;

import com.sungho.letterpick.newsletter.domain.MemberNewsletter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberNewsletterRepository extends JpaRepository<MemberNewsletter, Long> {
    Optional<MemberNewsletter> findByMemberIdAndNewsletterId(Long memberId, Long newsletterId);
}
