package com.sungho.letterpick.member.adapter.seed;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.domain.Email;
import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalMemberSeedRunnerTest {

    private static final String LOCAL_TEST_MEMBER_EMAIL = "tjdgh1129@gmail.com";
    private static final String LOCAL_TEST_MEMBER_INBOX_ADDRESS = "omcxewctypxo@inbound-dev.letterpicknews.com";
    private static final String LOCAL_TEST_MEMBER_SOCIAL_ID = "108786395888237010397";

    @InjectMocks
    private LocalMemberSeedRunner runner;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("로컬 테스트 회원이 없으면 저장한다")
    void run_saves_local_test_member_when_missing() {
        // given
        given(memberRepository.findBySocialIdentity(any(SocialIdentity.class)))
                .willReturn(Optional.empty());

        // when
        runner.run(null);

        // then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member member = memberCaptor.getValue();
        assertThat(member.getEmail().address()).isEqualTo(LOCAL_TEST_MEMBER_EMAIL);
        assertThat(member.getNewsletterInboxAddress().address()).isEqualTo(LOCAL_TEST_MEMBER_INBOX_ADDRESS);
        assertThat(member.getSocialIdentity().socialProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(member.getSocialIdentity().socialProviderId()).isEqualTo(LOCAL_TEST_MEMBER_SOCIAL_ID);
    }

    @Test
    @DisplayName("로컬 테스트 회원이 이미 있으면 저장하지 않는다")
    void run_skips_when_local_test_member_exists() {
        // given
        Member existingMember = Member.register(
                new Email(LOCAL_TEST_MEMBER_EMAIL),
                new Nickname("1132tttt"),
                new SocialIdentity(SocialProvider.GOOGLE, LOCAL_TEST_MEMBER_SOCIAL_ID),
                new NewsletterInboxAddress(LOCAL_TEST_MEMBER_INBOX_ADDRESS)
        );
        given(memberRepository.findBySocialIdentity(any(SocialIdentity.class)))
                .willReturn(Optional.of(existingMember));

        // when
        runner.run(null);

        // then
        verify(memberRepository, never()).save(any());
    }
}
