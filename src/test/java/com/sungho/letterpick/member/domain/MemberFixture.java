package com.sungho.letterpick.member.domain;

import com.sungho.letterpick.common.auth.SocialProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

    public static Member createMember() {
        return createMember("test@example.com", "테스트유저");
    }

    public static Member createMember(String email, String nickname) {
        return Member.register(
                new Email(email),
                new Nickname(nickname),
                new SocialIdentity(SocialProvider.GOOGLE, "google-" + email)
        );
    }

    public static Member createMemberWithId(Long memberId) {
        Member member = createMember(
                "test-" + memberId + "@example.com",
                "테스트유저" + memberId
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}
