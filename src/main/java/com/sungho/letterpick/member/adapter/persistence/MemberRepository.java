package com.sungho.letterpick.member.adapter.persistence;

import com.sungho.letterpick.common.domain.Email;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.SocialIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(Email email);

    boolean existsByNickname(Nickname nickname);

    boolean existsByNewsletterInboxAddress(NewsletterInboxAddress newsletterInboxAddress);

    Optional<Member> findBySocialIdentity(SocialIdentity identity);

    Optional<Member> findByNewsletterInboxAddress(NewsletterInboxAddress newsletterInboxAddress);
}
