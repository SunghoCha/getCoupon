package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.common.auth.CurrentUser;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.member.adapter.webapi.dto.MemberResponse;
import com.sungho.letterpick.member.application.provided.MemberFinder;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import com.sungho.letterpick.member.application.provided.MemberView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerApi {

    private final MemberModifier memberModifier;
    private final MemberFinder memberFinder;

    @Override
    @GetMapping("/me")
    public MemberResponse findMember(@CurrentUser LoginUser loginUser) {
        MemberView memberView = memberFinder.findMember(loginUser.memberId());
        return MemberResponse.from(memberView);
    }

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
