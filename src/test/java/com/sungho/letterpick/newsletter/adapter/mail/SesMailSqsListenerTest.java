package com.sungho.letterpick.newsletter.adapter.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SesMailSqsListenerTest {

    private static final String SQS_MESSAGE_BODY = "ses notification json";

    @Mock
    private SesMailReceiveProcessor processor;

    @Test
    @DisplayName("SQS message body를 SES mail receive processor에 전달한다")
    void receive_delegates_sqs_message_body_to_processor() {
        // given
        SesMailSqsListener listener = new SesMailSqsListener(processor);

        // when
        listener.receive(SQS_MESSAGE_BODY);

        // then
        verify(processor).process(SQS_MESSAGE_BODY);
    }
}
