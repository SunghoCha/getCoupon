package com.sungho.letterpick.newsletter.domain;

public enum InboundEmailStatus {

    /**
     * 메일 수신 기록만 만들어진 초기 상태.
     */
    RECEIVED,

    /**
     * 정상 처리되어 사용자가 볼 NewsletterIssue가 생성된 상태.
     */
    ISSUE_CREATED,

    /**
     * 회원이 앱 내 구독 해지한 뉴스레터라서 raw 수신 이력은 남겼지만 사용자 이슈는 만들지 않은 상태.
     */
    SKIPPED_UNSUBSCRIBED,

    /**
     * 수신자 주소로 회원을 찾지 못한 상태.
     */
    RECIPIENT_NOT_FOUND,

    /**
     * 수신자 주소가 뉴스레터 수신 주소 형식과 맞지 않는 상태.
     */
    INVALID_RECIPIENT_ADDRESS,

    /**
     * 발신자 이메일로 등록된 뉴스레터를 찾지 못한 상태.
     */
    NEWSLETTER_NOT_FOUND
}
