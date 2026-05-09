package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.CurrentUser;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.SubscriptionInfoResponse;
import com.sungho.letterpick.newsletter.application.SubscriptionInfo;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterFinder;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterModifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/newsletter-subscriptions")
@RequiredArgsConstructor
public class MemberNewsletterController implements MemberNewsletterControllerApi {

    private final MemberNewsletterFinder memberNewsletterFinder;
    private final MemberNewsletterModifier memberNewsletterModifier;

    @Override
    @GetMapping("/{newsletterId}")
    public SubscriptionInfoResponse getSubscriptionInfo(
            @CurrentUser LoginUser loginUser,
            @PathVariable("newsletterId") Long newsletterId
    ) {
        SubscriptionInfo info = memberNewsletterFinder.findSubscriptionInfo(loginUser.memberId(), newsletterId);
        return SubscriptionInfoResponse.from(info);
    }

    @Override
    @PatchMapping("/{newsletterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resubscribe(@CurrentUser LoginUser loginUser,
                            @PathVariable("newsletterId") Long newsletterId) {
        memberNewsletterModifier.resubscribe(loginUser.memberId(), newsletterId);
    }

    @Override
    @DeleteMapping("/{newsletterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@CurrentUser LoginUser loginUser,
                            @PathVariable("newsletterId") Long newsletterId) {
        memberNewsletterModifier.unsubscribe(loginUser.memberId(), newsletterId);
    }

}
