package com.sungho.letterpick.member.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

    public static Member createMember() {
        return createMember("test@example.com", "테스트유저");
    }

    public static Member createMember(Long id) {
        Member member = createMember("test@example.com", "테스트유저");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static Member createMember(String email, String nickname) {
        return Member.register(
                new Email(email),
                new Nickname(nickname)
        );
    }
}
