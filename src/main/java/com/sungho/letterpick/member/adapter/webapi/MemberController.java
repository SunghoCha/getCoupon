package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.common.auth.CurrentUser;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerApi {

    private final MemberModifier memberModifier;

    @Override
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@CurrentUser LoginUser loginUser) {
        memberModifier.withdraw(loginUser.memberId());
    }

    @Override
    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeNickname(
            @CurrentUser LoginUser loginUser,
            @Valid @RequestBody MemberNicknameChangeRequest request) {
        memberModifier.changeNickname(loginUser.memberId(), request);
    }
}
