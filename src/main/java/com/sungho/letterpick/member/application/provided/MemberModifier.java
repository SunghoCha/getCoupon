package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.member.domain.Member;

public interface MemberModifier {
    Member register(MemberRegisterRequest request);

    void changeNickname(Long memberId, MemberNicknameChangeRequest request);

    void withdraw(Long requesterId);

    void suspend(MemberSuspendRequest request);

    void withdrawByAdmin(MemberWithdrawByAdminRequest request);
}
