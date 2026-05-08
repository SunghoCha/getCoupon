package com.sungho.letterpick.newsletter.application;

public record SubscriptionInfo(
        SubscriptionStatus status,
        String externalSubscribeUrl
) {

    public SubscriptionInfo {
        if (status == null) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }

        if (status == SubscriptionStatus.NONE && externalSubscribeUrl == null) {
            throw new IllegalArgumentException("미구독 상태에는 외부 구독 URL이 필요합니다.");
        }

        if (status != SubscriptionStatus.NONE && externalSubscribeUrl != null) {
            throw new IllegalArgumentException("미구독 상태가 아니면 외부 구독 URL을 가질 수 없습니다.");
        }
    }

    public static SubscriptionInfo none(String externalSubscribeUrl) {
        return new SubscriptionInfo(SubscriptionStatus.NONE, externalSubscribeUrl);
    }

    public static SubscriptionInfo active() {
        return new SubscriptionInfo(SubscriptionStatus.ACTIVE, null);
    }

    public static SubscriptionInfo unsubscribed() {
        return new SubscriptionInfo(SubscriptionStatus.UNSUBSCRIBED, null);
    }


}
