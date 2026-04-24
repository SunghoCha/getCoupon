package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberSuspendRequest;
import com.sungho.letterpick.member.application.provided.MemberWithdrawByAdminRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController implements AdminMemberControllerApi {

    private final MemberModifier memberModifier;

    @Override
    @PostMapping("/suspension")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void suspend(@Valid @RequestBody MemberSuspendRequest request) {
        memberModifier.suspend(request);
    }

    @Override
    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void withdrawByAdmin(@Valid @RequestBody MemberWithdrawByAdminRequest request) {
        memberModifier.withdrawByAdmin(request);
    }
}
