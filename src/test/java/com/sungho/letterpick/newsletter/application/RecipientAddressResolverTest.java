package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RecipientAddressResolverTest {

    @InjectMocks
    private RecipientAddressResolver recipientAddressResolver;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("수신자 주소 형식이 바르지 않으면 INVALID_ADDRESS 결과를 반환하고 회원을 조회하지 않는다")
    void resolve_returns_invalid_address_when_recipient_address_is_invalid() {
        // given
        String recipientAddress = "test@inbound-dev.letterpicknews.com";
        // when
        RecipientAddressResolution resolution = recipientAddressResolver.resolve(recipientAddress);
        // then
        assertThat(resolution.type()).isEqualTo(RecipientAddressResolution.Type.INVALID_ADDRESS);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("수신자 주소 형식은 맞지만 회원을 찾지 못하면 NOT_FOUND 결과를 반환한다")
    void resolve_returns_not_found_when_member_does_not_exist() {
        // given
        String recipientAddress = "abcd1234efgh@inbound.letterpick.test";
        given(memberRepository.findByNewsletterInboxAddress(new NewsletterInboxAddress(recipientAddress)))
                .willReturn(Optional.empty());
        // when
        RecipientAddressResolution resolution = recipientAddressResolver.resolve(recipientAddress);
        // then
        assertThat(resolution.type()).isEqualTo(RecipientAddressResolution.Type.NOT_FOUND);
        verify(memberRepository).findByNewsletterInboxAddress(new NewsletterInboxAddress(recipientAddress));
    }

    @Test
    @DisplayName("수신자 주소로 회원을 찾으면 FOUND 결과와 memberId를 반환한다")
    void resolve_returns_found_when_member_exists() {
        // given
        Long memberId = 1L;
        String recipientAddress = "abcd1234efgh@inbound.letterpick.test";
        Member member = MemberFixture.createMemberWithId(memberId);
        given(memberRepository.findByNewsletterInboxAddress(new NewsletterInboxAddress(recipientAddress)))
                .willReturn(Optional.of(member));
        // when
        RecipientAddressResolution resolution = recipientAddressResolver.resolve(recipientAddress);
        // then
        assertThat(resolution.type()).isEqualTo(RecipientAddressResolution.Type.FOUND);
        assertThat(resolution.memberId()).isEqualTo(memberId);
        verify(memberRepository).findByNewsletterInboxAddress(new NewsletterInboxAddress(recipientAddress));
    }
}
