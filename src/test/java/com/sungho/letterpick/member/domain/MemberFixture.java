package com.sungho.letterpick.member.domain;

public class MemberFixture {

    public static Member createMember() {
        return createMember("test@example.com", "테스트유저");
    }

    public static Member createMember(String email, String nickname) {
        return Member.register(
                new Email(email),
                new Nickname(nickname)
        );
    }
}
