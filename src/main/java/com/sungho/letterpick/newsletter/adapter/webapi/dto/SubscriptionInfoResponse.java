package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import com.sungho.letterpick.newsletter.application.SubscriptionInfo;

import static java.util.Objects.requireNonNull;

public record SubscriptionInfoResponse(
        String status,
        String externalSubscribeUrl
) {

    public SubscriptionInfoResponse {
        requireNonNull(status);
    }

    public static SubscriptionInfoResponse from(SubscriptionInfo subscriptionInfo) {
        requireNonNull(subscriptionInfo);

        return new SubscriptionInfoResponse(
                subscriptionInfo.status().name(),
                subscriptionInfo.externalSubscribeUrl()
        );
    }
}
