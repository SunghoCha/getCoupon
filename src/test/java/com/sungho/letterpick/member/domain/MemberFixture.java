package com.sungho.letterpick.member.domain;

import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

    public static Member createMember() {
        return Member.register(
                new Email("test@example.com"),
                new Nickname("테스트유저")
        );
    }

    public static Member createMember(Long id) {
        Member member = Member.register(new Email("test@example.com"),
                new Nickname("테스트유저"));
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

}
