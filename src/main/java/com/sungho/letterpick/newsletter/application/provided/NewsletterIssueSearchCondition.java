package com.sungho.letterpick.newsletter.application.provided;

import java.time.Instant;

public record NewsletterIssueSearchCondition(
        Instant receivedFrom,
        Instant receivedTo
) {

}
