package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.member.domain.Member;

public interface MemberRegister {
    Member register(MemberRegisterRequest request);

    void changeNickname(MemberNicknameChangeRequest request);

    void withdraw(Long requesterId);
}
