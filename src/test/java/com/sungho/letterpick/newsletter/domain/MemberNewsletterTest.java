package com.sungho.letterpick.newsletter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.sungho.letterpick.newsletter.domain.MemberNewsletterStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class MemberNewsletterTest {

    @Test
    @DisplayName("create() 후 상태는 ACTIVE이다")
    void createSetsStatusActive() {
        // when
        MemberNewsletter memberNewsletter = MemberNewsletter.create(1L, 2L);
        // then
        assertThat(memberNewsletter.getStatus()).isEqualTo(ACTIVE);
    }
    
    @Test
    @DisplayName("ACTIVE 상태에서 구독해지하면 UNSUBSCRIBED가 된다")
    void unsubscribeWhenActive() {
        // given
        MemberNewsletter memberNewsletter = MemberNewsletterFixture.create();
        // when
        memberNewsletter.unsubscribe();
        // then
        assertThat(memberNewsletter.getStatus()).isEqualTo(UNSUBSCRIBED);
    }
    
    @Test
    @DisplayName("UNSUBSCRIBED 상태에서 다시 구독해지하면 상태를 유지한다")
    void unsubscribeWhenAlreadyUnsubscribed() {
        // given
        MemberNewsletter memberNewsletter = MemberNewsletterFixture.create();
        memberNewsletter.unsubscribe();
        // when
        memberNewsletter.unsubscribe();
        // then
        assertThat(memberNewsletter.getStatus()).isEqualTo(UNSUBSCRIBED);
    }

    @Test
    @DisplayName("UNSUBSCRIBED 상태에서 재구독하면 ACTIVE가 된다")
    void resubscribeWhenUnsubscribed() {
        // given
        MemberNewsletter memberNewsletter = MemberNewsletterFixture.create();
        memberNewsletter.unsubscribe();
        // when
        memberNewsletter.resubscribe();
        // then
        assertThat(memberNewsletter.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    @DisplayName("ACTIVE 상태에서 재구독하면 상태를 유지한다")
    void resubscribeWhenActive() {
        // given
        MemberNewsletter memberNewsletter = MemberNewsletterFixture.create();
        // when
        memberNewsletter.resubscribe();
        // then
        assertThat(memberNewsletter.getStatus()).isEqualTo(ACTIVE);
    }


}
