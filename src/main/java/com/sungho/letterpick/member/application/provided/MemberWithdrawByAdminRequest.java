package com.sungho.letterpick.member.application.provided;

import jakarta.validation.constraints.NotNull;

public record MemberWithdrawByAdminRequest(@NotNull Long memberId) {
}
