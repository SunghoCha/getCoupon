package com.sungho.letterpick.member.application.provided;

import jakarta.validation.constraints.NotNull;

public record MemberSuspendRequest(@NotNull Long memberId) {
}
