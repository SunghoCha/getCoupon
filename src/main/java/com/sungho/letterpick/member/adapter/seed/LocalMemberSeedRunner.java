package com.sungho.letterpick.member.adapter.seed;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.domain.Email;
import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.SocialIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@Profile("local")
@ConditionalOnProperty(name = "letterpick.seed.member.local-test.enabled", havingValue = "true")
public class LocalMemberSeedRunner implements ApplicationRunner {

    private static final String LOCAL_TEST_MEMBER_EMAIL = "tjdgh1129@gmail.com";
    private static final String LOCAL_TEST_MEMBER_NICKNAME = "1132tttt";
    private static final String LOCAL_TEST_MEMBER_SOCIAL_ID = "108786395888237010397";
    private static final String LOCAL_TEST_MEMBER_INBOX_ADDRESS = "omcxewctypxo@inbound-dev.letterpicknews.com";

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SocialIdentity socialIdentity = new SocialIdentity(SocialProvider.GOOGLE, LOCAL_TEST_MEMBER_SOCIAL_ID);
        if (memberRepository.findBySocialIdentity(socialIdentity).isPresent()) {
            log.info("로컬 테스트 Member 시드 이미 적재됨. skip (email={})", LOCAL_TEST_MEMBER_EMAIL);
            return;
        }

        Member member = Member.register(
                new Email(LOCAL_TEST_MEMBER_EMAIL),
                new Nickname(LOCAL_TEST_MEMBER_NICKNAME),
                socialIdentity,
                new NewsletterInboxAddress(LOCAL_TEST_MEMBER_INBOX_ADDRESS)
        );

        memberRepository.save(member);
        log.info("로컬 테스트 Member 시드 적재 완료. email={}, inboxAddress={}",
                LOCAL_TEST_MEMBER_EMAIL, LOCAL_TEST_MEMBER_INBOX_ADDRESS);
    }
}
